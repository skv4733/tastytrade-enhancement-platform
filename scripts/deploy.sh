#!/bin/bash

echo "Deploying Tastytrade Enhancement Platform..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Please start Docker first."
    exit 1
fi

# Build and start services with Docker Compose
echo "Starting services with Docker Compose..."
docker-compose up --build -d

echo "Checking service health..."
sleep 10

# Check if services are running
services=("postgres" "redis" "api-gateway")
for service in "${services[@]}"; do
    if docker-compose ps | grep -q "$service.*Up"; then
        echo "✓ $service is running"
    else
        echo "✗ $service failed to start"
        docker-compose logs $service
    fi
done

echo "Deployment completed!"
echo "API Gateway available at: http://localhost:8080"
echo "To view logs: docker-compose logs -f [service-name]"
echo "To stop all services: docker-compose down"