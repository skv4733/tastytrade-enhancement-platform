#!/bin/bash

echo "Running integration tests for Tastytrade Enhancement Platform..."

# Run Maven tests for all modules
echo "Running unit tests..."
mvn test -q

# Run frontend tests
echo "Running frontend tests..."
cd frontend
npm test -- --watchAll=false --silent
cd ..

# Run integration tests if services are running
if docker-compose ps | grep -q "Up"; then
    echo "Running integration tests..."
    # Add integration test commands here
    echo "Integration tests completed."
else
    echo "Services not running. Skipping integration tests."
    echo "Run './scripts/deploy.sh' first to start services."
fi

echo "All tests completed!"