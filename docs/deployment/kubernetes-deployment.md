# Kubernetes Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Tastytrade Enhancement Platform on Kubernetes. The platform consists of 9 microservices, 3 infrastructure components, and a React frontend.

## Prerequisites

### Software Requirements
- **Kubernetes**: v1.28+ (tested with v1.28)
- **kubectl**: v1.28+ 
- **Docker**: v24.0+ (for image building)
- **Helm**: v3.12+ (optional, for advanced configurations)

### Cluster Requirements
- **Minimum Resources**: 8 CPU cores, 16GB RAM, 200GB storage
- **Recommended Resources**: 16 CPU cores, 32GB RAM, 500GB storage
- **Storage Classes**: Fast SSD storage class recommended
- **Ingress Controller**: NGINX Ingress Controller

### Network Requirements
- **Load Balancer**: External load balancer for production
- **DNS**: Ability to configure DNS records
- **TLS Certificates**: SSL/TLS certificates for HTTPS

## Quick Start

### 1. Clone and Navigate
```bash
git clone <repository-url>
cd tastytrade-enhancement-platform
```

### 2. Configure Secrets
```bash
# Edit secrets with actual values
cp kubernetes/secrets/app-secrets.yaml kubernetes/secrets/app-secrets-prod.yaml
# Update base64 encoded values in app-secrets-prod.yaml
```

### 3. Deploy with Script
```bash
# Full deployment
./scripts/k8s-deploy.sh

# With image building
./scripts/k8s-deploy.sh --build-images

# Cleanup deployment
./scripts/k8s-deploy.sh --cleanup
```

## Manual Deployment Steps

### Step 1: Create Namespace
```bash
kubectl apply -f kubernetes/namespace.yaml
```

### Step 2: Deploy Secrets and ConfigMaps
```bash
# Deploy secrets (update with actual values first)
kubectl apply -f kubernetes/secrets/app-secrets.yaml

# Deploy configuration
kubectl apply -f kubernetes/configmaps/app-config.yaml
```

### Step 3: Deploy Infrastructure Services
```bash
# TimescaleDB
kubectl apply -f kubernetes/deployments/timescaledb.yaml

# Redis
kubectl apply -f kubernetes/deployments/redis.yaml

# RabbitMQ
kubectl apply -f kubernetes/deployments/rabbitmq.yaml

# Infrastructure services
kubectl apply -f kubernetes/services/infrastructure-services.yaml
```

### Step 4: Wait for Infrastructure
```bash
# Wait for all infrastructure pods to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=database -n tastytrade --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=cache -n tastytrade --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=message-broker -n tastytrade --timeout=300s
```

### Step 5: Deploy Application Services
```bash
# Core services
kubectl apply -f kubernetes/deployments/user-service.yaml
kubectl apply -f kubernetes/deployments/market-data-service.yaml
kubectl apply -f kubernetes/deployments/options-calculator.yaml
kubectl apply -f kubernetes/deployments/delta-monitor.yaml
kubectl apply -f kubernetes/deployments/api-gateway.yaml

# Application services
kubectl apply -f kubernetes/services/application-services.yaml
```

### Step 6: Deploy Networking
```bash
# Install NGINX Ingress Controller (if not present)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml

# Deploy ingress
kubectl apply -f kubernetes/services/ingress.yaml
```

## Configuration

### Environment Variables
All configuration is managed through ConfigMaps and Secrets:

- **app-config**: Application configuration
- **postgres-config**: Database configuration  
- **rabbitmq-config**: Message broker configuration
- **app-secrets**: Sensitive credentials

### Key Configuration Parameters

#### Database Configuration
```yaml
POSTGRES_HOST: timescaledb-service
POSTGRES_PORT: "5432"
POSTGRES_DB: tastytrade
```

#### Cache Configuration
```yaml
REDIS_HOST: redis-service
REDIS_PORT: "6379"
CACHE_DEFAULT_TTL: "300"
```

#### Message Broker Configuration
```yaml
RABBITMQ_HOST: rabbitmq-service
RABBITMQ_PORT: "5672"
RABBITMQ_VHOST: "/"
```

### Required Secrets

Update these secrets with actual values:

```bash
# JWT Secret (256-bit)
JWT_SECRET: <base64-encoded-secret>

# Database Password
DB_PASSWORD: <base64-encoded-password>

# External API Keys
TASTYTRADE_CLIENT_ID: <base64-encoded-client-id>
TASTYTRADE_CLIENT_SECRET: <base64-encoded-client-secret>
POLYGON_API_KEY: <base64-encoded-api-key>

# Notification Services
TWILIO_ACCOUNT_SID: <base64-encoded-sid>
TWILIO_AUTH_TOKEN: <base64-encoded-token>
FIREBASE_PRIVATE_KEY: <base64-encoded-key>
```

## Service Architecture

### Infrastructure Services
1. **TimescaleDB** (5432): Time-series database
2. **Redis** (6379): Caching and session storage
3. **RabbitMQ** (5672/15672): Message broker

### Application Services
1. **API Gateway** (8080): Main entry point
2. **User Service** (8084): Authentication/authorization
3. **Market Data Service** (8082): Real-time data streaming
4. **Options Calculator** (8085): Greeks calculations
5. **Delta Monitor** (8086): Risk monitoring
6. **Notification Service** (8087): Alerts
7. **Portfolio Service** (8083): Position management
8. **Reporting Service** (8088): Data export
9. **Tastytrade Integration** (8081): External API

