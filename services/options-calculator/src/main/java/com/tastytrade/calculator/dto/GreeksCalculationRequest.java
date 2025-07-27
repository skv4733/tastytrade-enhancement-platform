package com.tastytrade.calculator.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GreeksCalculationRequest {
    
    @NotNull
    private String symbol;
    
    @NotNull
    @Positive
    private BigDecimal underlyingPrice;
    
    @NotNull
    @Positive
    private BigDecimal strikePrice;
    
    @NotNull
    private LocalDate expirationDate;
    
    @NotNull
    private String optionType; // "CALL" or "PUT"
    
    @NotNull
    @Positive
    private BigDecimal volatility;
    
    private BigDecimal riskFreeRate = BigDecimal.valueOf(0.05); // Default 5%
    private BigDecimal dividendYield = BigDecimal.ZERO;

    public GreeksCalculationRequest() {}

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getUnderlyingPrice() { return underlyingPrice; }
    public void setUnderlyingPrice(BigDecimal underlyingPrice) { this.underlyingPrice = underlyingPrice; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public BigDecimal getVolatility() { return volatility; }
    public void setVolatility(BigDecimal volatility) { this.volatility = volatility; }

    public BigDecimal getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(BigDecimal riskFreeRate) { this.riskFreeRate = riskFreeRate; }

    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
}