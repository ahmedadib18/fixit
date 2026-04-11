#!/bin/bash

# Backend Health Check Script
# Run this to verify backend is working

echo "=================================="
echo "FixIt Backend Health Check"
echo "=================================="
echo ""

# Check if backend is running
echo "1. Checking if backend is running on port 8080..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Backend is running"
else
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo "   ✅ Backend is running (no actuator endpoint)"
    else
        echo "   ❌ Backend is NOT running"
        echo "   Start it with: mvn spring-boot:run"
        exit 1
    fi
fi
echo ""

# Test API endpoints
echo "2. Testing API endpoints..."

# Test health/root endpoint
echo "   Testing root endpoint..."
curl -s http://localhost:8080 > /dev/null && echo "   ✅ Root endpoint accessible" || echo "   ❌ Root endpoint failed"

# Test auth endpoints
echo "   Testing auth endpoints..."
curl -s http://localhost:8080/api/auth/login > /dev/null && echo "   ✅ Auth endpoints accessible" || echo "   ❌ Auth endpoints failed"

echo ""
echo "3. Database Check..."
echo "   Current profile: $(grep 'spring.profiles.active' src/main/resources/application.properties | cut -d'=' -f2)"
echo ""

# Check H2 console (if dev mode)
if grep -q "spring.profiles.active=dev" src/main/resources/application.properties; then
    echo "   H2 Console: http://localhost:8080/h2-console"
    echo "   JDBC URL: jdbc:h2:mem:fixitdb"
    echo "   Username: sa"
    echo "   Password: (empty)"
fi

echo ""
echo "=================================="
echo "Frontend: http://localhost:5173"
echo "Backend: http://localhost:8080"
echo "=================================="
