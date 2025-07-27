# Docker Deployment Guide

## Overview

This guide provides instructions for running the Tastytrade Enhancement Platform using Docker and Docker Compose for development and testing environments.

## Prerequisites

- **Docker**: v24.0+
- **Docker Compose**: v2.20+
- **Memory**: 8GB+ available RAM
- **Storage**: 20GB+ available disk space

## Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd tastytrade-enhancement-platform
```

### 2. Build Services
```bash
# Build all services
./scripts/build.sh

# Or build individual service
mvn -pl services/api-gateway clean package -DskipTests
```

### 3. Start Infrastructure
```bash
# Start infrastructure services only
docker-compose up -d timescaledb redis rabbitmq

# Wait for services to be ready
docker-compose logs -f timescaledb
```

### 4. Start All Services
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### 5. Verify Deployment
```bash
# Check service health
curl http://localhost:8080/actuator/health

# Check all running containers
docker-compose ps
```

## Service Configuration

### Docker Compose Services

#### Infrastructure Services
- **TimescaleDB**: PostgreSQL with TimescaleDB extension
- **Redis**: Cache and session storage
- **RabbitMQ**: Message broker with management UI

#### Application Services
- **API Gateway**: Port 8080
- **User Service**: Port 8084
- **Market Data Service**: Port 8082
- **Options Calculator**: Port 8085
- **Delta Monitor**: Port 8086
- **Notification Service**: Port 8087
- **Portfolio Service**: Port 8083
- **Reporting Service**: Port 8088
- **Tastytrade Integration**: Port 8081

### Environment Variables

#### Database Configuration
```yaml
POSTGRES_DB: tastytrade
POSTGRES_USER: tastytrade
POSTGRES_PASSWORD: password
```

#### Application Configuration
```yaml
SPRING_PROFILES_ACTIVE: local
REDIS_HOST: redis
RABBITMQ_HOST: rabbitmq
```

## Development Setup

### Building Services

#### Build All Services
```bash
# Using provided script
./scripts/build.sh

# Manual Maven build
mvn clean package -DskipTests
```

#### Build Individual Service
```bash
# Build specific service
mvn -pl services/market-data-service clean package -DskipTests

# Build with tests
mvn -pl services/options-calculator clean package
```

### Docker Images

#### Build Custom Images
```bash
# Build API Gateway
docker build -t tastytrade/api-gateway:latest services/api-gateway/

# Build all services
for service in api-gateway user-service market-data-service options-calculator delta-monitor; do
  docker build -t tastytrade/$service:latest services/$service/
done
```

#### Use Pre-built Images
Update `docker-compose.yml` to use pre-built images:
```yaml
services:
  api-gateway:
    image: tastytrade/api-gateway:latest
    # Remove build directive
```

### Development Workflow

#### Hot Reloading
For development with hot reloading:
```bash
# Run infrastructure only
docker-compose up -d timescaledb redis rabbitmq

# Run specific service locally
cd services/market-data-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Debugging
```bash
# Debug specific service
docker-compose up -d timescaledb redis rabbitmq
mvn -pl services/api-gateway spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## Production Configuration

### Environment-Specific Overrides

#### Production Override
Create `docker-compose.prod.yml`:
```yaml
version: '3.8'
services:
  timescaledb:
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data:Z
    
  api-gateway:
    environment:
      SPRING_PROFILES_ACTIVE: production
      JWT_SECRET: ${JWT_SECRET}
    restart: unless-stopped
    
  market-data-service:
    environment:
      POLYGON_API_KEY: ${POLYGON_API_KEY}
    restart: unless-stopped
```

#### Use Production Config
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Security Configuration

#### Secrets Management
```bash
# Create .env file
cat > .env << EOF
DB_PASSWORD=secure_password_here
JWT_SECRET=your_jwt_secret_256_bit_key
POLYGON_API_KEY=your_polygon_api_key
TASTYTRADE_CLIENT_ID=your_client_id
TASTYTRADE_CLIENT_SECRET=your_client_secret
EOF

# Use secrets in docker-compose
docker-compose --env-file .env up -d
```

#### Network Security
```yaml
# Add network isolation
networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
    internal: true

