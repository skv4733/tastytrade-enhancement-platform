package com.tastytrade.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class AccountBalance {
    
    @JsonProperty("account-number")
    private String accountNumber;
    
    @JsonProperty("cash-balance")
    private BigDecimal cashBalance;
    
    @JsonProperty("buying-power")
    private BigDecimal buyingPower;
    
    @JsonProperty("day-trade-buying-power")
    private BigDecimal dayTradeBuyingPower;
    
    @JsonProperty("net-liquidating-value")
    private BigDecimal netLiquidatingValue;
    
    @JsonProperty("equity-buying-power")
    private BigDecimal equityBuyingPower;
    
    @JsonProperty("derivative-buying-power")
    private BigDecimal derivativeBuyingPower;

    public AccountBalance() {}

    // Getters and setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }

    public BigDecimal getBuyingPower() { return buyingPower; }
    public void setBuyingPower(BigDecimal buyingPower) { this.buyingPower = buyingPower; }

    public BigDecimal getDayTradeBuyingPower() { return dayTradeBuyingPower; }
    public void setDayTradeBuyingPower(BigDecimal dayTradeBuyingPower) { this.dayTradeBuyingPower = dayTradeBuyingPower; }

    public BigDecimal getNetLiquidatingValue() { return netLiquidatingValue; }
    public void setNetLiquidatingValue(BigDecimal netLiquidatingValue) { this.netLiquidatingValue = netLiquidatingValue; }

    public BigDecimal getEquityBuyingPower() { return equityBuyingPower; }
    public void setEquityBuyingPower(BigDecimal equityBuyingPower) { this.equityBuyingPower = equityBuyingPower; }

    public BigDecimal getDerivativeBuyingPower() { return derivativeBuyingPower; }
    public void setDerivativeBuyingPower(BigDecimal derivativeBuyingPower) { this.derivativeBuyingPower = derivativeBuyingPower; }
}