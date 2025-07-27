#!/bin/bash

# Kubernetes Deployment Script for Tastytrade Enhancement Platform
# This script deploys the complete platform to Kubernetes

set -e

# Configuration
NAMESPACE="tastytrade"
KUBECTL_VERSION_REQUIRED="1.28"
HELM_VERSION_REQUIRED="3.12"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi
    
    # Check kubectl version
    KUBECTL_VERSION=$(kubectl version --client --short | grep -o 'v[0-9]*\.[0-9]*' | head -1 | sed 's/v//')
    if [[ "$(printf '%s\n' "$KUBECTL_VERSION_REQUIRED" "$KUBECTL_VERSION" | sort -V | head -n1)" != "$KUBECTL_VERSION_REQUIRED" ]]; then
        log_warning "kubectl version $KUBECTL_VERSION is older than recommended $KUBECTL_VERSION_REQUIRED"
    fi
    
    # Check cluster connectivity
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        exit 1
    fi
    
    # Check if namespace exists
    if kubectl get namespace $NAMESPACE &> /dev/null; then
        log_warning "Namespace $NAMESPACE already exists. Continuing with existing namespace."
    fi
    
    log_success "Prerequisites check passed"
}

# Create namespace and basic setup
setup_namespace() {
    log_info "Setting up namespace and basic resources..."
    
    # Create namespace
    kubectl apply -f kubernetes/namespace.yaml
    
    # Wait for namespace to be ready
    kubectl wait --for=condition=Active namespace/$NAMESPACE --timeout=60s
    
    log_success "Namespace setup completed"
}

# Deploy secrets (with safety checks)
deploy_secrets() {
    log_info "Deploying secrets..."
    
    # Check if secrets already exist
    if kubectl get secret app-secrets -n $NAMESPACE &> /dev/null; then
        log_warning "Secrets already exist. Skipping secret creation."
        log_warning "Please ensure secrets are properly configured with actual values."
        return
    fi
    
    # Deploy secrets
    kubectl apply -f kubernetes/secrets/app-secrets.yaml
    
    log_warning "⚠️  IMPORTANT: Default secrets have been deployed with placeholder values."
    log_warning "   Please update the following secrets with actual values:"
    log_warning "   - JWT_SECRET"
    log_warning "   - DB_PASSWORD"
    log_warning "   - RABBITMQ_PASSWORD"
    log_warning "   - External API keys (Tastytrade, Polygon, etc.)"
    log_warning "   - Notification service credentials (Twilio, Firebase, AWS SES)"
    
    log_success "Secrets deployed (with placeholder values)"
}

# Deploy ConfigMaps
deploy_configmaps() {
    log_info "Deploying ConfigMaps..."
    
    kubectl apply -f kubernetes/configmaps/app-config.yaml
    
    log_success "ConfigMaps deployed"
}

# Deploy infrastructure services
deploy_infrastructure() {
    log_info "Deploying infrastructure services..."
    
    # Deploy TimescaleDB
    log_info "Deploying TimescaleDB..."
    kubectl apply -f kubernetes/deployments/timescaledb.yaml
    
    # Deploy Redis
    log_info "Deploying Redis..."
    kubectl apply -f kubernetes/deployments/redis.yaml
    
    # Deploy RabbitMQ
    log_info "Deploying RabbitMQ..."
    kubectl apply -f kubernetes/deployments/rabbitmq.yaml
    
    # Deploy infrastructure services
    kubectl apply -f kubernetes/services/infrastructure-services.yaml
    
    log_info "Waiting for infrastructure services to be ready..."
    
    # Wait for TimescaleDB
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=timescaledb -n $NAMESPACE --timeout=300s
    log_success "TimescaleDB is ready"
    
    # Wait for Redis
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=redis -n $NAMESPACE --timeout=300s
    log_success "Redis is ready"
    
    # Wait for RabbitMQ
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=rabbitmq -n $NAMESPACE --timeout=300s
    log_success "RabbitMQ is ready"
    
    log_success "Infrastructure services deployed and ready"
}

# Build and push Docker images (if Docker registry is available)
build_and_push_images() {
    log_info "Building and pushing Docker images..."
    
    # Check if Docker registry is configured
    if [[ -z "${DOCKER_REGISTRY}" ]]; then
        log_warning "DOCKER_REGISTRY environment variable not set. Skipping image build."
        log_warning "Please build and push images manually or set DOCKER_REGISTRY."
        return
    fi
    
    # List of services to build
    services=("api-gateway" "user-service" "tastytrade-integration" "market-data-service" 
              "options-calculator" "delta-monitor" "notification-service" 
              "portfolio-service" "reporting-service")
    
    for service in "${services[@]}"; do
        log_info "Building $service..."
        
        # Build with Maven
        mvn -pl services/$service clean package -DskipTests
        
        # Build Docker image
        docker build -t ${DOCKER_REGISTRY}/tastytrade/${service}:latest services/$service/
        
        # Push to registry
        docker push ${DOCKER_REGISTRY}/tastytrade/${service}:latest
        
        log_success "$service image built and pushed"
    done
    
    log_success "All images built and pushed"
}

