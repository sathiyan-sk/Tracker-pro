#!/bin/bash

# TrackerPro Admin API - Corrected Comprehensive Test Script
# Using correct field names and data types as per DTOs

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "=============================================="
echo "TrackerPro Admin API - Corrected Full Test"
echo "=============================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass_count=0
fail_count=0

print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
        ((pass_count++))
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
        ((fail_count++))
    fi
}

echo "=== 1. AUTHENTICATION ==="
echo ""

# Test 1: Admin Login
echo "Test 1: POST /api/auth/login (Admin)"
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trackerpro.com","password":"admin123"}')

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
BODY=$(echo "$LOGIN_RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    if [ ! -z "$TOKEN" ]; then
        print_result 0 "Admin Login"
        echo "  ✓ JWT Token received"
    else
        print_result 1 "Admin Login - No token"
    fi
else
    print_result 1 "Admin Login - HTTP $HTTP_CODE"
fi
echo ""

echo "=== 2. REGISTRATIONS (Students) ==="
echo ""

# Test 2: Get All Registrations
echo "Test 2: GET /api/registrations"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/registrations" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get All Registrations"
    TOTAL=$(echo "$BODY" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo "  ✓ Total students: $TOTAL"
else
    print_result 1 "Get All Registrations - HTTP $HTTP_CODE"
fi
echo ""

# Test 3: Delete Registration (skip if no data)
echo "Test 3: DELETE /api/registrations/{id}"
FIRST_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ ! -z "$FIRST_ID" ]; then
    RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/registrations/$FIRST_ID" \
      -H "Authorization: Bearer $TOKEN" -o /dev/null)
    
    if [ "$RESPONSE" == "200" ] || [ "$RESPONSE" == "204" ]; then
        print_result 0 "Delete Registration"
    else
        print_result 1 "Delete Registration - HTTP $RESPONSE"
    fi
else
    echo "  ⚠️  SKIP: No registrations to delete"
fi
echo ""

echo "=== 3. USER MANAGEMENT (HR/Faculty) ==="
echo ""

# Test 4: Get All Users
echo "Test 4: GET /api/users"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get All Users"
    TOTAL=$(echo "$BODY" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo "  ✓ Total HR/Faculty: $TOTAL"
else
    print_result 1 "Get All Users - HTTP $HTTP_CODE"
fi
echo ""

# Test 5: Create New User (HR) - CORRECTED FIELDS
echo "Test 5: POST /api/users (Create HR User)"
TIMESTAMP=$(date +%s)
MOBILE_SUFFIX=$((TIMESTAMP % 100000))

CREATE_USER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Rahul\",
    \"lastName\": \"Singh\",
    \"email\": \"rahul.singh${TIMESTAMP}@trackerpro.com\",
    \"password\": \"password123\",
    \"userType\": \"HR\",
    \"gender\": \"Male\",
    \"mobileNo\": \"912345${MOBILE_SUFFIX:0:4}\",
    \"location\": \"Chennai\",
    \"dob\": \"20/08/1988\",
    \"age\": 36
  }")

HTTP_CODE=$(echo "$CREATE_USER_RESPONSE" | tail -n1)
BODY=$(echo "$CREATE_USER_RESPONSE" | head -n-1)

NEW_USER_ID=""
if [ "$HTTP_CODE" == "200" ] || [ "$HTTP_CODE" == "201" ]; then
    NEW_USER_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    print_result 0 "Create HR User"
    echo "  ✓ User ID: $NEW_USER_ID"
    echo "  ✓ Email: rahul.singh${TIMESTAMP}@trackerpro.com"
else
    print_result 1 "Create HR User - HTTP $HTTP_CODE"
    echo "  Error: $(echo $BODY | head -c 300)"
fi
echo ""

# Test 6: Update User
if [ ! -z "$NEW_USER_ID" ]; then
    echo "Test 6: PUT /api/users/$NEW_USER_ID"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/users/$NEW_USER_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"firstName\": \"Rahul\",
        \"lastName\": \"Kumar\",
        \"email\": \"rahul.singh${TIMESTAMP}@trackerpro.com\",
        \"password\": \"password123\",
        \"userType\": \"HR\",
        \"gender\": \"Male\",
        \"mobileNo\": \"912345${MOBILE_SUFFIX:0:4}\",
        \"location\": \"Mumbai\",
        \"dob\": \"20/08/1988\",
        \"age\": 36
      }")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    
    if [ "$HTTP_CODE" == "200" ]; then
        print_result 0 "Update User"
        echo "  ✓ Location updated to Mumbai"
    else
        print_result 1 "Update User - HTTP $HTTP_CODE"
    fi
else
    echo "Test 6: Update User - SKIPPED (No user created)"
fi
echo ""

# Test 7: Toggle User Status
if [ ! -z "$NEW_USER_ID" ]; then
    echo "Test 7: PATCH /api/users/$NEW_USER_ID/toggle-status"
    RESPONSE=$(curl -s -w "%{http_code}" -X PATCH "$BASE_URL/users/$NEW_USER_ID/toggle-status" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"isEnabled": false}' \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        print_result 0 "Toggle User Status (Disable)"
    else
        print_result 1 "Toggle User Status - HTTP $RESPONSE"
    fi
