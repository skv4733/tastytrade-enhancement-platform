package com.tastytrade.calculator.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BlackScholesParameters {
    
    @NotNull
    @Positive
    private BigDecimal underlyingPrice;
    
    @NotNull
    @Positive
    private BigDecimal strikePrice;
    
    @NotNull
    @Positive
    private BigDecimal timeToExpiry; // in years
    
    @NotNull
    private BigDecimal riskFreeRate;
    
    @NotNull
    @Positive
    private BigDecimal volatility;
    
    @NotNull
    private String optionType; // "CALL" or "PUT"
    
    private BigDecimal dividendYield = BigDecimal.ZERO;

    public BlackScholesParameters() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BlackScholesParameters params = new BlackScholesParameters();

        public Builder underlyingPrice(BigDecimal underlyingPrice) {
            params.underlyingPrice = underlyingPrice;
            return this;
        }

        public Builder strikePrice(BigDecimal strikePrice) {
            params.strikePrice = strikePrice;
            return this;
        }

        public Builder timeToExpiry(BigDecimal timeToExpiry) {
            params.timeToExpiry = timeToExpiry;
            return this;
        }

        public Builder riskFreeRate(BigDecimal riskFreeRate) {
            params.riskFreeRate = riskFreeRate;
            return this;
        }

        public Builder volatility(BigDecimal volatility) {
            params.volatility = volatility;
            return this;
        }

        public Builder optionType(String optionType) {
            params.optionType = optionType;
            return this;
        }

        public Builder dividendYield(BigDecimal dividendYield) {
            params.dividendYield = dividendYield;
            return this;
        }

        public BlackScholesParameters build() {
            return params;
        }
    }

    // Getters and setters
    public BigDecimal getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(BigDecimal underlyingPrice) { this.underlyingPrice = underlyingPrice; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }

    public BigDecimal getTimeToExpiry() { return timeToExpiry; }
    public void setTimeToExpiry(BigDecimal timeToExpiry) { this.timeToExpiry = timeToExpiry; }

    public BigDecimal getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(BigDecimal riskFreeRate) { this.riskFreeRate = riskFreeRate; }

    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
}