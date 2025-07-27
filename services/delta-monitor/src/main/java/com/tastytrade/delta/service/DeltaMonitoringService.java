package com.tastytrade.delta.service;

import com.tastytrade.delta.model.AlertMessage;
import com.tastytrade.delta.model.AlertPriority;
import com.tastytrade.delta.model.AlertType;
import com.tastytrade.delta.model.DeltaThresholdEvent;
import com.tastytrade.marketdata.model.MarketDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DeltaMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeltaMonitoringService.class);
    
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private OptionsCalculatorService calculatorService;
    
    private final ConcurrentMap<String, BigDecimal> lastDeltaValues = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> lastAlertTimes = new ConcurrentHashMap<>();
    
    private static final BigDecimal DEFAULT_DELTA_THRESHOLD = BigDecimal.valueOf(0.1);
    private static final long ALERT_COOLDOWN_SECONDS = 300; // 5 minutes

    @EventListener
    @Async
    public void handleMarketDataUpdate(MarketDataEvent event) {
        if (event.getDelta() == null || event.getSymbol() == null) {
            return;
        }
        
        processOptionsGreeks(event)
                .flatMap(this::checkThresholds)
                .filter(this::exceedsThreshold)
                .subscribe(this::triggerAlert,
                          error -> logger.error("Error processing delta monitoring for {}: {}", 
                                               event.getSymbol(), error.getMessage()));
    }

    private Mono<OptionsGreeks> processOptionsGreeks(MarketDataEvent event) {
        return Mono.fromCallable(() -> {
            try {
                // Extract Greeks from market data event
                OptionsGreeks greeks = OptionsGreeks.builder()
                        .symbol(event.getSymbol())
                        .delta(event.getDelta())
                        .gamma(event.getGamma())
                        .theta(event.getTheta())
                        .vega(event.getVega())
                        .rho(event.getRho())
                        .underlyingPrice(event.getUnderlyingPrice())
                        .strikePrice(event.getStrikePrice())
                        .impliedVolatility(event.getImpliedVolatility())
                        .timestamp(event.getTimestamp())
                        .build();
                
                logger.debug("Processed Greeks for symbol: {} - Delta: {}", 
                           event.getSymbol(), greeks.getDelta());
                
                return greeks;
                
            } catch (Exception e) {
                logger.error("Error processing Greeks for symbol: {}", event.getSymbol(), e);
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<DeltaThresholdEvent> checkThresholds(OptionsGreeks greeks) {
        return redisTemplate.opsForValue()
                .get("threshold:" + greeks.getSymbol())
                .defaultIfEmpty(DEFAULT_DELTA_THRESHOLD.toString())
                .map(thresholdStr -> {
                    try {
                        BigDecimal currentDelta = greeks.getDelta();
                        BigDecimal threshold = new BigDecimal(thresholdStr);
                        BigDecimal previousDelta = lastDeltaValues.get(greeks.getSymbol());
                        
                        // Update last delta value
                        lastDeltaValues.put(greeks.getSymbol(), currentDelta);
                        
                        return DeltaThresholdEvent.builder()
                                .symbol(greeks.getSymbol())
                                .currentDelta(currentDelta)
                                .threshold(threshold)
                                .previousDelta(previousDelta)
                                .timestamp(Instant.now())
                                .triggeredBy("market_data_update")
                                .build();
                        
                    } catch (Exception e) {
                        logger.error("Error checking thresholds for symbol: {}", greeks.getSymbol(), e);
                        return null;
                    }
                })
                .filter(event -> event != null);
    }

    private boolean exceedsThreshold(DeltaThresholdEvent event) {
        if (event.getCurrentDelta() == null || event.getThreshold() == null) {
            return false;
        }
        
        // Check if we're in cooldown period
        Instant lastAlert = lastAlertTimes.get(event.getSymbol());
        if (lastAlert != null && 
            Instant.now().getEpochSecond() - lastAlert.getEpochSecond() < ALERT_COOLDOWN_SECONDS) {
            logger.debug("Skipping alert for {} - still in cooldown period", event.getSymbol());
            return false;
        }
        
        BigDecimal deltaChange = event.getCurrentDelta().abs();
        BigDecimal thresholdAbs = event.getThreshold().abs();
        
        boolean exceeds = deltaChange.compareTo(thresholdAbs) > 0;
        
        // Also check for significant delta change if we have previous value
        if (!exceeds && event.getPreviousDelta() != null) {
            BigDecimal deltaMovement = event.getCurrentDelta()
                    .subtract(event.getPreviousDelta()).abs();
            exceeds = deltaMovement.compareTo(BigDecimal.valueOf(0.05)) > 0; // 5% delta change
        }
        
        if (exceeds) {
            logger.info("Delta threshold exceeded for {}: current={}, threshold={}", 
                       event.getSymbol(), event.getCurrentDelta(), event.getThreshold());
        }
        
        return exceeds;
    }

    private void triggerAlert(DeltaThresholdEvent event) {
        try {
            // Update last alert time
            lastAlertTimes.put(event.getSymbol(), Instant.now());
            
            AlertMessage alert = AlertMessage.builder()
                    .type(AlertType.DELTA_THRESHOLD_BREACH)
                    .symbol(event.getSymbol())
                    .accountNumber(event.getAccountNumber())
                    .currentValue(event.getCurrentDelta())
                    .threshold(event.getThreshold())
                    .message(formatAlertMessage(event))
                    .priority(determinePriority(event))
                    .timestamp(event.getTimestamp())
                    .build();

            // Send alert to notification service via RabbitMQ
            rabbitTemplate.convertAndSend("alerts.exchange", "delta.threshold", alert);
            
            // Store alert in Redis for quick access
            storeAlertInRedis(alert);
            
            logger.info("Delta threshold alert triggered for symbol: {} - Alert ID: {}", 
                       event.getSymbol(), alert.getId());
            
        } catch (Exception e) {
            logger.error("Error triggering alert for symbol: {}", event.getSymbol(), e);
        }
    }

    public Mono<Void> setDeltaThreshold(String symbol, BigDecimal threshold) {
        return redisTemplate.opsForValue()
                .set("threshold:" + symbol, threshold.toString())
                .doOnSuccess(success -> logger.info("Set delta threshold for {}: {}", symbol, threshold))
                .doOnError(error -> logger.error("Error setting threshold for {}: {}", symbol, error.getMessage()))
                .then();
    }

    public Mono<BigDecimal> getDeltaThreshold(String symbol) {
        return redisTemplate.opsForValue()
                .get("threshold:" + symbol)
                .map(BigDecimal::new)
                .defaultIfEmpty(DEFAULT_DELTA_THRESHOLD);
    }

    public Mono<Void> enableAdaptiveThresholds(String symbol) {
        return Mono.fromRunnable(() -> {
            // Calculate adaptive threshold based on historical volatility
            BigDecimal adaptiveThreshold = calculateAdaptiveThreshold(symbol);
            setDeltaThreshold(symbol, adaptiveThreshold).subscribe();
            
            logger.info("Enabled adaptive thresholds for symbol: {} with threshold: {}", 
                       symbol, adaptiveThreshold);
        });
    }

    private BigDecimal calculateAdaptiveThreshold(String symbol) {
        // This would typically analyze historical delta movements
        // For now, return a simple adaptive threshold
        BigDecimal baseThreshold = DEFAULT_DELTA_THRESHOLD;
        
        // Get recent delta values for this symbol
        BigDecimal recentDelta = lastDeltaValues.get(symbol);
        if (recentDelta != null) {
            // Adjust threshold based on current delta magnitude
            BigDecimal adjustment = recentDelta.abs().multiply(BigDecimal.valueOf(0.1));
            baseThreshold = baseThreshold.add(adjustment);
        }
        
        return baseThreshold.min(BigDecimal.valueOf(0.3)); // Cap at 30%
    }

    private String formatAlertMessage(DeltaThresholdEvent event) {
        return String.format("Delta threshold breach detected for %s. Current delta: %s, Threshold: %s. " +
                           "Previous delta: %s. Time: %s",
                           event.getSymbol(),
                           event.getCurrentDelta(),
                           event.getThreshold(),
                           event.getPreviousDelta(),
                           event.getTimestamp());
    }

    private AlertPriority determinePriority(DeltaThresholdEvent event) {
        if (event.getCurrentDelta() == null || event.getThreshold() == null) {
            return AlertPriority.LOW;
        }
        
        BigDecimal ratio = event.getCurrentDelta().abs()
                .divide(event.getThreshold().abs(), 2, java.math.RoundingMode.HALF_UP);
        
        if (ratio.compareTo(BigDecimal.valueOf(3.0)) >= 0) {
            return AlertPriority.CRITICAL;
        } else if (ratio.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
            return AlertPriority.HIGH;
        } else if (ratio.compareTo(BigDecimal.valueOf(1.5)) >= 0) {
            return AlertPriority.MEDIUM;
        } else {
            return AlertPriority.LOW;
        }
    }

    private void storeAlertInRedis(AlertMessage alert) {
        try {
            String alertKey = "alert:" + alert.getSymbol() + ":" + alert.getId();
            redisTemplate.opsForValue()
                    .set(alertKey, alert.toString(), java.time.Duration.ofHours(24))
                    .subscribe();
        } catch (Exception e) {
            logger.warn("Failed to store alert in Redis: {}", e.getMessage());
        }
    }

    public Mono<Long> getActiveAlertsCount() {
        return redisTemplate.keys("alert:*")
                .count();
    }

    public Mono<Void> acknowledgeAlert(String alertId) {
        return redisTemplate.opsForValue()
                .get("alert:*:" + alertId)
                .doOnNext(alert -> logger.info("Alert acknowledged: {}", alertId))
                .then();
    }

    // Mock classes for compilation - these would be injected from other services
    private static class OptionsGreeks {
        private String symbol;
        private BigDecimal delta;
        private BigDecimal gamma;
        private BigDecimal theta;
        private BigDecimal vega;
        private BigDecimal rho;
        private BigDecimal underlyingPrice;
        private BigDecimal strikePrice;
        private BigDecimal impliedVolatility;
        private Instant timestamp;

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private OptionsGreeks greeks = new OptionsGreeks();
            public Builder symbol(String symbol) { greeks.symbol = symbol; return this; }
            public Builder delta(BigDecimal delta) { greeks.delta = delta; return this; }
            public Builder gamma(BigDecimal gamma) { greeks.gamma = gamma; return this; }
            public Builder theta(BigDecimal theta) { greeks.theta = theta; return this; }
            public Builder vega(BigDecimal vega) { greeks.vega = vega; return this; }
            public Builder rho(BigDecimal rho) { greeks.rho = rho; return this; }
            public Builder underlyingPrice(BigDecimal underlyingPrice) { greeks.underlyingPrice = underlyingPrice; return this; }
            public Builder strikePrice(BigDecimal strikePrice) { greeks.strikePrice = strikePrice; return this; }
            public Builder impliedVolatility(BigDecimal impliedVolatility) { greeks.impliedVolatility = impliedVolatility; return this; }
            public Builder timestamp(Instant timestamp) { greeks.timestamp = timestamp; return this; }
            public OptionsGreeks build() { return greeks; }
        }

        public String getSymbol() { return symbol; }
        public BigDecimal getDelta() { return delta; }
    }

    private static class OptionsCalculatorService {
        // Mock service - would be injected
    }
}