## Scaling Configuration

### Horizontal Pod Autoscalers (HPA)

#### API Gateway
- **Min Replicas**: 3
- **Max Replicas**: 10
- **CPU Target**: 70%
- **Memory Target**: 80%

#### Market Data Service
- **Min Replicas**: 3
- **Max Replicas**: 15
- **CPU Target**: 70%
- **Aggressive Scaling**: High-traffic service

#### User Service
- **Min Replicas**: 2
- **Max Replicas**: 8
- **CPU Target**: 70%

### Manual Scaling
```bash
# Scale specific service
kubectl scale deployment market-data-service --replicas=5 -n tastytrade

# Scale all services
kubectl scale deployment --all --replicas=3 -n tastytrade
```

## Monitoring and Health Checks

### Health Check Endpoints
All services expose health check endpoints:

```bash
# Check service health
kubectl exec -n tastytrade deployment/api-gateway -- curl localhost:8080/actuator/health

# Check all pods
kubectl get pods -n tastytrade

# Check services
kubectl get svc -n tastytrade
```

### Logs and Debugging
```bash
# View logs for specific service
kubectl logs -f deployment/market-data-service -n tastytrade

# View logs for all pods with label
kubectl logs -f -l app.kubernetes.io/component=market-data -n tastytrade

# Describe pod for troubleshooting
kubectl describe pod <pod-name> -n tastytrade
```

## Network Access

### Internal Access
Services communicate internally using Kubernetes DNS:
- `api-gateway-service.tastytrade.svc.cluster.local:8080`
- `market-data-service.tastytrade.svc.cluster.local:8082`

### External Access

#### Production Setup
1. Configure DNS records:
   ```
   api.tastytrade.com -> Load Balancer IP
   ws.tastytrade.com -> Load Balancer IP
   ```

2. Update TLS certificates in `tls-secret`

#### Local Development
Add to `/etc/hosts`:
```
127.0.0.1 api.tastytrade.local
127.0.0.1 ws.tastytrade.local
127.0.0.1 admin.tastytrade.local
```

### Access URLs
- **API Gateway**: https://api.tastytrade.local
- **WebSocket**: wss://ws.tastytrade.local/ws
- **RabbitMQ Management**: http://admin.tastytrade.local/rabbitmq

## Security

### Network Policies
Network policies restrict communication between pods:
- Only ingress controller can access application ports
- Services can communicate with infrastructure
- External egress allowed for API calls

### RBAC (Future Enhancement)
Create service accounts with minimal required permissions:
```bash
kubectl create serviceaccount tastytrade-app -n tastytrade
```

### Secrets Management
- Use external secret management (HashiCorp Vault, AWS Secrets Manager)
- Rotate secrets regularly
- Monitor secret access

## Backup and Recovery

### Database Backup
```bash
# Create database backup
kubectl exec -n tastytrade deployment/timescaledb -- pg_dump -U tastytrade tastytrade > backup.sql

# Restore database
kubectl exec -i -n tastytrade deployment/timescaledb -- psql -U tastytrade tastytrade < backup.sql
```

### Redis Backup
```bash
# Create Redis backup
kubectl exec -n tastytrade deployment/redis -- redis-cli --rdb /tmp/dump.rdb
```

### Configuration Backup
```bash
# Backup all configurations
kubectl get all,configmap,secret -n tastytrade -o yaml > tastytrade-backup.yaml
```

## Troubleshooting

### Common Issues

#### Pods Not Starting
```bash
# Check pod events
kubectl describe pod <pod-name> -n tastytrade

# Check resource constraints
kubectl top pods -n tastytrade
```

#### Database Connection Issues
```bash
# Check database connectivity
kubectl exec -n tastytrade deployment/api-gateway -- nc -zv timescaledb-service 5432

# Check database logs
kubectl logs -f deployment/timescaledb -n tastytrade
```

#### High Memory Usage
```bash
# Check memory usage
kubectl top pods -n tastytrade

# Adjust resource limits
kubectl patch deployment market-data-service -n tastytrade -p '{"spec":{"template":{"spec":{"containers":[{"name":"market-data-service","resources":{"limits":{"memory":"4Gi"}}}]}}}}'
```

### Performance Optimization

#### Resource Tuning
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

#### JVM Tuning
Add JVM options to deployment:
```yaml
env:
- name: JAVA_OPTS
  value: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

## Maintenance

### Rolling Updates
```bash
# Update deployment image
kubectl set image deployment/market-data-service market-data-service=tastytrade/market-data-service:v2.0 -n tastytrade

# Check rollout status
kubectl rollout status deployment/market-data-service -n tastytrade

# Rollback if needed
kubectl rollout undo deployment/market-data-service -n tastytrade
```

### Health Maintenance
```bash
# Restart unhealthy pods
kubectl delete pod -l app.kubernetes.io/name=market-data-service -n tastytrade

# Drain node for maintenance
kubectl drain <node-name> --ignore-daemonsets --delete-emptydir-data
```

This comprehensive Kubernetes deployment provides a production-ready setup for the Tastytrade Enhancement Platform with proper scaling, monitoring, and security configurations.