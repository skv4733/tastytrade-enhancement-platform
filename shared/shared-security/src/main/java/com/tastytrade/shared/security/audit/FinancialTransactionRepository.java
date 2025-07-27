package com.tastytrade.shared.security.audit;

import com.tastytrade.shared.models.transaction.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {
    List<FinancialTransaction> findByCreatedDateAfter(Instant createdDate);
    List<FinancialTransaction> findByAccountNumber(String accountNumber);
    List<FinancialTransaction> findByAccountNumberAndCreatedDateBetween(
        String accountNumber, Instant startDate, Instant endDate);
}