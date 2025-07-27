package com.tastytrade.shared.models.transaction;

import org.springframework.context.ApplicationEvent;

public class FinancialTransactionEvent extends ApplicationEvent {
    
    private final FinancialTransaction transaction;
    
    public FinancialTransactionEvent(Object source, FinancialTransaction transaction) {
        super(source);
        this.transaction = transaction;
    }
    
    public FinancialTransaction getTransaction() {
        return transaction;
    }
}