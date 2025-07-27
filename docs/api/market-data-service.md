# Market Data Service Documentation

## Overview

The Market Data Service provides real-time streaming of options market data using reactive programming with Spring WebFlux. It supports WebSocket, Server-Sent Events (SSE), and REST endpoints for comprehensive market data access.

## Service Details

- **Port**: 8082
- **Technology**: Spring WebFlux, WebSocket, TimescaleDB
- **Real-time**: Sub-100ms latency streaming

## Features

### Real-time Streaming
- Server-Sent Events (SSE) for quote streams
- WebSocket connections for bidirectional communication
- Reactive backpressure handling
- Connection pooling and management

### Data Management
- Historical quote storage in TimescaleDB
- Redis caching for latest quotes
- Data compression and retention policies
- Time-series optimization

### Market Data Processing
- Options quotes with Greeks
- Underlying price tracking
- Volume and open interest data
- Implied volatility calculations

## API Endpoints

### Streaming Endpoints

#### Real-time Quote Stream (SSE)
```http
GET /api/market-data/stream/{symbol}
Accept: text/event-stream
```

**Response Stream:**
```
event: quote-update
id: 1640995200000
data: {
  "symbol": "AAPL240119C00150000",
  "price": 5.75,
  "underlyingPrice": 152.50,
  "strikePrice": 150.00,
  "bidPrice": 5.70,
  "askPrice": 5.80,
  "delta": 0.65,
  "gamma": 0.025,
  "theta": -0.05,
  "vega": 0.15,
  "impliedVolatility": 0.25,
  "timestamp": "2024-01-01T12:00:00Z"
}
```

#### WebSocket Connection
```javascript
const socket = new SockJS('/ws/market-data');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/quotes/AAPL240119C00150000', function(message) {
        const quote = JSON.parse(message.body);
        updateUI(quote);
    });
});
```

### REST Endpoints

#### Get Latest Quote
```http
GET /api/market-data/latest/{symbol}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "symbol": "AAPL240119C00150000",
  "price": 5.75,
  "underlyingPrice": 152.50,
  "strikePrice": 150.00,
  "bidPrice": 5.70,
  "askPrice": 5.80,
  "bidSize": 25,
  "askSize": 30,
  "volume": 1250,
  "openInterest": 5000,
  "impliedVolatility": 0.25,
  "delta": 0.65,
  "gamma": 0.025,
  "theta": -0.05,
  "vega": 0.15,
  "rho": 0.08,
  "optionType": "CALL",
  "quoteTime": "2024-01-01T12:00:00Z"
}
```

#### Get Historical Quotes
```http
GET /api/market-data/historical/{symbol}?fromTime=2024-01-01T00:00:00Z&toTime=2024-01-01T23:59:59Z
```

#### Publish Quote (Internal)
```http
POST /api/market-data/quotes
Content-Type: application/json

{
  "symbol": "AAPL240119C00150000",
  "price": 5.75,
  "underlyingPrice": 152.50,
  "delta": 0.65,
  "gamma": 0.025
}
```

## Data Models

### OptionsQuote Entity
```java
@Entity
@Table(name = "options_quotes")
public class OptionsQuote {
    @Id
    private UUID id;
    private String symbol;
    private BigDecimal price;
    private BigDecimal underlyingPrice;
    private BigDecimal strikePrice;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Integer bidSize;
    private Integer askSize;
    private Long volume;
    private Integer openInterest;
    private BigDecimal impliedVolatility;
    private BigDecimal delta;
    private BigDecimal gamma;
    private BigDecimal theta;
    private BigDecimal vega;
    private BigDecimal rho;
    private String optionType;
    private Instant quoteTime;
}
```

### MarketDataEvent
```java
public class MarketDataEvent {
    private String symbol;
    private BigDecimal price;
    private BigDecimal delta;
    private String eventType; // "QUOTE", "TRADE", "GREEKS_UPDATE"
    private Instant timestamp;
}
```

## Performance Characteristics

### Streaming Performance
- **Latency**: < 100ms end-to-end
- **Throughput**: 10,000+ quotes/second
- **Concurrent Connections**: 1,000+ WebSocket connections
- **Backpressure**: Automatic handling with latest value strategy

### Database Performance
- **Time-series Optimization**: TimescaleDB hypertables
- **Compression**: 7-day compression policy
- **Retention**: 2-year data retention
- **Indexing**: Symbol and time-based indexes

### Caching Strategy
- **Redis TTL**: 5 minutes for latest quotes
- **Cache Hit Ratio**: > 95% for latest data
- **Memory Usage**: < 500MB Redis cache
- **Eviction Policy**: LRU (Least Recently Used)

## Monitoring and Metrics

### Business Metrics
- **Active Subscriptions**: Real-time subscriber count
- **Quote Volume**: Quotes processed per second
- **Latency Percentiles**: P50, P95, P99 response times
- **Error Rates**: Failed quote processing rate

### System Metrics
- **Memory Usage**: Heap and off-heap memory
- **CPU Utilization**: Service CPU usage
- **Database Connections**: Active/idle connections
- **Cache Performance**: Hit/miss ratios

### Health Checks
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

## Configuration

### Application Configuration
```yaml
spring:
  webflux:
    netty:
      connection-timeout: 30s
      max-connections: 1000
  data:
    redis:
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 50
```

### Environment Variables
```yaml
TIMESCALEDB_URL: jdbc:postgresql://timescaledb:5432/tastytrade
REDIS_URL: redis://redis:6379/2
RABBITMQ_URL: amqp://rabbitmq:5672
MAX_SUBSCRIBERS_PER_SYMBOL: 100
QUOTE_BATCH_SIZE: 1000
```

## Deployment

### Resource Requirements
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

### Scaling Strategy
- **Horizontal Scaling**: Stateless service design
- **Load Balancing**: Sticky sessions for WebSocket
- **Auto-scaling**: CPU and memory-based
- **Database Sharding**: By symbol or time range