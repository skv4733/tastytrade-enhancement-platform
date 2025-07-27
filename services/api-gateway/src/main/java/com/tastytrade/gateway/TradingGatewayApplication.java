package com.tastytrade.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TradingGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("tastytrade-integration", r -> r.path("/api/tastytrade/**")
                        .uri("http://localhost:8081"))
                .route("market-data", r -> r.path("/api/market-data/**")
                        .uri("http://localhost:8082"))
                .route("portfolio", r -> r.path("/api/portfolio/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}