# Deploy application services
deploy_applications() {
    log_info "Deploying application services..."
    
    # Deploy core services first
    log_info "Deploying User Service..."
    kubectl apply -f kubernetes/deployments/user-service.yaml
    
    log_info "Deploying Options Calculator..."
    kubectl apply -f kubernetes/deployments/options-calculator.yaml
    
    log_info "Deploying Market Data Service..."
    kubectl apply -f kubernetes/deployments/market-data-service.yaml
    
    log_info "Deploying Delta Monitor..."
    kubectl apply -f kubernetes/deployments/delta-monitor.yaml
    
    log_info "Deploying API Gateway..."
    kubectl apply -f kubernetes/deployments/api-gateway.yaml
    
    # Deploy application services
    kubectl apply -f kubernetes/services/application-services.yaml
    
    log_info "Waiting for application services to be ready..."
    
    # Wait for User Service
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=user-service -n $NAMESPACE --timeout=300s
    log_success "User Service is ready"
    
    # Wait for Options Calculator
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=options-calculator -n $NAMESPACE --timeout=300s
    log_success "Options Calculator is ready"
    
    # Wait for Market Data Service
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=market-data-service -n $NAMESPACE --timeout=300s
    log_success "Market Data Service is ready"
    
    # Wait for API Gateway
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=api-gateway -n $NAMESPACE --timeout=300s
    log_success "API Gateway is ready"
    
    log_success "Application services deployed and ready"
}

# Deploy ingress and networking
deploy_networking() {
    log_info "Deploying ingress and networking..."
    
    # Check if NGINX Ingress Controller is installed
    if ! kubectl get ingressclass nginx &> /dev/null; then
        log_warning "NGINX Ingress Controller not found. Installing..."
        kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
        
        # Wait for ingress controller to be ready
        kubectl wait --namespace ingress-nginx \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/component=controller \
            --timeout=300s
    fi
    
    # Deploy ingress
    kubectl apply -f kubernetes/services/ingress.yaml
    
    log_success "Ingress and networking deployed"
}

# Verify deployment
verify_deployment() {
    log_info "Verifying deployment..."
    
    # Check all pods are running
    log_info "Checking pod status..."
    kubectl get pods -n $NAMESPACE
    
    # Check services
    log_info "Checking service status..."
    kubectl get services -n $NAMESPACE
    
    # Check ingress
    log_info "Checking ingress status..."
    kubectl get ingress -n $NAMESPACE
    
    # Health check for each service
    log_info "Performing health checks..."
    
    services=("api-gateway-service:8080" "user-service:8084" "market-data-service:8082" 
              "options-calculator-service:8085")
    
    for service in "${services[@]}"; do
        service_name=$(echo $service | cut -d':' -f1)
        port=$(echo $service | cut -d':' -f2)
        
        if kubectl exec -n $NAMESPACE deployment/$service_name -- wget -q --spider http://localhost:$port/actuator/health; then
            log_success "$service_name health check passed"
        else
            log_warning "$service_name health check failed"
        fi
    done
    
    log_success "Deployment verification completed"
}

# Print access information
print_access_info() {
    log_info "Deployment completed successfully!"
    echo
    log_info "Access Information:"
    echo "===================="
    
    # Get ingress IP
    INGRESS_IP=$(kubectl get ingress tastytrade-ingress -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "pending")
    
    if [[ "$INGRESS_IP" != "pending" && -n "$INGRESS_IP" ]]; then
        echo "🌐 API Gateway: https://$INGRESS_IP"
        echo "📊 Market Data WebSocket: wss://$INGRESS_IP/ws"
    else
        echo "🌐 API Gateway: https://api.tastytrade.local (add to /etc/hosts)"
        echo "📊 Market Data WebSocket: wss://ws.tastytrade.local/ws"
        echo
        log_info "To access locally, add these entries to your /etc/hosts file:"
        echo "127.0.0.1 api.tastytrade.local"
        echo "127.0.0.1 ws.tastytrade.local"
        echo "127.0.0.1 admin.tastytrade.local"
    fi
    
    echo
    log_info "Management Interfaces:"
    echo "🐰 RabbitMQ Management: http://admin.tastytrade.local/rabbitmq"
    echo "📈 Metrics: http://admin.tastytrade.local/health/*"
    
    echo
    log_info "Useful Commands:"
    echo "📋 Check pods: kubectl get pods -n $NAMESPACE"
    echo "📋 Check logs: kubectl logs -f deployment/<service-name> -n $NAMESPACE"
    echo "📋 Scale service: kubectl scale deployment <service-name> --replicas=<count> -n $NAMESPACE"
    echo "🔄 Restart service: kubectl rollout restart deployment/<service-name> -n $NAMESPACE"
    
    echo
    log_warning "⚠️  Remember to:"
    echo "   1. Update secrets with actual values"
    echo "   2. Configure external API credentials"
    echo "   3. Set up monitoring and alerting"
    echo "   4. Configure backup strategies"
}

# Cleanup function
cleanup() {
    if [[ "${1}" == "true" ]]; then
        log_info "Cleaning up deployment..."
        kubectl delete namespace $NAMESPACE --ignore-not-found=true
        log_success "Cleanup completed"
    fi
}

# Main deployment function
main() {
    echo "🚀 Tastytrade Enhancement Platform - Kubernetes Deployment"
    echo "=========================================================="
    echo
    
    # Parse arguments
    CLEANUP=false
    BUILD_IMAGES=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --cleanup)
                CLEANUP=true
                shift
                ;;
            --build-images)
                BUILD_IMAGES=true
                shift
                ;;
            --help)
                echo "Usage: $0 [OPTIONS]"
                echo "Options:"
                echo "  --cleanup      Remove all deployed resources"
                echo "  --build-images Build and push Docker images"
                echo "  --help         Show this help message"
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    if [[ "$CLEANUP" == "true" ]]; then
        cleanup true
        exit 0
    fi
    
    # Run deployment steps
    check_prerequisites
    setup_namespace
    deploy_secrets
    deploy_configmaps
    deploy_infrastructure
    
    if [[ "$BUILD_IMAGES" == "true" ]]; then
        build_and_push_images
    fi
    
    deploy_applications
    deploy_networking
    verify_deployment
    print_access_info
    
    log_success "🎉 Deployment completed successfully!"
}

# Run main function with all arguments
main "$@"