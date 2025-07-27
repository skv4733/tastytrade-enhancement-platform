package com.tastytrade.marketdata.service;

import com.tastytrade.marketdata.model.MarketDataEvent;
import com.tastytrade.marketdata.model.OptionsQuote;
import com.tastytrade.marketdata.repository.OptionsQuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);
    
    @Autowired
    private OptionsQuoteRepository optionsQuoteRepository;
    
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    
    private final Map<String, Sinks.Many<OptionsQuote>> symbolStreams = new ConcurrentHashMap<>();
    private final Sinks.Many<MarketDataEvent> marketDataSink = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<OptionsQuote> getQuoteStream(String symbol) {
        return symbolStreams.computeIfAbsent(symbol, 
                k -> Sinks.many().multicast().onBackpressureBuffer())
                .asFlux()
                .onBackpressureLatest()
                .doOnSubscribe(subscription -> logger.info("New subscriber for symbol: {}", symbol))
                .doOnCancel(() -> logger.info("Subscription cancelled for symbol: {}", symbol));
    }

    public Flux<MarketDataEvent> getMarketDataStream() {
        return marketDataSink.asFlux()
                .onBackpressureLatest()
                .doOnSubscribe(subscription -> logger.info("New market data stream subscriber"))
                .doOnCancel(() -> logger.info("Market data stream subscription cancelled"));
    }

    public Mono<Void> publishQuote(OptionsQuote quote) {
        return Mono.fromRunnable(() -> {
            try {
                // Save to database
                optionsQuoteRepository.save(quote);
                
                // Cache in Redis
                cacheQuote(quote);
                
                // Publish to WebSocket streams
                Sinks.Many<OptionsQuote> sink = symbolStreams.get(quote.getSymbol());
                if (sink != null) {
                    sink.tryEmitNext(quote);
                }
                
                // Publish as market data event
                MarketDataEvent event = convertToMarketDataEvent(quote);
                marketDataSink.tryEmitNext(event);
                
                logger.debug("Published quote for symbol: {} at price: {}", 
                           quote.getSymbol(), quote.getPrice());
                
            } catch (Exception e) {
                logger.error("Error publishing quote for symbol: {}", quote.getSymbol(), e);
            }
        });
    }

    public Mono<OptionsQuote> getLatestQuote(String symbol) {
        return redisTemplate.opsForValue()
                .get("quote:" + symbol)
                .cast(OptionsQuote.class)
                .switchIfEmpty(
                    Mono.fromCallable(() -> optionsQuoteRepository.findLatestBySymbol(symbol))
                        .flatMap(quote -> {
                            if (quote != null) {
                                cacheQuote(quote);
                            }
                            return Mono.justOrEmpty(quote);
                        })
                );
    }

    public Flux<OptionsQuote> getHistoricalQuotes(String symbol, Instant fromTime, Instant toTime) {
        return Flux.fromIterable(
                optionsQuoteRepository.findBySymbolAndQuoteTimeBetween(symbol, fromTime, toTime)
        );
    }

    public Flux<OptionsQuote> getRealtimeQuotes(String symbol) {
        // Start with latest cached quote
        return getLatestQuote(symbol)
                .flux()
                // Then continue with real-time stream
                .concatWith(getQuoteStream(symbol))
                .distinctUntilChanged(quote -> quote.getPrice() + "|" + quote.getTimestamp());
    }

    public Mono<Void> startMarketDataSimulation() {
        logger.info("Starting market data simulation");
        
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> {
                    // Simulate market data for popular options symbols
                    return Flux.just("AAPL", "TSLA", "SPY", "QQQ", "NVDA")
                            .flatMap(this::generateSimulatedQuote)
                            .flatMap(this::publishQuote);
                })
                .then();
    }

    private Mono<OptionsQuote> generateSimulatedQuote(String underlying) {
        return Mono.fromCallable(() -> {
            OptionsQuote quote = new OptionsQuote();
            quote.setSymbol(underlying + "240119C00150000"); // Sample option symbol
            quote.setPrice(java.math.BigDecimal.valueOf(Math.random() * 10 + 5));
            quote.setUnderlyingPrice(java.math.BigDecimal.valueOf(Math.random() * 200 + 100));
            quote.setStrikePrice(java.math.BigDecimal.valueOf(150));
            quote.setBidPrice(quote.getPrice().subtract(java.math.BigDecimal.valueOf(0.05)));
            quote.setAskPrice(quote.getPrice().add(java.math.BigDecimal.valueOf(0.05)));
            quote.setBidSize((int)(Math.random() * 100 + 10));
            quote.setAskSize((int)(Math.random() * 100 + 10));
            quote.setVolume((long)(Math.random() * 1000));
            quote.setOpenInterest((int)(Math.random() * 10000 + 1000));
            quote.setImpliedVolatility(java.math.BigDecimal.valueOf(Math.random() * 0.5 + 0.1));
            quote.setDelta(java.math.BigDecimal.valueOf(Math.random() * 0.8 + 0.1));
            quote.setGamma(java.math.BigDecimal.valueOf(Math.random() * 0.1));
            quote.setTheta(java.math.BigDecimal.valueOf(-Math.random() * 0.05));
            quote.setVega(java.math.BigDecimal.valueOf(Math.random() * 0.3));
            quote.setRho(java.math.BigDecimal.valueOf(Math.random() * 0.1));
            quote.setOptionType("CALL");
            quote.setQuoteTime(Instant.now());
            
            return quote;
        });
    }

    private void cacheQuote(OptionsQuote quote) {
        try {
            redisTemplate.opsForValue()
                    .set("quote:" + quote.getSymbol(), quote.toString(), Duration.ofMinutes(5))
                    .subscribe();
        } catch (Exception e) {
            logger.warn("Failed to cache quote for symbol: {}", quote.getSymbol(), e);
        }
    }

    private MarketDataEvent convertToMarketDataEvent(OptionsQuote quote) {
        return MarketDataEvent.builder()
                .symbol(quote.getSymbol())
                .price(quote.getPrice())
                .underlyingPrice(quote.getUnderlyingPrice())
                .strikePrice(quote.getStrikePrice())
                .impliedVolatility(quote.getImpliedVolatility())
                .delta(quote.getDelta())
                .gamma(quote.getGamma())
                .theta(quote.getTheta())
                .vega(quote.getVega())
                .rho(quote.getRho())
                .volume(quote.getVolume())
                .bidPrice(quote.getBidPrice())
                .askPrice(quote.getAskPrice())
                .bidSize(quote.getBidSize())
                .askSize(quote.getAskSize())
                .openInterest(quote.getOpenInterest())
                .optionType(quote.getOptionType())
                .eventType("QUOTE")
                .timestamp(quote.getQuoteTime())
                .build();
    }

    public Mono<Long> getActiveSubscriberCount(String symbol) {
        Sinks.Many<OptionsQuote> sink = symbolStreams.get(symbol);
        return Mono.just(sink != null ? sink.currentSubscriberCount() : 0L);
    }

    public Mono<Void> cleanupInactiveStreams() {
        return Mono.fromRunnable(() -> {
            symbolStreams.entrySet().removeIf(entry -> {
                boolean hasSubscribers = entry.getValue().currentSubscriberCount() > 0;
                if (!hasSubscribers) {
                    logger.debug("Cleaning up inactive stream for symbol: {}", entry.getKey());
                }
                return !hasSubscribers;
            });
        });
    }
}