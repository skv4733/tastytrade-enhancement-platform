package com.tastytrade.marketdata.model;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketDataEvent {
    
    private String symbol;
    private BigDecimal price;
    private BigDecimal underlyingPrice;
    private BigDecimal strikePrice;
    private BigDecimal timeToExpiry;
    private BigDecimal riskFreeRate;
    private BigDecimal impliedVolatility;
    private BigDecimal delta;
    private BigDecimal gamma;
    private BigDecimal theta;
    private BigDecimal vega;
    private BigDecimal rho;
    private Long volume;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Integer bidSize;
    private Integer askSize;
    private Integer openInterest;
    private String optionType;
    private Instant timestamp;
    private String eventType; // "QUOTE", "TRADE", "GREEKS_UPDATE"

    public MarketDataEvent() {
        this.timestamp = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MarketDataEvent event = new MarketDataEvent();

        public Builder symbol(String symbol) {
            event.symbol = symbol;
            return this;
        }

        public Builder price(BigDecimal price) {
            event.price = price;
            return this;
        }

        public Builder underlyingPrice(BigDecimal underlyingPrice) {
            event.underlyingPrice = underlyingPrice;
            return this;
        }

        public Builder strikePrice(BigDecimal strikePrice) {
            event.strikePrice = strikePrice;
            return this;
        }

        public Builder timeToExpiry(BigDecimal timeToExpiry) {
            event.timeToExpiry = timeToExpiry;
            return this;
        }

        public Builder riskFreeRate(BigDecimal riskFreeRate) {
            event.riskFreeRate = riskFreeRate;
            return this;
        }

        public Builder impliedVolatility(BigDecimal impliedVolatility) {
            event.impliedVolatility = impliedVolatility;
            return this;
        }

        public Builder delta(BigDecimal delta) {
            event.delta = delta;
            return this;
        }

        public Builder gamma(BigDecimal gamma) {
            event.gamma = gamma;
            return this;
        }

        public Builder theta(BigDecimal theta) {
            event.theta = theta;
            return this;
        }

        public Builder vega(BigDecimal vega) {
            event.vega = vega;
            return this;
        }

        public Builder rho(BigDecimal rho) {
            event.rho = rho;
            return this;
        }

        public Builder volume(Long volume) {
            event.volume = volume;
            return this;
        }

        public Builder bidPrice(BigDecimal bidPrice) {
            event.bidPrice = bidPrice;
            return this;
        }

        public Builder askPrice(BigDecimal askPrice) {
            event.askPrice = askPrice;
            return this;
        }

        public Builder bidSize(Integer bidSize) {
            event.bidSize = bidSize;
            return this;
        }

        public Builder askSize(Integer askSize) {
            event.askSize = askSize;
            return this;
        }

        public Builder openInterest(Integer openInterest) {
            event.openInterest = openInterest;
            return this;
        }

        public Builder optionType(String optionType) {
            event.optionType = optionType;
            return this;
        }

        public Builder eventType(String eventType) {
            event.eventType = eventType;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            event.timestamp = timestamp;
            return this;
        }

        public MarketDataEvent build() {
            return event;
        }
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(BigDecimal underlyingPrice) { this.underlyingPrice = underlyingPrice; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }

    public BigDecimal getTimeToExpiry() { return timeToExpiry; }
    public void setTimeToExpiry(BigDecimal timeToExpiry) { this.timeToExpiry = timeToExpiry; }

    public BigDecimal getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(BigDecimal riskFreeRate) { this.riskFreeRate = riskFreeRate; }

    public BigDecimal getImpliedVolatility() { return impliedVolatility; }
    public void setImpliedVolatility(BigDecimal impliedVolatility) { this.impliedVolatility = impliedVolatility; }

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

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }

    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }

    public Integer getBidSize() { return bidSize; }
    public void setBidSize(Integer bidSize) { this.bidSize = bidSize; }

    public Integer getAskSize() { return askSize; }
    public void setAskSize(Integer askSize) { this.askSize = askSize; }

    public Integer getOpenInterest() { return openInterest; }
    public void setOpenInterest(Integer openInterest) { this.openInterest = openInterest; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}