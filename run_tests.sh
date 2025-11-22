#!/bin/bash

echo "========================================"
echo "TrackerPro Admin Module Testing"
echo "========================================"
echo ""

echo "=== TEST 1: Root URL (/) Redirection ==="
curl -s -L -o /dev/null -w "HTTP Status: %{http_code}\nRedirect URL: %{url_effective}\n" http://localhost:8080/
echo ""

echo "=== TEST 2: Index.html Accessibility ==="
curl -s http://localhost:8080/index.html | head -10
echo ""

echo "=== TEST 3: Admin Login ==="
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trackerpro.com","password":"admin123"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
  echo "✅ Admin login successful!"
  echo "Token obtained: ${TOKEN:0:50}..."
else
  echo "❌ Admin login failed!"
  echo "Response:"
  curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@trackerpro.com","password":"admin123"}'
  exit 1
fi
echo ""

echo "=== TEST 4: Create New HR User ==="
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "HR",
    "email": "testhr@trackerpro.com",
    "password": "password123",
    "userType": "HR",
    "gender": "Male",
    "mobileNo": "9876543210",
    "dob": "15/05/1990",
    "age": 34,
    "location": "Mumbai"
  }')

echo "$CREATE_RESPONSE" | python3 -m json.tool
echo ""

echo "=== TEST 5: Verify User Appears in Users List ==="
curl -s -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | python3 -m json.tool
echo ""

echo "========================================"
echo "All Tests Completed!"
echo "========================================"
