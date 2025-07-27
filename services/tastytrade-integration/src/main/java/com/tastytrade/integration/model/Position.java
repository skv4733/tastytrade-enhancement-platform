package com.tastytrade.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Position {
    
    @JsonProperty("account-number")
    private String accountNumber;
    
    private String symbol;
    
    @JsonProperty("instrument-type")
    private String instrumentType;
    
    private String underlying;
    
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @JsonProperty("quantity-direction")
    private String quantityDirection;
    
    @JsonProperty("close-price")
    private BigDecimal closePrice;
    
    @JsonProperty("average-open-price")
    private BigDecimal averageOpenPrice;
    
    @JsonProperty("market-value")
    private BigDecimal marketValue;
    
    @JsonProperty("unrealized-day-gain")
    private BigDecimal unrealizedDayGain;
    
    @JsonProperty("unrealized-day-gain-percent")
    private BigDecimal unrealizedDayGainPercent;
    
    @JsonProperty("unrealized-gain")
    private BigDecimal unrealizedGain;
    
    @JsonProperty("unrealized-gain-percent")
    private BigDecimal unrealizedGainPercent;
    
    private BigDecimal delta;
    private BigDecimal gamma;
    private BigDecimal theta;
    private BigDecimal vega;
    
    @JsonProperty("expiration-date")
    private LocalDate expirationDate;
    
    @JsonProperty("strike-price")
    private BigDecimal strikePrice;

    public Position() {}

    // Getters and setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getInstrumentType() { return instrumentType; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }

    public String getUnderlying() { return underlying; }
    public void setUnderlying(String underlying) { this.underlying = underlying; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getQuantityDirection() { return quantityDirection; }
    public void setQuantityDirection(String quantityDirection) { this.quantityDirection = quantityDirection; }

    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }

    public BigDecimal getAverageOpenPrice() { return averageOpenPrice; }
    public void setAverageOpenPrice(BigDecimal averageOpenPrice) { this.averageOpenPrice = averageOpenPrice; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public BigDecimal getUnrealizedDayGain() { return unrealizedDayGain; }
    public void setUnrealizedDayGain(BigDecimal unrealizedDayGain) { this.unrealizedDayGain = unrealizedDayGain; }

    public BigDecimal getUnrealizedDayGainPercent() { return unrealizedDayGainPercent; }
    public void setUnrealizedDayGainPercent(BigDecimal unrealizedDayGainPercent) { this.unrealizedDayGainPercent = unrealizedDayGainPercent; }

    public BigDecimal getUnrealizedGain() { return unrealizedGain; }
    public void setUnrealizedGain(BigDecimal unrealizedGain) { this.unrealizedGain = unrealizedGain; }

    public BigDecimal getUnrealizedGainPercent() { return unrealizedGainPercent; }
    public void setUnrealizedGainPercent(BigDecimal unrealizedGainPercent) { this.unrealizedGainPercent = unrealizedGainPercent; }

    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }

    public BigDecimal getGamma() { return gamma; }
    public void setGamma(BigDecimal gamma) { this.gamma = gamma; }

    public BigDecimal getTheta() { return theta; }
    public void setTheta(BigDecimal theta) { this.theta = theta; }

    public BigDecimal getVega() { return vega; }
    public void setVega(BigDecimal vega) { this.vega = vega; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }
}