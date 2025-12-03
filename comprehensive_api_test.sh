#!/bin/bash

# TrackerPro Admin API Comprehensive Test Script
# Testing all endpoints as per problem statement

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "========================================="
echo "TrackerPro Admin API - Comprehensive Test"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

pass_count=0
fail_count=0

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
        ((pass_count++))
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
        ((fail_count++))
    fi
}

echo "=== 1. AUTHENTICATION TESTS ==="
echo ""

# Test 1: Admin Login
echo "Test 1: Admin Login (POST /api/auth/login)"
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trackerpro.com","password":"admin123"}')

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
BODY=$(echo "$LOGIN_RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    if [ ! -z "$TOKEN" ]; then
        print_result 0 "Admin Login - Got JWT token"
        echo "  Token: ${TOKEN:0:50}..."
    else
        print_result 1 "Admin Login - No token in response"
        echo "  Response: $BODY"
    fi
else
    print_result 1 "Admin Login - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

# Test 2: Invalid Credentials
echo "Test 2: Invalid Credentials Test"
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"wrong@email.com","password":"wrongpass"}' \
  -o /dev/null)

if [ "$RESPONSE" == "401" ] || [ "$RESPONSE" == "400" ]; then
    print_result 0 "Invalid Credentials - Properly rejected (HTTP $RESPONSE)"
else
    print_result 1 "Invalid Credentials - Should return 401 or 400, got $RESPONSE"
fi
echo ""

echo "=== 2. REGISTRATIONS (STUDENTS) TESTS ==="
echo ""

# Test 3: Get All Registrations
echo "Test 3: Get All Registrations (GET /api/registrations)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/registrations" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get Registrations - HTTP 200"
    echo "  Response: $(echo $BODY | head -c 200)..."
else
    print_result 1 "Get Registrations - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

# Test 4: Delete Registration (if exists)
echo "Test 4: Delete Registration (DELETE /api/registrations/{id})"
# First try to get an ID from the list
FIRST_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ ! -z "$FIRST_ID" ]; then
    RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/registrations/$FIRST_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ] || [ "$RESPONSE" == "204" ]; then
        print_result 0 "Delete Registration - HTTP $RESPONSE"
    else
        print_result 1 "Delete Registration - HTTP $RESPONSE"
    fi
else
    echo "  ⚠️  SKIP: No registrations to delete"
fi
echo ""

echo "=== 3. USER MANAGEMENT (HR/FACULTY) TESTS ==="
echo ""

# Test 5: Get All Users
echo "Test 5: Get All Users (GET /api/users)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get All Users - HTTP 200"
    echo "  Response: $(echo $BODY | head -c 200)..."
else
    print_result 1 "Get All Users - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

# Test 6: Create New User (HR)
echo "Test 6: Create New HR User (POST /api/users)"
TIMESTAMP=$(date +%s)
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Test\",
    \"lastName\": \"HR$TIMESTAMP\",
    \"email\": \"testhr$TIMESTAMP@trackerpro.com\",
    \"password\": \"password123\",
    \"role\": \"HR\",
    \"gender\": \"Male\",
    \"mobile\": \"91234${TIMESTAMP:5:5}\",
    \"city\": \"Mumbai\",
    \"dob\": \"1990-05-15\",
    \"age\": 34
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

NEW_USER_ID=""
if [ "$HTTP_CODE" == "200" ] || [ "$HTTP_CODE" == "201" ]; then
    NEW_USER_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    print_result 0 "Create HR User - HTTP $HTTP_CODE (User ID: $NEW_USER_ID)"
    echo "  Response: $(echo $BODY | head -c 200)..."
else
    print_result 1 "Create HR User - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

# Test 7: Update User
if [ ! -z "$NEW_USER_ID" ]; then
    echo "Test 7: Update User (PUT /api/users/$NEW_USER_ID)"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/users/$NEW_USER_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"firstName\": \"Updated\",
        \"lastName\": \"Name\",
        \"email\": \"testhr$TIMESTAMP@trackerpro.com\",
        \"role\": \"HR\",
        \"gender\": \"Male\",
        \"mobile\": \"91234${TIMESTAMP:5:5}\",
        \"city\": \"Delhi\",
        \"dob\": \"1990-05-15\",
        \"age\": 34
      }")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    
    if [ "$HTTP_CODE" == "200" ]; then
        print_result 0 "Update User - HTTP 200"
    else
        print_result 1 "Update User - HTTP $HTTP_CODE"
    fi
else
    echo "Test 7: Update User - SKIPPED (No user created)"
fi
echo ""

