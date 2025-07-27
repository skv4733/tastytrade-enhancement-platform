package com.tastytrade.integration.client;

import com.tastytrade.integration.model.AccountBalance;
import com.tastytrade.integration.model.Position;
import com.tastytrade.integration.service.TastytradeApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class TastytradeClient {
    
    @Autowired
    private TastytradeApiService tastytradeApiService;

    public CompletableFuture<AccountBalance> getAccountBalanceAsync(String accountNumber) {
        return CompletableFuture.supplyAsync(() -> 
                tastytradeApiService.getAccountBalance(accountNumber));
    }

    public CompletableFuture<List<Position>> getPositionsAsync(String accountNumber) {
        return CompletableFuture.supplyAsync(() -> 
                tastytradeApiService.getPositions(accountNumber));
    }

    public CompletableFuture<List<Position>> getOptionsPositionsAsync(String accountNumber) {
        return CompletableFuture.supplyAsync(() -> 
                tastytradeApiService.getOptionsPositions(accountNumber));
    }

    public AccountBalance getAccountBalance(String accountNumber) {
        return tastytradeApiService.getAccountBalance(accountNumber);
    }

    public List<Position> getPositions(String accountNumber) {
        return tastytradeApiService.getPositions(accountNumber);
    }

    public List<Position> getOptionsPositions(String accountNumber) {
        return tastytradeApiService.getOptionsPositions(accountNumber);
    }

    public boolean isApiConnected() {
        return !tastytradeApiService.isTokenExpired();
    }
}