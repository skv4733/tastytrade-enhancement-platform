#!/bin/bash

echo "Building Tastytrade Enhancement Platform..."

# Build parent project
echo "Building parent POM..."
mvn clean compile -q

# Build shared modules
echo "Building shared modules..."
for module in shared-models shared-security shared-utils; do
    echo "  Building $module..."
    mvn -pl shared/$module clean package -DskipTests -q
done

# Build services
echo "Building microservices..."
services=("api-gateway" "tastytrade-integration" "market-data-service" "options-calculator" "delta-monitor" "notification-service" "portfolio-service" "user-service" "reporting-service")

for service in "${services[@]}"; do
    echo "  Building $service..."
    mvn -pl services/$service clean package -DskipTests -q
done

# Build frontend
echo "Building frontend..."
cd frontend
npm install --silent
npm run build --silent
cd ..

echo "Build completed successfully!"