package com.tastytrade.delta.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AlertMessage {
    
    private UUID id;
    private AlertType type;
    private String symbol;
    private String accountNumber;
    private BigDecimal currentValue;
    private BigDecimal threshold;
    private String message;
    private String userId;
    private String phoneNumber;
    private String email;
    private String fcmToken;
    private Instant timestamp;
    private AlertPriority priority;
    private boolean acknowledged;

    public AlertMessage() {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.acknowledged = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertMessage alert = new AlertMessage();

        public Builder type(AlertType type) {
            alert.type = type;
            return this;
        }

        public Builder symbol(String symbol) {
            alert.symbol = symbol;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            alert.accountNumber = accountNumber;
            return this;
        }

        public Builder currentValue(BigDecimal currentValue) {
            alert.currentValue = currentValue;
            return this;
        }

        public Builder threshold(BigDecimal threshold) {
            alert.threshold = threshold;
            return this;
        }

        public Builder message(String message) {
            alert.message = message;
            return this;
        }

        public Builder userId(String userId) {
            alert.userId = userId;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            alert.phoneNumber = phoneNumber;
            return this;
        }

        public Builder email(String email) {
            alert.email = email;
            return this;
        }

        public Builder fcmToken(String fcmToken) {
            alert.fcmToken = fcmToken;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            alert.timestamp = timestamp;
            return this;
        }

        public Builder priority(AlertPriority priority) {
            alert.priority = priority;
            return this;
        }

        public AlertMessage build() {
            return alert;
        }
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public AlertPriority getPriority() { return priority; }
    public void setPriority(AlertPriority priority) { this.priority = priority; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}