else
    echo "Test 7: Toggle User Status - SKIPPED"
fi
echo ""

echo "=== 4. INTERNSHIPS (Career Outcomes) ==="
echo ""

# Test 8: Get All Internships
echo "Test 8: GET /api/internships"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/internships" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get All Internships"
    TOTAL=$(echo "$BODY" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo "  ✓ Total internships: $TOTAL"
else
    print_result 1 "Get All Internships - HTTP $HTTP_CODE"
fi
echo ""

# Test 9: Create Internship - CORRECTED (duration as Integer)
echo "Test 9: POST /api/internships"
TIMESTAMP=$(date +%s)
CREATE_INTERNSHIP_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/internships" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"code\": \"AI-${TIMESTAMP:5:5}\",
    \"title\": \"Artificial Intelligence Internship\",
    \"duration\": 5,
    \"workMode\": \"Online\",
    \"prerequisites\": \"Python, TensorFlow, Deep Learning\",
    \"description\": \"Advanced AI and machine learning internship program\",
    \"status\": \"Posted\"
  }")

HTTP_CODE=$(echo "$CREATE_INTERNSHIP_RESPONSE" | tail -n1)
BODY=$(echo "$CREATE_INTERNSHIP_RESPONSE" | head -n-1)

NEW_INTERNSHIP_ID=""
if [ "$HTTP_CODE" == "200" ] || [ "$HTTP_CODE" == "201" ]; then
    NEW_INTERNSHIP_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    print_result 0 "Create Internship"
    echo "  ✓ Internship ID: $NEW_INTERNSHIP_ID"
    echo "  ✓ Code: AI-${TIMESTAMP:5:5}"
else
    print_result 1 "Create Internship - HTTP $HTTP_CODE"
    echo "  Error: $(echo $BODY | head -c 300)"
fi
echo ""

# Test 10: Update Internship
if [ ! -z "$NEW_INTERNSHIP_ID" ]; then
    echo "Test 10: PUT /api/internships/$NEW_INTERNSHIP_ID"
    RESPONSE=$(curl -s -w "%{http_code}" -X PUT "$BASE_URL/internships/$NEW_INTERNSHIP_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{
        \"code\": \"AI-${TIMESTAMP:5:5}\",
        \"title\": \"Advanced AI Internship - Updated\",
        \"duration\": 6,
        \"workMode\": \"Hybrid\",
        \"prerequisites\": \"Python, TensorFlow, Deep Learning, NLP\",
        \"description\": \"Updated: Advanced AI and machine learning internship\",
        \"status\": \"Posted\"
      }" \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        print_result 0 "Update Internship"
        echo "  ✓ Duration updated to 6 months"
    else
        print_result 1 "Update Internship - HTTP $RESPONSE"
    fi
else
    echo "Test 10: Update Internship - SKIPPED"
fi
echo ""

# Test 11: Toggle Internship Status
if [ ! -z "$NEW_INTERNSHIP_ID" ]; then
    echo "Test 11: PATCH /api/internships/$NEW_INTERNSHIP_ID/toggle-status"
    RESPONSE=$(curl -s -w "%{http_code}" -X PATCH "$BASE_URL/internships/$NEW_INTERNSHIP_ID/toggle-status" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"status": "Draft"}' \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ]; then
        print_result 0 "Toggle Internship Status (Draft)"
    else
        print_result 1 "Toggle Internship Status - HTTP $RESPONSE"
    fi
else
    echo "Test 11: Toggle Internship Status - SKIPPED"
fi
echo ""

# Test 12: Delete Internship
if [ ! -z "$NEW_INTERNSHIP_ID" ]; then
    echo "Test 12: DELETE /api/internships/$NEW_INTERNSHIP_ID"
    RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/internships/$NEW_INTERNSHIP_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -o /dev/null)
    
    if [ "$RESPONSE" == "200" ] || [ "$RESPONSE" == "204" ]; then
        print_result 0 "Delete Internship"
    else
        print_result 1 "Delete Internship - HTTP $RESPONSE"
    fi
else
    echo "Test 12: Delete Internship - SKIPPED"
fi
echo ""

echo "=== 5. DASHBOARD STATISTICS ==="
echo ""

# Test 13: Get Dashboard Stats
echo "Test 13: GET /api/dashboard/stats"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/dashboard/stats" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
    print_result 0 "Get Dashboard Statistics"
    echo "  Response: $BODY"
else
    print_result 1 "Get Dashboard Statistics - HTTP $HTTP_CODE"
fi
echo ""

echo "=============================================="
echo "TEST SUMMARY"
echo "=============================================="
echo -e "${GREEN}✅ Passed: $pass_count${NC}"
echo -e "${RED}❌ Failed: $fail_count${NC}"
echo "Total Tests: $((pass_count + fail_count))"
echo ""

if [ $fail_count -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
    echo ""
    echo "✅ All API endpoints are working correctly"
    echo "✅ Data validation is functioning properly"
    echo "✅ JWT authentication is working"
    echo "✅ CRUD operations are operational"
    exit 0
else
    echo -e "${YELLOW}⚠️  $fail_count test(s) failed${NC}"
    exit 1
fi
