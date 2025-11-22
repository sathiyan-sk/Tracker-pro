#!/bin/bash

echo "========================================"
echo "Testing Fixes for User Management & H2 Console"
echo "========================================"
echo ""

# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trackerpro.com","password":"admin123"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "=== TEST 1: Create Another HR User ==="
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@trackerpro.com",
    "password": "password123",
    "userType": "HR",
    "gender": "Male",
    "mobileNo": "9876543211",
    "dob": "20/06/1988",
    "age": 36,
    "location": "Delhi"
  }')
echo "$CREATE_RESPONSE" | python3 -m json.tool
echo ""

echo "=== TEST 2: Create Faculty User ==="
CREATE_RESPONSE2=$(curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sarah",
    "lastName": "Johnson",
    "email": "sarah.j@trackerpro.com",
    "password": "faculty123",
    "userType": "FACULTY",
    "gender": "Female",
    "mobileNo": "9876543212",
    "dob": "15/03/1985",
    "age": 39,
    "location": "Bangalore"
  }')
echo "$CREATE_RESPONSE2" | python3 -m json.tool
echo ""

echo "=== TEST 3: Get All Users (HR & Faculty) ==="
curl -s -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
echo ""

echo "=== TEST 4: Filter Only HR Users ==="
curl -s -X GET "http://localhost:8080/api/v1/users?filter=hr" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
echo ""

echo "=== TEST 5: Filter Only Faculty Users ==="
curl -s -X GET "http://localhost:8080/api/v1/users?filter=faculty" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
echo ""

echo "=== TEST 6: H2 Console Accessibility ==="
H2_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/h2-console)
if [ "$H2_RESPONSE" = "200" ]; then
  echo "✅ H2 Console is accessible (HTTP 200)"
  echo "URL: http://localhost:8080/h2-console"
  echo "JDBC URL: jdbc:h2:mem:trackerpro_db"
  echo "Username: sa"
  echo "Password: (leave empty)"
else
  echo "❌ H2 Console returned HTTP $H2_RESPONSE"
fi
echo ""

echo "========================================"
echo "All Tests Completed!"
echo "========================================"
