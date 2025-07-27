package com.tastytrade.shared.security.audit;

import java.math.BigDecimal;
import java.time.Instant;

public class AuditReport {
    
    private Instant reportTime;
    private int transactionCount;
    private BigDecimal totalAmount;
    private String integrityStatus;
    
    private AuditReport(Builder builder) {
        this.reportTime = builder.reportTime;
        this.transactionCount = builder.transactionCount;
        this.totalAmount = builder.totalAmount;
        this.integrityStatus = builder.integrityStatus;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Instant reportTime;
        private int transactionCount;
        private BigDecimal totalAmount;
        private String integrityStatus;
        
        public Builder reportTime(Instant reportTime) {
            this.reportTime = reportTime;
            return this;
        }
        
        public Builder transactionCount(int transactionCount) {
            this.transactionCount = transactionCount;
            return this;
        }
        
        public Builder totalAmount(Object totalAmount) {
            this.totalAmount = (BigDecimal) totalAmount;
            return this;
        }
        
        public Builder integrityStatus(String integrityStatus) {
            this.integrityStatus = integrityStatus;
            return this;
        }
        
        public AuditReport build() {
            return new AuditReport(this);
        }
    }
    
    // Getters
    public Instant getReportTime() { return reportTime; }
    public int getTransactionCount() { return transactionCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getIntegrityStatus() { return integrityStatus; }
    
    @Override
    public String toString() {
        return "AuditReport{" +
                "reportTime=" + reportTime +
                ", transactionCount=" + transactionCount +
                ", totalAmount=" + totalAmount +
                ", integrityStatus='" + integrityStatus + '\'' +
                '}';
    }
}