services:
  api-gateway:
    networks:
      - frontend
      - backend
  
  timescaledb:
    networks:
      - backend
```

## Service Management

### Container Management

#### Start/Stop Services
```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d market-data-service

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

#### Restart Services
```bash
# Restart specific service
docker-compose restart api-gateway

# Rebuild and restart
docker-compose up -d --build api-gateway
```

#### Scale Services
```bash
# Scale market data service to 3 replicas
docker-compose up -d --scale market-data-service=3

# Scale multiple services
docker-compose up -d --scale api-gateway=2 --scale market-data-service=3
```

### Logs and Monitoring

#### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f market-data-service

# Last 100 lines
docker-compose logs --tail=100 api-gateway
```

#### Monitor Resources
```bash
# Container stats
docker stats

# Specific containers
docker stats $(docker-compose ps -q)
```

### Health Checks

#### Service Health
```bash
# Check API Gateway
curl http://localhost:8080/actuator/health

# Check Market Data Service
curl http://localhost:8082/actuator/health

# Check all services
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo "Port $port: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health)"
done
```

#### Database Health
```bash
# Check TimescaleDB
docker-compose exec timescaledb pg_isready -U tastytrade

# Connect to database
docker-compose exec timescaledb psql -U tastytrade -d tastytrade
```

#### Message Broker Health
```bash
# RabbitMQ Management UI
open http://localhost:15672
# Default credentials: tastytrade/password

# Check RabbitMQ status
docker-compose exec rabbitmq rabbitmq-diagnostics status
```

## Data Management

### Database Operations

#### Database Backup
```bash
# Create backup
docker-compose exec timescaledb pg_dump -U tastytrade tastytrade > backup.sql

# Restore backup
docker-compose exec -T timescaledb psql -U tastytrade tastytrade < backup.sql
```

#### Database Migrations
```bash
# Run with specific profile for migrations
docker-compose exec api-gateway java -jar app.jar --spring.profiles.active=migration
```

### Volume Management

#### Persistent Volumes
```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect tastytrade-enhancement-platform_postgres_data

# Backup volume
docker run --rm -v tastytrade-enhancement-platform_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz /data
```

#### Clean Up Volumes
```bash
# Remove all volumes (WARNING: Data loss)
docker-compose down -v

# Remove specific volume
docker volume rm tastytrade-enhancement-platform_postgres_data
```

## Troubleshooting

### Common Issues

#### Port Conflicts
```bash
# Check port usage
lsof -i :8080

# Change ports in docker-compose.yml
ports:
  - "8090:8080"  # Change host port
```

#### Out of Memory
```bash
# Increase memory limits
services:
  market-data-service:
    mem_limit: 2g
    memswap_limit: 2g
```

#### Service Won't Start
```bash
# Check service logs
docker-compose logs market-data-service

# Check container status
docker-compose ps

# Restart service
docker-compose restart market-data-service
```

#### Database Connection Issues
```bash
# Check network connectivity
docker-compose exec api-gateway nc -zv timescaledb 5432

# Check database logs
docker-compose logs timescaledb

# Verify environment variables
docker-compose exec api-gateway env | grep POSTGRES
```

### Performance Optimization

#### Resource Limits
```yaml
services:
  market-data-service:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

#### JVM Tuning
```yaml
services:
  options-calculator:
    environment:
      JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### Network Issues

#### Service Discovery
```bash
# Test service connectivity
docker-compose exec api-gateway ping market-data-service

# Check DNS resolution
docker-compose exec api-gateway nslookup market-data-service
```

#### External API Access
```bash
# Test external API from container
docker-compose exec tastytrade-integration curl -I https://api.tastytrade.com
```

## Integration Testing

### Test Setup
```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
./scripts/test.sh

# Cleanup test environment
docker-compose -f docker-compose.test.yml down -v
```

### Load Testing
```bash
# Install k6 for load testing
brew install k6  # macOS

# Run load test
k6 run tests/load/api-gateway-load-test.js
```

This Docker deployment guide provides comprehensive instructions for running the Tastytrade Enhancement Platform in containerized environments, from development to production scenarios.