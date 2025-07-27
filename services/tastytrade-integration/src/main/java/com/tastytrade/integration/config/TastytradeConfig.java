package com.tastytrade.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestClient;

@Configuration
public class TastytradeConfig {
    
    @Value("${tastytrade.api.base-url}")
    private String baseUrl;
    
    @Value("${tastytrade.api.client-id}")
    private String clientId;
    
    @Value("${tastytrade.api.client-secret}")
    private String clientSecret;
    
    @Value("${tastytrade.api.redirect-uri}")
    private String redirectUri;

    @Bean
    public RestClient tastytradeRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(tastytradeClientRegistration());
    }

    private ClientRegistration tastytradeClientRegistration() {
        return ClientRegistration.withRegistrationId("tastytrade")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .authorizationUri(baseUrl + "/oauth/authorize")
                .tokenUri(baseUrl + "/oauth/token")
                .userInfoUri(baseUrl + "/api/accounts")
                .userNameAttributeName("email")
                .clientName("Tastytrade")
                .build();
    }
}