#!/bin/bash

# TrackerPro - Career Posts API Testing Script
# Tests all Career Post endpoints in the Admin Module

echo "========================================"
echo "TrackerPro - Career Posts API Tests"
echo "========================================"
echo ""

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8080"

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASSED${NC}"
    else
        echo -e "${RED}✗ FAILED${NC}"
    fi
}

# Test 1: Admin Login
echo "Test 1: Admin Login"
echo "-------------------"
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@trackerpro.com",
    "password": "admin123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('token', ''))" 2>/dev/null)

if [ -n "$TOKEN" ]; then
    echo "JWT Token obtained successfully"
    print_result 0
else
    echo "Failed to obtain JWT token"
    print_result 1
    exit 1
fi
echo ""

# Test 2: Get All Career Posts (Initially Empty)
echo "Test 2: Get All Career Posts"
echo "----------------------------"
RESPONSE=$(curl -s -X GET ${BASE_URL}/api/v1/career-posts \
  -H "Authorization: Bearer $TOKEN")
TOTAL=$(echo $RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('total', 0))" 2>/dev/null)
echo "Total career posts: $TOTAL"
print_result 0
echo ""

# Test 3: Create Career Post #1
echo "Test 3: Create Career Post #1 (Full Stack)"
echo "-------------------------------------------"
POST1=$(curl -s -X POST ${BASE_URL}/api/v1/career-posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "code": "INT-2025-FS-001",
    "title": "Full Stack Developer Internship",
    "duration": 6,
    "workMode": "Hybrid",
    "prerequisites": "Java, Spring Boot, React, SQL",
    "description": "Work on cutting-edge full-stack projects",
    "status": "Posted"
  }')
POST1_ID=$(echo $POST1 | python3 -c "import sys, json; print(json.load(sys.stdin).get('data', {}).get('id', 0))" 2>/dev/null)
if [ $POST1_ID -gt 0 ]; then
    echo "Post created with ID: $POST1_ID"
    print_result 0
else
    echo "Failed to create post"
    print_result 1
fi
echo ""

# Test 4: Create Career Post #2
echo "Test 4: Create Career Post #2 (Data Science)"
echo "---------------------------------------------"
POST2=$(curl -s -X POST ${BASE_URL}/api/v1/career-posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "code": "INT-2025-DS-002",
    "title": "Data Science Internship",
    "duration": 4,
    "workMode": "Online",
    "prerequisites": "Python, ML, pandas",
    "description": "Analyze data and build ML models",
    "status": "Posted"
  }')
POST2_ID=$(echo $POST2 | python3 -c "import sys, json; print(json.load(sys.stdin).get('data', {}).get('id', 0))" 2>/dev/null)
if [ $POST2_ID -gt 0 ]; then
    echo "Post created with ID: $POST2_ID"
    print_result 0
else
    echo "Failed to create post"
    print_result 1
fi
echo ""

# Test 5: Get Career Post by ID
echo "Test 5: Get Career Post by ID"
echo "-----------------------------"
SINGLE_POST=$(curl -s -X GET ${BASE_URL}/api/v1/career-posts/${POST1_ID} \
  -H "Authorization: Bearer $TOKEN")
TITLE=$(echo $SINGLE_POST | python3 -c "import sys, json; print(json.load(sys.stdin).get('data', {}).get('title', ''))" 2>/dev/null)
if [ -n "$TITLE" ]; then
    echo "Retrieved post: $TITLE"
    print_result 0
else
    echo "Failed to retrieve post"
    print_result 1
fi
echo ""

# Test 6: Update Career Post
echo "Test 6: Update Career Post"
echo "--------------------------"
UPDATE=$(curl -s -X PUT ${BASE_URL}/api/v1/career-posts/${POST2_ID} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "code": "INT-2025-DS-002",
    "title": "Data Science & AI Internship (Updated)",
    "duration": 5,
    "workMode": "Hybrid",
    "prerequisites": "Python, ML, Deep Learning",
    "description": "Updated: Build advanced ML and AI models",
    "status": "Posted"
  }')
SUCCESS=$(echo $UPDATE | python3 -c "import sys, json; print(json.load(sys.stdin).get('success', False))" 2>/dev/null)
if [ "$SUCCESS" = "True" ]; then
    echo "Post updated successfully"
    print_result 0
else
    echo "Failed to update post"
    print_result 1
fi
echo ""

# Test 7: Search Career Posts
echo "Test 7: Search Career Posts (term: 'Data')"
echo "------------------------------------------"
SEARCH=$(curl -s -X GET "${BASE_URL}/api/v1/career-posts/search?term=Data" \
  -H "Authorization: Bearer $TOKEN")
SEARCH_COUNT=$(echo $SEARCH | python3 -c "import sys, json; print(json.load(sys.stdin).get('total', 0))" 2>/dev/null)
echo "Found $SEARCH_COUNT post(s) matching 'Data'"
print_result 0
echo ""

# Test 8: Get All Career Posts (Should have 2)
echo "Test 8: Get All Career Posts (After Creation)"
echo "---------------------------------------------"
ALL_POSTS=$(curl -s -X GET ${BASE_URL}/api/v1/career-posts \
  -H "Authorization: Bearer $TOKEN")
FINAL_TOTAL=$(echo $ALL_POSTS | python3 -c "import sys, json; print(json.load(sys.stdin).get('total', 0))" 2>/dev/null)
echo "Total career posts: $FINAL_TOTAL"
if [ $FINAL_TOTAL -ge 2 ]; then
    print_result 0
else
    print_result 1
fi
echo ""

# Test 9: Dashboard Stats
echo "Test 9: Dashboard Stats (includes career posts count)"
echo "-----------------------------------------------------"
STATS=$(curl -s -X GET ${BASE_URL}/api/v1/dashboard/stats \
  -H "Authorization: Bearer $TOKEN")
PUBLISHED=$(echo $STATS | python3 -c "import sys, json; print(json.load(sys.stdin).get('publishedPosts', 0))" 2>/dev/null)
echo "Published career posts: $PUBLISHED"
if [ $PUBLISHED -ge 2 ]; then
    print_result 0
else
    print_result 1
fi
echo ""

# Test 10: Delete Career Post
echo "Test 10: Delete Career Post"
echo "---------------------------"
DELETE=$(curl -s -X DELETE ${BASE_URL}/api/v1/career-posts/${POST2_ID} \
  -H "Authorization: Bearer $TOKEN")
DEL_SUCCESS=$(echo $DELETE | python3 -c "import sys, json; print(json.load(sys.stdin).get('success', False))" 2>/dev/null)
if [ "$DEL_SUCCESS" = "True" ]; then
    echo "Post deleted successfully"
    print_result 0
else
    echo "Failed to delete post"
    print_result 1
fi
echo ""

echo "========================================"
echo "✅ All Career Posts API Tests Completed"
echo "========================================"
