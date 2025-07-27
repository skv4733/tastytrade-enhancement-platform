package com.tastytrade.delta.model;

import java.math.BigDecimal;
import java.time.Instant;

public class DeltaThresholdEvent {
    
    private String symbol;
    private String accountNumber;
    private BigDecimal currentDelta;
    private BigDecimal threshold;
    private BigDecimal previousDelta;
    private String alertType;
    private Instant timestamp;
    private String userId;
    private BigDecimal portfolioDelta;
    private String triggeredBy;

    public DeltaThresholdEvent() {
        this.timestamp = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DeltaThresholdEvent event = new DeltaThresholdEvent();

        public Builder symbol(String symbol) {
            event.symbol = symbol;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            event.accountNumber = accountNumber;
            return this;
        }

        public Builder currentDelta(BigDecimal currentDelta) {
            event.currentDelta = currentDelta;
            return this;
        }

        public Builder threshold(BigDecimal threshold) {
            event.threshold = threshold;
            return this;
        }

        public Builder previousDelta(BigDecimal previousDelta) {
            event.previousDelta = previousDelta;
            return this;
        }

        public Builder alertType(String alertType) {
            event.alertType = alertType;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            event.timestamp = timestamp;
            return this;
        }

        public Builder userId(String userId) {
            event.userId = userId;
            return this;
        }

        public Builder portfolioDelta(BigDecimal portfolioDelta) {
            event.portfolioDelta = portfolioDelta;
            return this;
        }

        public Builder triggeredBy(String triggeredBy) {
            event.triggeredBy = triggeredBy;
            return this;
        }

        public DeltaThresholdEvent build() {
            return event;
        }
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getCurrentDelta() { return currentDelta; }
    public void setCurrentDelta(BigDecimal currentDelta) { this.currentDelta = currentDelta; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public BigDecimal getPreviousDelta() { return previousDelta; }
    public void setPreviousDelta(BigDecimal previousDelta) { this.previousDelta = previousDelta; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getPortfolioDelta() { return portfolioDelta; }
    public void setPortfolioDelta(BigDecimal portfolioDelta) { this.portfolioDelta = portfolioDelta; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
}