package com.tastytrade.marketdata.repository;

import com.tastytrade.marketdata.model.OptionsQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OptionsQuoteRepository extends JpaRepository<OptionsQuote, UUID> {
    
    @Query("SELECT q FROM OptionsQuote q WHERE q.symbol = :symbol ORDER BY q.quoteTime DESC LIMIT 1")
    OptionsQuote findLatestBySymbol(@Param("symbol") String symbol);
    
    List<OptionsQuote> findBySymbolAndQuoteTimeBetween(String symbol, Instant fromTime, Instant toTime);
    
    @Query("SELECT q FROM OptionsQuote q WHERE q.symbol = :symbol AND q.quoteTime >= :fromTime ORDER BY q.quoteTime DESC")
    List<OptionsQuote> findRecentBySymbol(@Param("symbol") String symbol, @Param("fromTime") Instant fromTime);
    
    @Query("SELECT DISTINCT q.symbol FROM OptionsQuote q WHERE q.quoteTime >= :fromTime")
    List<String> findActiveSymbols(@Param("fromTime") Instant fromTime);
    
    @Query("SELECT COUNT(q) FROM OptionsQuote q WHERE q.symbol = :symbol AND q.quoteTime >= :fromTime")
    Long countBySymbolSince(@Param("symbol") String symbol, @Param("fromTime") Instant fromTime);
}