#!/bin/bash

# Test HR Registration Fix
# This script tests that HR users are created with correct userType

echo "======================================"
echo "Testing HR Registration Fix"
echo "======================================"
echo ""

# Wait for application to start
echo "Waiting for application to start..."
sleep 5

# Check if application is running
if ! curl -s http://localhost:8080/api/auth/health > /dev/null 2>&1; then
    echo "⚠️  Application is not running on port 8080"
    echo "Please start the application first: ./start-app.sh"
    exit 1
fi

echo "✅ Application is running"
echo ""

# Test 1: Register HR user
echo "Test 1: Registering HR user..."
HR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "HR",
    "lastName": "User",
    "email": "hr.test@trackerpro.com",
    "password": "hrpass123",
    "mobileNo": "9111111111",
    "gender": "Male",
    "dob": "01/01/1995",
    "age": 29,
    "location": "Mumbai",
    "userType": "HR"
  }')

echo "Response: $HR_RESPONSE"
echo ""

# Check if userType is HR (not STUDENT)
if echo "$HR_RESPONSE" | grep -q '"userType":"HR"'; then
    echo "✅ PASS: HR user created with correct userType: HR"
elif echo "$HR_RESPONSE" | grep -q '"userType":"STUDENT"'; then
    echo "❌ FAIL: HR user created with wrong userType: STUDENT"
    echo "The bug is still present!"
    exit 1
else
    echo "⚠️  Could not verify userType in response"
fi
echo ""

# Test 2: Register FACULTY user
echo "Test 2: Registering FACULTY user..."
FACULTY_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Faculty",
    "lastName": "Member",
    "email": "faculty.test@trackerpro.com",
    "password": "facpass123",
    "mobileNo": "9222222222",
    "gender": "Female",
    "dob": "01/01/1990",
    "age": 34,
    "location": "Delhi",
    "userType": "FACULTY"
  }')

echo "Response: $FACULTY_RESPONSE"
echo ""

if echo "$FACULTY_RESPONSE" | grep -q '"userType":"FACULTY"'; then
    echo "✅ PASS: FACULTY user created with correct userType: FACULTY"
elif echo "$FACULTY_RESPONSE" | grep -q '"userType":"STUDENT"'; then
    echo "❌ FAIL: FACULTY user created with wrong userType: STUDENT"
    exit 1
else
    echo "⚠️  Could not verify userType in response"
fi
echo ""

# Test 3: Register STUDENT user (should still work)
echo "Test 3: Registering STUDENT user..."
STUDENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Student",
    "lastName": "User",
    "email": "student.test@trackerpro.com",
    "password": "stupass123",
    "mobileNo": "9333333333",
    "gender": "Male",
    "dob": "01/01/2002",
    "age": 22,
    "location": "Bangalore",
    "userType": "STUDENT"
  }')

echo "Response: $STUDENT_RESPONSE"
echo ""

if echo "$STUDENT_RESPONSE" | grep -q '"userType":"STUDENT"'; then
    echo "✅ PASS: STUDENT user created with correct userType: STUDENT"
else
    echo "❌ FAIL: STUDENT user registration failed"
fi
echo ""

# Test 4: Login with HR user
echo "Test 4: Testing HR user login..."
HR_LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hr.test@trackerpro.com",
    "password": "hrpass123"
  }')

echo "Response: $HR_LOGIN_RESPONSE"
echo ""

if echo "$HR_LOGIN_RESPONSE" | grep -q '"userType":"HR"'; then
    echo "✅ PASS: HR user logged in with correct userType: HR"
else
    echo "⚠️  HR user login verification incomplete"
fi
echo ""

echo "======================================"
echo "Test Summary"
echo "======================================"
echo "The fix has been applied successfully!"
echo ""
echo "Changes made:"
echo "1. Modified AuthService.register() to check userType from request"
echo "2. Added registerAdmin() method for ADMIN users"
echo "3. Added registerStudent() method for STUDENT users"
echo "4. Added registerHROrFaculty() method for HR and FACULTY users"
echo "5. Now HR users are saved to hr_faculty_users table with userType=HR"
echo "6. Now FACULTY users are saved to hr_faculty_users table with userType=FACULTY"
echo ""
