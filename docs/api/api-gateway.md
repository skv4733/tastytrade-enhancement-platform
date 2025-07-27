# API Gateway Service Documentation

## Overview

The API Gateway serves as the single entry point for all client requests. Built with Spring Cloud Gateway, it provides routing, load balancing, authentication, and cross-cutting concerns.

## Service Details

- **Port**: 8080
- **Technology**: Spring Cloud Gateway
- **Dependencies**: Spring Boot 3.2, Spring Cloud 2023.0.0

## Features

### Routing Configuration
- Automatic service discovery via Eureka
- Path-based routing to microservices
- Load balancing with round-robin strategy

### Security Integration
- JWT token validation
- OAuth2 resource server configuration
- CORS handling for web clients

### Resilience Patterns
- Circuit breaker integration with Resilience4j
- Rate limiting per client
- Request/response transformation

### Monitoring
- Health check endpoints
- Metrics collection with Micrometer
- Distributed tracing support

## Route Configuration

### Authentication Routes
```yaml
/api/auth/** → user-service:8084
```

### Core Service Routes
```yaml
/api/tastytrade/** → tastytrade-integration:8081
/api/market-data/** → market-data-service:8082
/api/options/** → options-calculator:8085
/api/delta/** → delta-monitor:8086
/api/notifications/** → notification-service:8087
/api/portfolio/** → portfolio-service:8083
/api/reports/** → reporting-service:8088
```

## Circuit Breaker Configuration

### Market Data Circuit Breaker
- **Failure Threshold**: 50%
- **Wait Duration**: 30 seconds
- **Permitted Calls**: 10
- **Fallback**: Cached data or error response

### Portfolio Circuit Breaker
- **Failure Threshold**: 60%
- **Wait Duration**: 60 seconds
- **Rate Limiter**: 100 requests/minute

## Rate Limiting

### Default Limits
- **Burst Capacity**: 200 requests
- **Replenish Rate**: 100 requests/second
- **Requested Tokens**: 1

### VIP Tier Limits
- **Burst Capacity**: 500 requests
- **Replenish Rate**: 200 requests/second

## Health Checks

### Gateway Health
```bash
GET /actuator/health
```

### Service Dependencies
- TimescaleDB connection
- Redis connection
- RabbitMQ connection
- Eureka registry

## Configuration

### Environment Variables
```yaml
EUREKA_CLIENT_SERVICE_URL: http://eureka-server:8761/eureka
REDIS_HOST: redis-service
REDIS_PORT: 6379
JWT_SECRET: ${JWT_SECRET}
```

### Resource Limits
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

## Error Handling

### Standard Error Responses
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Service temporarily unavailable",
  "path": "/api/market-data/stream/AAPL"
}
```

### Circuit Breaker Fallback
```json
{
  "error": "Service temporarily unavailable",
  "fallback": true,
  "retryAfter": 30
}
```

## Deployment

### Docker Configuration
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Deployment
- **Replicas**: 3
- **Rolling Update Strategy**: 25% max unavailable
- **Readiness Probe**: /actuator/health
- **Liveness Probe**: /actuator/health