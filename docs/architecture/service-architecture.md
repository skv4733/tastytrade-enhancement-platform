# Service Architecture Diagram

## High-Level Architecture

```mermaid
graph TB
    subgraph "Frontend"
        UI[React Frontend]
    end
    
    subgraph "API Layer"
        GW[API Gateway<br/>:8080]
    end
    
    subgraph "Authentication"
        USER[User Service<br/>:8084]
    end
    
    subgraph "Core Services"
        TT[Tastytrade Integration<br/>:8081]
        MD[Market Data Service<br/>:8082]
        OC[Options Calculator<br/>:8085]
        DM[Delta Monitor<br/>:8086]
        NS[Notification Service<br/>:8087]
        PS[Portfolio Service<br/>:8083]
        RS[Reporting Service<br/>:8088]
    end
    
    subgraph "Infrastructure"
        DB[(TimescaleDB)]
        REDIS[(Redis)]
        MQ[RabbitMQ]
    end
    
    subgraph "External"
        TTAPI[Tastytrade API]
        SMS[Twilio SMS]
        EMAIL[Email Service]
        FCM[Firebase FCM]
    end
    
    UI --> GW
    GW --> USER
    GW --> TT
    GW --> MD
    GW --> OC
    GW --> DM
    GW --> NS
    GW --> PS
    GW --> RS
    
    TT --> TTAPI
    MD --> DB
    MD --> REDIS
    DM --> MQ
    NS --> MQ
    NS --> SMS
    NS --> EMAIL
    NS --> FCM
    PS --> DB
    RS --> DB
    
    MD -.->|Events| DM
    DM -.->|Alerts| NS
```

## Service Communication Patterns

### Synchronous Communication
- Frontend ↔ API Gateway (HTTP/REST)
- API Gateway ↔ Services (HTTP/REST)
- Services ↔ External APIs (HTTP/REST)

### Asynchronous Communication
- Market Data → Delta Monitor (Events)
- Delta Monitor → Notification Service (Alerts)
- Portfolio Service → Audit Service (Transactions)

### Real-time Communication
- Market Data Service → Frontend (WebSocket/SSE)
- Notification Service → Frontend (WebSocket)

## Data Storage Strategy

### TimescaleDB (Primary Database)
- Market data time-series
- Transaction history
- User accounts and positions
- Audit logs

### Redis (Cache & Sessions)
- Session management
- Real-time quotes cache
- Delta thresholds
- Rate limiting counters

### RabbitMQ (Message Broker)
- Market data events
- Alert notifications
- Audit events
- System notifications

## Security Layers

```mermaid
graph LR
    subgraph "Network Security"
        NGINX[NGINX Ingress<br/>SSL/TLS]
    end
    
    subgraph "Application Security"
        JWT[JWT Authentication]
        RBAC[Role-Based Access]
        AUDIT[Audit Logging]
    end
    
    subgraph "Data Security"
        ENCRYPT[Data Encryption]
        SIGN[Digital Signatures]
        HASH[Password Hashing]
    end
    
    NGINX --> JWT
    JWT --> RBAC
    RBAC --> AUDIT
    AUDIT --> ENCRYPT
    ENCRYPT --> SIGN
    SIGN --> HASH
```