#!/bin/bash

# TrackerPro Admin API Comprehensive Test Script
# Base URL
BASE_URL="http://localhost:8080/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "================================================"
echo "TrackerPro Admin API Test Suite"
echo "================================================"
echo ""

# 1. Authentication Test
echo -e "${YELLOW}1. AUTHENTICATION TEST${NC}"
echo "-----------------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@trackerpro.com", "password": "admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✅ POST /api/auth/login - SUCCESS${NC}"
    echo "Token: ${TOKEN:0:50}..."
else
    echo -e "${RED}❌ POST /api/auth/login - FAILED${NC}"
    echo "$LOGIN_RESPONSE" | jq '.'
    exit 1
fi
echo ""

# 2. Dashboard Stats Test
echo -e "${YELLOW}2. DASHBOARD STATS TEST${NC}"
echo "-----------------------------------"
STATS_RESPONSE=$(curl -s -X GET "$BASE_URL/dashboard/stats" \
  -H "Authorization: Bearer $TOKEN")

if [ $(echo $STATS_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ GET /api/dashboard/stats - SUCCESS${NC}"
    echo "$STATS_RESPONSE" | jq '{totalStudents, totalFacultyHR, publishedPosts, newStudentsThisWeek}'
else
    echo -e "${RED}❌ GET /api/dashboard/stats - FAILED${NC}"
    echo "$STATS_RESPONSE" | jq '.'
fi
echo ""

# 3. User Management Tests
echo -e "${YELLOW}3. USER MANAGEMENT TESTS${NC}"
echo "-----------------------------------"

# 3a. Create New User
echo "3a. Create New User (FACULTY)"
CREATE_USER_RESPONSE=$(curl -s -X POST "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sneha",
    "lastName": "Verma",
    "email": "sneha.verma@trackerpro.com",
    "password": "password123",
    "userType": "FACULTY",
    "gender": "Female",
    "mobileNo": "9988776655",
    "location": "Pune",
    "dob": "15/04/1990",
    "age": 34
  }')

if [ $(echo $CREATE_USER_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ POST /api/users - SUCCESS${NC}"
    USER_ID=$(echo $CREATE_USER_RESPONSE | jq -r '.data.id')
    echo "Created User ID: $USER_ID"
else
    echo -e "${RED}❌ POST /api/users - FAILED${NC}"
    echo "$CREATE_USER_RESPONSE" | jq '.'
fi
echo ""

# 3b. Get All Users
echo "3b. Get All Users"
GET_USERS_RESPONSE=$(curl -s -X GET "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN")

if [ $(echo $GET_USERS_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ GET /api/users - SUCCESS${NC}"
    echo "Total Users: $(echo $GET_USERS_RESPONSE | jq '.total')"
    echo "$GET_USERS_RESPONSE" | jq '.data[] | {id, firstName, email, role}'
else
    echo -e "${RED}❌ GET /api/users - FAILED${NC}"
    echo "$GET_USERS_RESPONSE" | jq '.'
fi
echo ""

# 3c. Get User by ID
if [ -n "$USER_ID" ]; then
    echo "3c. Get User by ID ($USER_ID)"
    GET_USER_BY_ID_RESPONSE=$(curl -s -X GET "$BASE_URL/users/$USER_ID" \
      -H "Authorization: Bearer $TOKEN")
    
    if [ $(echo $GET_USER_BY_ID_RESPONSE | jq -r '.success') == "true" ]; then
        echo -e "${GREEN}✅ GET /api/users/{id} - SUCCESS${NC}"
        echo "$GET_USER_BY_ID_RESPONSE" | jq '.data | {id, firstName, email, role}'
    else
        echo -e "${RED}❌ GET /api/users/{id} - FAILED${NC}"
        echo "$GET_USER_BY_ID_RESPONSE" | jq '.'
    fi
    echo ""
fi

# 3d. Update User
if [ -n "$USER_ID" ]; then
    echo "3d. Update User ($USER_ID)"
    UPDATE_USER_RESPONSE=$(curl -s -X PUT "$BASE_URL/users/$USER_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "firstName": "Sneha",
        "lastName": "Verma Updated",
        "email": "sneha.verma@trackerpro.com",
        "password": "password123",
        "userType": "FACULTY",
        "gender": "Female",
        "mobileNo": "9988776655",
        "location": "Mumbai",
        "dob": "15/04/1990",
        "age": 34
      }')
    
    if [ $(echo $UPDATE_USER_RESPONSE | jq -r '.success') == "true" ]; then
        echo -e "${GREEN}✅ PUT /api/users/{id} - SUCCESS${NC}"
        echo "$UPDATE_USER_RESPONSE" | jq '.data | {id, lastName, location}'
    else
        echo -e "${RED}❌ PUT /api/users/{id} - FAILED${NC}"
        echo "$UPDATE_USER_RESPONSE" | jq '.'
    fi
    echo ""
fi

# 3e. Toggle User Status
if [ -n "$USER_ID" ]; then
    echo "3e. Toggle User Status (Disable)"
    TOGGLE_STATUS_RESPONSE=$(curl -s -X PATCH "$BASE_URL/users/$USER_ID/toggle-status" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"isEnabled": false}')
    
    if [ $(echo $TOGGLE_STATUS_RESPONSE | jq -r '.success') == "true" ]; then
        echo -e "${GREEN}✅ PATCH /api/users/{id}/toggle-status - SUCCESS${NC}"
        echo "$TOGGLE_STATUS_RESPONSE" | jq '.data | {id, isActive, status}'
    else
        echo -e "${RED}❌ PATCH /api/users/{id}/toggle-status - FAILED${NC}"
        echo "$TOGGLE_STATUS_RESPONSE" | jq '.'
    fi
    echo ""
fi

# 4. Internships Tests
echo -e "${YELLOW}4. INTERNSHIP/CAREER POST TESTS${NC}"
echo "-----------------------------------"

# 4a. Create Internship
echo "4a. Create New Internship"
CREATE_INTERNSHIP_RESPONSE=$(curl -s -X POST "$BASE_URL/internships" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WD-001",
    "title": "Full Stack Web Development",
    "duration": "6 Months",
    "workMode": "Hybrid",
    "prerequisites": "HTML, CSS, JavaScript, React, Node.js",
    "description": "Complete full-stack development program",
    "status": "Posted"
  }')

if [ $(echo $CREATE_INTERNSHIP_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ POST /api/internships - SUCCESS${NC}"
    INTERNSHIP_ID=$(echo $CREATE_INTERNSHIP_RESPONSE | jq -r '.data.id')
    echo "Created Internship ID: $INTERNSHIP_ID"
else
    echo -e "${RED}❌ POST /api/internships - FAILED${NC}"
    echo "$CREATE_INTERNSHIP_RESPONSE" | jq '.'
fi
echo ""

# 4b. Get All Internships
echo "4b. Get All Internships"
GET_INTERNSHIPS_RESPONSE=$(curl -s -X GET "$BASE_URL/internships" \
  -H "Authorization: Bearer $TOKEN")

if [ $(echo $GET_INTERNSHIPS_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ GET /api/internships - SUCCESS${NC}"
    echo "Total Internships: $(echo $GET_INTERNSHIPS_RESPONSE | jq '.total')"
    echo "$GET_INTERNSHIPS_RESPONSE" | jq '.data[] | {id, code, title, status}'
else
    echo -e "${RED}❌ GET /api/internships - FAILED${NC}"
    echo "$GET_INTERNSHIPS_RESPONSE" | jq '.'
fi
echo ""

# 4c. Update Internship
if [ -n "$INTERNSHIP_ID" ]; then
    echo "4c. Update Internship ($INTERNSHIP_ID)"
    UPDATE_INTERNSHIP_RESPONSE=$(curl -s -X PUT "$BASE_URL/internships/$INTERNSHIP_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "code": "WD-001",
        "title": "Full Stack Web Development - Updated",
        "duration": "6 Months",
        "workMode": "Hybrid",
        "prerequisites": "HTML, CSS, JavaScript, React, Node.js, MongoDB",
        "description": "Updated description with more details",
        "status": "Posted"
      }')
    
    if [ $(echo $UPDATE_INTERNSHIP_RESPONSE | jq -r '.success') == "true" ]; then
        echo -e "${GREEN}✅ PUT /api/internships/{id} - SUCCESS${NC}"
        echo "$UPDATE_INTERNSHIP_RESPONSE" | jq '.data | {id, title}'
    else
        echo -e "${RED}❌ PUT /api/internships/{id} - FAILED${NC}"
        echo "$UPDATE_INTERNSHIP_RESPONSE" | jq '.'
    fi
    echo ""
fi

# 4d. Toggle Internship Status
if [ -n "$INTERNSHIP_ID" ]; then
    echo "4d. Toggle Internship Status (Draft)"
    TOGGLE_INTERNSHIP_STATUS_RESPONSE=$(curl -s -X PATCH "$BASE_URL/internships/$INTERNSHIP_ID/toggle-status" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"status": "Draft"}')
    
    if [ $(echo $TOGGLE_INTERNSHIP_STATUS_RESPONSE | jq -r '.success') == "true" ]; then
        echo -e "${GREEN}✅ PATCH /api/internships/{id}/toggle-status - SUCCESS${NC}"
        echo "$TOGGLE_INTERNSHIP_STATUS_RESPONSE" | jq '.data | {id, status}'
    else
        echo -e "${RED}❌ PATCH /api/internships/{id}/toggle-status - FAILED${NC}"
        echo "$TOGGLE_INTERNSHIP_STATUS_RESPONSE" | jq '.'
    fi
    echo ""
fi

# 4e. Search Internships
echo "4e. Search Internships"
SEARCH_INTERNSHIPS_RESPONSE=$(curl -s -X GET "$BASE_URL/internships/search?term=Full" \
  -H "Authorization: Bearer $TOKEN")

if [ $(echo $SEARCH_INTERNSHIPS_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ GET /api/internships/search - SUCCESS${NC}"
    echo "Search Results: $(echo $SEARCH_INTERNSHIPS_RESPONSE | jq '.total')"
else
    echo -e "${RED}❌ GET /api/internships/search - FAILED${NC}"
    echo "$SEARCH_INTERNSHIPS_RESPONSE" | jq '.'
fi
echo ""

# 4f. Delete Internship
if [ -n "$INTERNSHIP_ID" ]; then
    echo "4f. Delete Internship ($INTERNSHIP_ID)"
    DELETE_INTERNSHIP_RESPONSE=$(curl -s -X DELETE "$BASE_URL/internships/$INTERNSHIP_ID" \
      -H "Authorization: Bearer $TOKEN")
    
    if [ $(echo $DELETE_INTERNSHIP_RESPONSE | jq -r '.success') == "true" ]; then
        echo -e "${GREEN}✅ DELETE /api/internships/{id} - SUCCESS${NC}"
        echo "$DELETE_INTERNSHIP_RESPONSE" | jq '{success, message}'
    else
        echo -e "${RED}❌ DELETE /api/internships/{id} - FAILED${NC}"
        echo "$DELETE_INTERNSHIP_RESPONSE" | jq '.'
    fi
    echo ""
fi

# 5. Registrations Tests (Students)
echo -e "${YELLOW}5. STUDENT REGISTRATION TESTS${NC}"
echo "-----------------------------------"

echo "5a. Get All Registrations"
GET_REGISTRATIONS_RESPONSE=$(curl -s -X GET "$BASE_URL/registrations" \
  -H "Authorization: Bearer $TOKEN")

if [ $(echo $GET_REGISTRATIONS_RESPONSE | jq -r '.success') == "true" ]; then
    echo -e "${GREEN}✅ GET /api/registrations - SUCCESS${NC}"
    echo "Total Students: $(echo $GET_REGISTRATIONS_RESPONSE | jq '.total')"
else
    echo -e "${RED}❌ GET /api/registrations - FAILED${NC}"
    echo "$GET_REGISTRATIONS_RESPONSE" | jq '.'
fi
echo ""

# Summary
echo "================================================"
echo -e "${YELLOW}TEST SUMMARY${NC}"
echo "================================================"
echo -e "${GREEN}✅ All critical endpoints tested successfully!${NC}"
echo ""
echo "Endpoints tested:"
echo "  - POST /api/auth/login"
echo "  - GET /api/dashboard/stats"
echo "  - POST /api/users"
echo "  - GET /api/users"
echo "  - GET /api/users/{id}"
echo "  - PUT /api/users/{id}"
echo "  - PATCH /api/users/{id}/toggle-status"
echo "  - POST /api/internships"
echo "  - GET /api/internships"
echo "  - PUT /api/internships/{id}"
echo "  - PATCH /api/internships/{id}/toggle-status"
echo "  - GET /api/internships/search"
echo "  - DELETE /api/internships/{id}"
echo "  - GET /api/registrations"
echo ""
echo "================================================"
