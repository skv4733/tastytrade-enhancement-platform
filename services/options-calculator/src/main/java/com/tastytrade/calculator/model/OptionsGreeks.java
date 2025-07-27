package com.tastytrade.calculator.model;

import java.math.BigDecimal;
import java.time.Instant;

public class OptionsGreeks {
    
    private String symbol;
    private BigDecimal delta;
    private BigDecimal gamma;
    private BigDecimal theta;
    private BigDecimal vega;
    private BigDecimal rho;
    private BigDecimal optionPrice;
    private BigDecimal underlyingPrice;
    private BigDecimal strikePrice;
    private BigDecimal volatility;
    private BigDecimal timeToExpiry;
    private BigDecimal riskFreeRate;
    private String optionType; // "CALL" or "PUT"
    private Instant calculationTime;

    public OptionsGreeks() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OptionsGreeks greeks = new OptionsGreeks();

        public Builder symbol(String symbol) {
            greeks.symbol = symbol;
            return this;
        }

        public Builder delta(BigDecimal delta) {
            greeks.delta = delta;
            return this;
        }

        public Builder gamma(BigDecimal gamma) {
            greeks.gamma = gamma;
            return this;
        }

        public Builder theta(BigDecimal theta) {
            greeks.theta = theta;
            return this;
        }

        public Builder vega(BigDecimal vega) {
            greeks.vega = vega;
            return this;
        }

        public Builder rho(BigDecimal rho) {
            greeks.rho = rho;
            return this;
        }

        public Builder optionPrice(BigDecimal optionPrice) {
            greeks.optionPrice = optionPrice;
            return this;
        }

        public Builder underlyingPrice(BigDecimal underlyingPrice) {
            greeks.underlyingPrice = underlyingPrice;
            return this;
        }

        public Builder strikePrice(BigDecimal strikePrice) {
            greeks.strikePrice = strikePrice;
            return this;
        }

        public Builder volatility(BigDecimal volatility) {
            greeks.volatility = volatility;
            return this;
        }

        public Builder timeToExpiry(BigDecimal timeToExpiry) {
            greeks.timeToExpiry = timeToExpiry;
            return this;
        }

        public Builder riskFreeRate(BigDecimal riskFreeRate) {
            greeks.riskFreeRate = riskFreeRate;
            return this;
        }

        public Builder optionType(String optionType) {
            greeks.optionType = optionType;
            return this;
        }

        public Builder calculationTime(Instant calculationTime) {
            greeks.calculationTime = calculationTime;
            return this;
        }

        public OptionsGreeks build() {
            greeks.calculationTime = Instant.now();
            return greeks;
        }
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }

    public BigDecimal getGamma() { return gamma; }
    public void setGamma(BigDecimal gamma) { this.gamma = gamma; }

    public BigDecimal getTheta() { return theta; }
    public void setTheta(BigDecimal theta) { this.theta = theta; }

    public BigDecimal getVega() { return vega; }
    public void setVega(BigDecimal vega) { this.vega = vega; }

    public BigDecimal getRho() { return rho; }
    public void setRho(BigDecimal rho) { this.rho = rho; }

    public BigDecimal getOptionPrice() { return optionPrice; }
    public void setOptionPrice(BigDecimal optionPrice) { this.optionPrice = optionPrice; }

    public BigDecimal getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(BigDecimal underlyingPrice) { this.underlyingPrice = underlyingPrice; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }

    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }

    public BigDecimal getTimeToExpiry() { return timeToExpiry; }
    public void setTimeToExpiry(BigDecimal timeToExpiry) { this.timeToExpiry = timeToExpiry; }

    public BigDecimal getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(BigDecimal riskFreeRate) { this.riskFreeRate = riskFreeRate; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public Instant getCalculationTime() { return calculationTime; }
    public void setCalculationTime(Instant calculationTime) { this.calculationTime = calculationTime; }
}