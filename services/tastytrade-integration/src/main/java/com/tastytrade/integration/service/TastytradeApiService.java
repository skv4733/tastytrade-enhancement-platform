package com.tastytrade.integration.service;

import com.tastytrade.integration.exception.TastytradeApiException;
import com.tastytrade.integration.model.AccountBalance;
import com.tastytrade.integration.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TastytradeApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(TastytradeApiService.class);
    
    private final RestClient restClient;
    private final OAuth2AuthorizedClientService authorizedClientService;
    
    @Value("${tastytrade.api.base-url}")
    private String baseUrl;

    @Autowired
    public TastytradeApiService(RestClient tastytradeRestClient,
                               OAuth2AuthorizedClientService authorizedClientService) {
        this.restClient = tastytradeRestClient;
        this.authorizedClientService = authorizedClientService;
    }

    public AccountBalance getAccountBalance(String accountNumber) {
        try {
            return restClient.get()
                    .uri("/accounts/{accountNumber}/balances", accountNumber)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, 
                            (request, response) -> {
                                throw new TastytradeApiException("Server error occurred while fetching account balance");
                            })
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                throw new TastytradeApiException("Client error occurred while fetching account balance");
                            })
                    .body(AccountBalance.class);
        } catch (Exception e) {
            logger.error("Error fetching account balance for account: {}", accountNumber, e);
            throw new TastytradeApiException("Failed to fetch account balance", e);
        }
    }

    public List<Position> getPositions(String accountNumber) {
        try {
            return restClient.get()
                    .uri("/accounts/{accountNumber}/positions", accountNumber)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new TastytradeApiException("Server error occurred while fetching positions");
                            })
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                throw new TastytradeApiException("Client error occurred while fetching positions");
                            })
                    .body(new ParameterizedTypeReference<List<Position>>() {});
        } catch (Exception e) {
            logger.error("Error fetching positions for account: {}", accountNumber, e);
            throw new TastytradeApiException("Failed to fetch positions", e);
        }
    }

    public List<Position> getOptionsPositions(String accountNumber) {
        try {
            return restClient.get()
                    .uri("/accounts/{accountNumber}/positions?instrument-type=Equity Option", accountNumber)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new TastytradeApiException("Server error occurred while fetching options positions");
                            })
                    .body(new ParameterizedTypeReference<List<Position>>() {});
        } catch (Exception e) {
            logger.error("Error fetching options positions for account: {}", accountNumber, e);
            throw new TastytradeApiException("Failed to fetch options positions", e);
        }
    }

    private String getAccessToken() {
        OAuth2AuthorizedClient authorizedClient = 
                authorizedClientService.loadAuthorizedClient("tastytrade", "system");
        
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new TastytradeApiException("No valid OAuth2 token available");
        }
        
        return authorizedClient.getAccessToken().getTokenValue();
    }

    public boolean isTokenExpired() {
        OAuth2AuthorizedClient authorizedClient = 
                authorizedClientService.loadAuthorizedClient("tastytrade", "system");
        
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return true;
        }
        
        return authorizedClient.getAccessToken().getExpiresAt() != null &&
               authorizedClient.getAccessToken().getExpiresAt().isBefore(java.time.Instant.now());
    }
}