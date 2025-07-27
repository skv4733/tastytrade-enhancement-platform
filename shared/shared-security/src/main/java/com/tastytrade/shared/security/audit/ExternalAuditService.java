package com.tastytrade.shared.security.audit;

import com.tastytrade.shared.models.transaction.FinancialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExternalAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuditService.class);
    
    public void logTransaction(FinancialTransaction transaction) {
        logger.info("External audit log - Transaction: {} | Account: {} | Amount: {} | Time: {}", 
                   transaction.getId(), 
                   transaction.getAccountNumber(),
                   transaction.getAmount(),
                   transaction.getCreatedDate());
    }
    
    public void submitReport(AuditReport report) {
        logger.info("External audit report submitted: {}", report);
    }
}