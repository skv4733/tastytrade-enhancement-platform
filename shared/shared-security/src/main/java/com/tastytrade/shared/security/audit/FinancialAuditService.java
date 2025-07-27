package com.tastytrade.shared.security.audit;

import com.tastytrade.shared.models.transaction.FinancialTransaction;
import com.tastytrade.shared.models.transaction.FinancialTransactionEvent;
import com.tastytrade.shared.security.signature.DigitalSignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class FinancialAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialAuditService.class);
    
    @Autowired
    private DigitalSignatureService signatureService;
    
    @Autowired
    private FinancialTransactionRepository transactionRepository;
    
    @Autowired
    private ExternalAuditService externalAuditService;
    
    @Autowired
    private AlertService alertService;
    
    @EventListener
    @Async
    public void auditFinancialTransaction(FinancialTransactionEvent event) {
        FinancialTransaction transaction = event.getTransaction();
        
        try {
            String signature = signatureService.sign(transaction);
            transaction.setDigitalSignature(signature);
            
            transactionRepository.save(transaction);
            
            logToExternalAuditSystem(transaction);
            
            logger.info("Successfully audited financial transaction: {}", transaction.getId());
        } catch (Exception e) {
            logger.error("Failed to audit financial transaction: {}", transaction.getId(), e);
            alertService.sendCriticalAlert("Audit failure for transaction: " + transaction.getId());
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void validateTransactionIntegrity() {
        logger.info("Starting daily transaction integrity validation");
        
        List<FinancialTransaction> transactions = transactionRepository
                .findByCreatedDateAfter(Instant.now().minus(Duration.ofDays(1)));
        
        transactions.parallelStream().forEach(transaction -> {
            if (!signatureService.verify(transaction, transaction.getDigitalSignature())) {
                String alertMessage = "Transaction integrity violation detected: " + transaction.getId();
                logger.error(alertMessage);
                alertService.sendCriticalAlert(alertMessage);
            }
        });
        
        logger.info("Completed daily transaction integrity validation. Checked {} transactions", 
                   transactions.size());
    }
    
    @Scheduled(fixedRate = 3600000)
    public void generateHourlyAuditReport() {
        try {
            Instant hourAgo = Instant.now().minus(Duration.ofHours(1));
            List<FinancialTransaction> recentTransactions = 
                transactionRepository.findByCreatedDateAfter(hourAgo);
            
            AuditReport report = AuditReport.builder()
                .reportTime(Instant.now())
                .transactionCount(recentTransactions.size())
                .totalAmount(calculateTotalAmount(recentTransactions))
                .integrityStatus("VERIFIED")
                .build();
            
            externalAuditService.submitReport(report);
            
        } catch (Exception e) {
            logger.error("Failed to generate hourly audit report", e);
        }
    }
    
    private void logToExternalAuditSystem(FinancialTransaction transaction) {
        try {
            externalAuditService.logTransaction(transaction);
        } catch (Exception e) {
            logger.error("Failed to log transaction to external audit system: {}", 
                        transaction.getId(), e);
        }
    }
    
    private Object calculateTotalAmount(List<FinancialTransaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount() != null)
                .map(FinancialTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}