# Test 8: Toggle User Status
if [ ! -z "$NEW_USER_ID" ]; then
    echo "Test 8: Toggle User Status (PATCH /api/users/$NEW_USER_ID/toggle-status)"
    RESPONSE=$(curl -s -w "%{http_code}" -X PATCH "$BASE_URL/users/$NEW_USER_ID/toggle-status" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"isEnabled": false}' \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        print_result 0 "Toggle User Status - HTTP 200"
    else
        print_result 1 "Toggle User Status - HTTP $RESPONSE"
    fi
else
    echo "Test 8: Toggle User Status - SKIPPED (No user created)"
fi
echo ""

echo "=== 4. INTERNSHIPS (CAREER OUTCOMES) TESTS ==="
echo ""

# Test 9: Get All Internships
echo "Test 9: Get All Internships (GET /api/internships)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/internships" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get All Internships - HTTP 200"
    echo "  Response: $(echo $BODY | head -c 200)..."
else
    print_result 1 "Get All Internships - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

# Test 10: Create Internship
echo "Test 10: Create New Internship (POST /api/internships)"
TIMESTAMP=$(date +%s)
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/internships" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"internshipId\": \"TEST-$TIMESTAMP\",
    \"internshipTitle\": \"Test Internship $TIMESTAMP\",
    \"duration\": \"6 Months\",
    \"workMode\": \"Hybrid\",
    \"skills\": \"Testing, API Testing, Automation\",
    \"description\": \"This is a test internship posting\",
    \"status\": \"PUBLISHED\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

NEW_INTERNSHIP_ID=""
if [ "$HTTP_CODE" == "200" ] || [ "$HTTP_CODE" == "201" ]; then
    NEW_INTERNSHIP_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    print_result 0 "Create Internship - HTTP $HTTP_CODE (Internship ID: $NEW_INTERNSHIP_ID)"
    echo "  Response: $(echo $BODY | head -c 200)..."
else
    print_result 1 "Create Internship - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

# Test 11: Update Internship
if [ ! -z "$NEW_INTERNSHIP_ID" ]; then
    echo "Test 11: Update Internship (PUT /api/internships/$NEW_INTERNSHIP_ID)"
    RESPONSE=$(curl -s -w "%{http_code}" -X PUT "$BASE_URL/internships/$NEW_INTERNSHIP_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"internshipId\": \"TEST-$TIMESTAMP\",
        \"internshipTitle\": \"Updated Test Internship\",
        \"duration\": \"3 Months\",
        \"workMode\": \"Remote\",
        \"skills\": \"Updated Skills\",
        \"description\": \"Updated description\",
        \"status\": \"PUBLISHED\"
      }" \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        print_result 0 "Update Internship - HTTP 200"
    else
        print_result 1 "Update Internship - HTTP $RESPONSE"
    fi
else
    echo "Test 11: Update Internship - SKIPPED"
fi
echo ""

# Test 12: Toggle Internship Status
if [ ! -z "$NEW_INTERNSHIP_ID" ]; then
    echo "Test 12: Toggle Internship Status (PATCH /api/internships/$NEW_INTERNSHIP_ID/toggle-status)"
    RESPONSE=$(curl -s -w "%{http_code}" -X PATCH "$BASE_URL/internships/$NEW_INTERNSHIP_ID/toggle-status" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"status": "DRAFT"}' \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        print_result 0 "Toggle Internship Status - HTTP 200"
    else
        print_result 1 "Toggle Internship Status - HTTP $RESPONSE"
    fi
else
    echo "Test 12: Toggle Internship Status - SKIPPED"
fi
echo ""

# Test 13: Delete Internship
if [ ! -z "$NEW_INTERNSHIP_ID" ]; then
    echo "Test 13: Delete Internship (DELETE /api/internships/$NEW_INTERNSHIP_ID)"
    RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/internships/$NEW_INTERNSHIP_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ] || [ "$RESPONSE" == "204" ]; then
        print_result 0 "Delete Internship - HTTP $RESPONSE"
    else
        print_result 1 "Delete Internship - HTTP $RESPONSE"
    fi
else
    echo "Test 13: Delete Internship - SKIPPED"
fi
echo ""

echo "=== 5. DASHBOARD STATS TESTS ==="
echo ""

# Test 14: Get Dashboard Stats
echo "Test 14: Get Dashboard Statistics (GET /api/dashboard/stats)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/dashboard/stats" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get Dashboard Stats - HTTP 200"
    echo "  Response: $BODY"
else
    print_result 1 "Get Dashboard Stats - HTTP $HTTP_CODE"
    echo "  Response: $BODY"
fi
echo ""

echo "========================================="
echo "TEST SUMMARY"
echo "========================================="
echo -e "${GREEN}Passed: $pass_count${NC}"
echo -e "${RED}Failed: $fail_count${NC}"
echo "Total: $((pass_count + fail_count))"
echo ""

if [ $fail_count -eq 0 ]; then
    echo -e "${GREEN}✅ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  SOME TESTS FAILED${NC}"
    exit 1
fi
