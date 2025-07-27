package com.tastytrade.calculator.service;

import com.tastytrade.calculator.calculator.BlackScholesCalculator;
import com.tastytrade.calculator.model.BlackScholesParameters;
import com.tastytrade.calculator.model.OptionsGreeks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OptionsCalculatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(OptionsCalculatorService.class);
    
    @Autowired
    private BlackScholesCalculator blackScholesCalculator;

    @Cacheable(value = "optionsGreeks", key = "#params.hashCode()")
    public OptionsGreeks calculateGreeks(BlackScholesParameters params) {
        try {
            logger.debug("Calculating Greeks for: S={}, K={}, T={}, r={}, σ={}, type={}", 
                        params.getUnderlyingPrice(),
                        params.getStrikePrice(),
                        params.getTimeToExpiry(),
                        params.getRiskFreeRate(),
                        params.getVolatility(),
                        params.getOptionType());
            
            OptionsGreeks greeks = blackScholesCalculator.calculateGreeks(params);
            
            logger.debug("Calculated Greeks: δ={}, Γ={}, Θ={}, ν={}, ρ={}", 
                        greeks.getDelta(),
                        greeks.getGamma(),
                        greeks.getTheta(),
                        greeks.getVega(),
                        greeks.getRho());
            
            return greeks;
        } catch (Exception e) {
            logger.error("Error calculating Greeks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate option Greeks", e);
        }
    }

    public CompletableFuture<OptionsGreeks> calculateGreeksAsync(BlackScholesParameters params) {
        return CompletableFuture.supplyAsync(() -> calculateGreeks(params));
    }

    public BigDecimal calculateImpliedVolatility(BlackScholesParameters params, BigDecimal marketPrice) {
        try {
            logger.debug("Calculating implied volatility for market price: {}", marketPrice);
            
            BigDecimal impliedVol = blackScholesCalculator.calculateImpliedVolatility(params, marketPrice);
            
            logger.debug("Calculated implied volatility: {}", impliedVol);
            
            return impliedVol;
        } catch (Exception e) {
            logger.error("Error calculating implied volatility: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate implied volatility", e);
        }
    }

    public BigDecimal calculateTimeToExpiry(LocalDate expirationDate) {
        LocalDate today = LocalDate.now();
        long daysToExpiry = ChronoUnit.DAYS.between(today, expirationDate);
        
        if (daysToExpiry <= 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(daysToExpiry / 365.0);
    }

    public OptionsGreeks calculatePortfolioGreeks(List<PositionGreeks> positions) {
        BigDecimal totalDelta = BigDecimal.ZERO;
        BigDecimal totalGamma = BigDecimal.ZERO;
        BigDecimal totalTheta = BigDecimal.ZERO;
        BigDecimal totalVega = BigDecimal.ZERO;
        BigDecimal totalRho = BigDecimal.ZERO;
        
        for (PositionGreeks position : positions) {
            BigDecimal quantity = position.getQuantity();
            OptionsGreeks greeks = position.getGreeks();
            
            totalDelta = totalDelta.add(greeks.getDelta().multiply(quantity));
            totalGamma = totalGamma.add(greeks.getGamma().multiply(quantity));
            totalTheta = totalTheta.add(greeks.getTheta().multiply(quantity));
            totalVega = totalVega.add(greeks.getVega().multiply(quantity));
            totalRho = totalRho.add(greeks.getRho().multiply(quantity));
        }
        
        return OptionsGreeks.builder()
                .symbol("PORTFOLIO")
                .delta(totalDelta)
                .gamma(totalGamma)
                .theta(totalTheta)
                .vega(totalVega)
                .rho(totalRho)
                .build();
    }

    public BigDecimal calculateDeltaHedgeRatio(OptionsGreeks optionGreeks, BigDecimal optionQuantity) {
        // For delta hedging, we need to buy/sell the underlying to offset the option delta
        // Hedge ratio = -(option delta * option quantity)
        return optionGreeks.getDelta().multiply(optionQuantity).negate();
    }

    public BigDecimal calculateGammaScaling(OptionsGreeks greeks, BigDecimal underlyingPriceMove) {
        // Gamma scaling estimates the change in delta for a given underlying price move
        return greeks.getGamma().multiply(underlyingPriceMove);
    }

    public BigDecimal calculateThetaDecay(OptionsGreeks greeks, int daysToDecay) {
        // Calculate the theoretical price decay over a given number of days
        return greeks.getTheta().multiply(BigDecimal.valueOf(daysToDecay));
    }

    // Inner class to represent a position with its Greeks
    public static class PositionGreeks {
        private String symbol;
        private BigDecimal quantity;
        private OptionsGreeks greeks;
        
        public PositionGreeks(String symbol, BigDecimal quantity, OptionsGreeks greeks) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.greeks = greeks;
        }
        
        public String getSymbol() { return symbol; }
        public BigDecimal getQuantity() { return quantity; }
        public OptionsGreeks getGreeks() { return greeks; }
    }
}