#!/bin/bash

echo "========================================="
echo "TrackerPro API Testing Script"
echo "========================================="
echo ""

BASE_URL="http://localhost:8080"

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
HEALTH_RESPONSE=$(curl -s $BASE_URL/api/auth/health)
if [ "$HEALTH_RESPONSE" = "Auth service is running" ]; then
    echo -e "${GREEN}✓ Health check passed${NC}"
else
    echo -e "${RED}✗ Health check failed${NC}"
fi
echo ""

# Test 2: Admin Login
echo -e "${YELLOW}Test 2: Admin Login${NC}"
ADMIN_LOGIN=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trackerpro.com","password":"admin123"}')

ADMIN_SUCCESS=$(echo $ADMIN_LOGIN | jq -r '.success')
if [ "$ADMIN_SUCCESS" = "true" ]; then
    echo -e "${GREEN}✓ Admin login successful${NC}"
    ADMIN_TOKEN=$(echo $ADMIN_LOGIN | jq -r '.token')
    echo "Admin Token: ${ADMIN_TOKEN:0:50}..."
else
    echo -e "${RED}✗ Admin login failed${NC}"
fi
echo ""

# Test 3: Student Registration
echo -e "${YELLOW}Test 3: Student Registration${NC}"
RANDOM_EMAIL="student_$(date +%s)@test.com"
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Test\",
    \"lastName\": \"Student\",
    \"email\": \"$RANDOM_EMAIL\",
    \"password\": \"test123\",
    \"mobileNo\": \"98765$(shuf -i 10000-99999 -n 1)\",
    \"gender\": \"Male\",
    \"dob\": \"15/05/2001\",
    \"age\": 23,
    \"location\": \"Mumbai\",
    \"userType\": \"STUDENT\"
  }")

REG_SUCCESS=$(echo $REGISTER_RESPONSE | jq -r '.success')
if [ "$REG_SUCCESS" = "true" ]; then
    echo -e "${GREEN}✓ Student registration successful${NC}"
    echo "Registered Email: $RANDOM_EMAIL"
else
    echo -e "${RED}✗ Student registration failed${NC}"
    echo "Error: $(echo $REGISTER_RESPONSE | jq -r '.message')"
fi
echo ""

# Test 4: Student Login (with newly registered user)
echo -e "${YELLOW}Test 4: Student Login (New User)${NC}"
if [ "$REG_SUCCESS" = "true" ]; then
    STUDENT_LOGIN=$(curl -s -X POST $BASE_URL/api/auth/login \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$RANDOM_EMAIL\",\"password\":\"test123\"}")
    
    STUDENT_SUCCESS=$(echo $STUDENT_LOGIN | jq -r '.success')
    if [ "$STUDENT_SUCCESS" = "true" ]; then
        echo -e "${GREEN}✓ Student login successful${NC}"
    else
        echo -e "${RED}✗ Student login failed${NC}"
    fi
else
    echo -e "${YELLOW}⊘ Skipped (registration failed)${NC}"
fi
echo ""

# Test 5: Invalid Credentials
echo -e "${YELLOW}Test 5: Invalid Login Credentials${NC}"
INVALID_LOGIN=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"wrong@test.com","password":"wrongpass"}')

INVALID_SUCCESS=$(echo $INVALID_LOGIN | jq -r '.success')
if [ "$INVALID_SUCCESS" = "false" ]; then
    echo -e "${GREEN}✓ Invalid credentials correctly rejected${NC}"
else
    echo -e "${RED}✗ Invalid credentials validation failed${NC}"
fi
echo ""

# Test 6: Duplicate Email Registration
echo -e "${YELLOW}Test 6: Duplicate Email Registration${NC}"
DUP_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Duplicate",
    "lastName": "User",
    "email": "admin@trackerpro.com",
    "password": "test123",
    "mobileNo": "9876543211",
    "gender": "Male",
    "dob": "01/01/2000",
    "age": 24,
    "location": "Delhi",
    "userType": "STUDENT"
  }')

DUP_SUCCESS=$(echo $DUP_RESPONSE | jq -r '.success')
if [ "$DUP_SUCCESS" = "false" ]; then
    echo -e "${GREEN}✓ Duplicate email correctly rejected${NC}"
    echo "Message: $(echo $DUP_RESPONSE | jq -r '.message')"
else
    echo -e "${RED}✗ Duplicate email validation failed${NC}"
fi
echo ""

echo "========================================="
echo "Testing Complete!"
echo "========================================="
