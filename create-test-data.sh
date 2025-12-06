#!/bin/bash

# TrackerPro - Test Data Creation Script
# This script creates test users and applications for HR module testing

BASE_URL="http://localhost:8080/api"

echo "========================================="
echo "TrackerPro - Creating Test Data"
echo "========================================="

# Function to register a user (student or HR)
register_user() {
    local role=$1
    local email=$2
    local password=$3
    local firstName=$4
    local lastName=$5
    
    echo ""
    echo "📝 Registering $role: $email"
    
    response=$(curl -s -X POST "$BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$email\",
            \"password\": \"$password\",
            \"firstName\": \"$firstName\",
            \"lastName\": \"$lastName\",
            \"mobileNo\": \"9876543210\",
            \"role\": \"$role\"
        }")
    
    echo "$response" | jq '.'
}

# Function to login and get token
login_user() {
    local role=$1
    local email=$2
    local password=$3
    
    echo ""
    echo "🔐 Logging in as $role: $email"
    
    response=$(curl -s -X POST "$BASE_URL/auth/${role,,}/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$email\",
            \"password\": \"$password\"
        }")
    
    token=$(echo "$response" | jq -r '.data.token // empty')
    
    if [ -z "$token" ] || [ "$token" == "null" ]; then
        echo "❌ Login failed for $email"
        echo "$response" | jq '.'
        return 1
    else
        echo "✅ Login successful! Token obtained."
        echo "$token"
    fi
}

# Function to create internship post (admin only)
create_internship() {
    local token=$1
    local code=$2
    local title=$3
    local duration=$4
    local workMode=$5
    
    echo ""
    echo "💼 Creating internship: $title ($code)"
    
    response=$(curl -s -X POST "$BASE_URL/admin/career-posts" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "{
            \"code\": \"$code\",
            \"title\": \"$title\",
            \"duration\": $duration,
            \"workMode\": \"$workMode\",
            \"description\": \"This is a great opportunity to learn and grow.\",
            \"responsibilities\": \"Work on exciting projects\",
            \"requirements\": \"Basic programming knowledge\",
            \"location\": \"Bangalore, India\",
            \"stipend\": \"15000\",
            \"openings\": 5,
            \"skills\": \"Java, Spring Boot, React\",
            \"perks\": \"Certificate, LOR, Flexible hours\",
            \"startDate\": \"2025-01-15\",
            \"applicationDeadline\": \"2025-12-31\",
            \"status\": \"Posted\"
        }")
    
    echo "$response" | jq '.'
}

# Function to apply for internship (student only)
apply_for_internship() {
    local token=$1
    local careerPostId=$2
    local studentName=$3
    
    echo ""
    echo "📄 $studentName applying for internship ID: $careerPostId"
    
    response=$(curl -s -X POST "$BASE_URL/student/applications" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "{
            \"careerPostId\": $careerPostId,
            \"coverLetter\": \"I am very interested in this position and I believe I would be a great fit.\",
            \"additionalSkills\": \"Python, JavaScript, Git\",
            \"availability\": \"Immediate\",
            \"expectedStipend\": \"15000\"
        }")
    
    echo "$response" | jq '.'
}

# ========================================
# STEP 1: Register Admin
# ========================================
register_user "ADMIN" "admin@trackerpro.com" "admin123" "Admin" "User"

# ========================================
# STEP 2: Register HR User
# ========================================
register_user "HR" "hr@trackerpro.com" "hr123456" "HR" "Manager"

# ========================================
# STEP 3: Register Students
# ========================================
register_user "STUDENT" "john.doe@student.com" "student123" "John" "Doe"
register_user "STUDENT" "jane.smith@student.com" "student123" "Jane" "Smith"
register_user "STUDENT" "raj.kumar@student.com" "student123" "Raj" "Kumar"
register_user "STUDENT" "priya.sharma@student.com" "student123" "Priya" "Sharma"
register_user "STUDENT" "amit.patel@student.com" "student123" "Amit" "Patel"

sleep 2

# ========================================
# STEP 4: Login as Admin and Create Internship Posts
# ========================================
ADMIN_TOKEN=$(login_user "ADMIN" "admin@trackerpro.com" "admin123")

if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
    create_internship "$ADMIN_TOKEN" "INT001" "Software Development Intern" 6 "Hybrid"
    sleep 1
    create_internship "$ADMIN_TOKEN" "INT002" "Data Science Intern" 3 "Remote"
    sleep 1
    create_internship "$ADMIN_TOKEN" "INT003" "UI/UX Design Intern" 4 "Onsite"
fi

sleep 2

# ========================================
# STEP 5: Students Apply for Internships
# ========================================

# John Doe applies for INT001 and INT002
JOHN_TOKEN=$(login_user "STUDENT" "john.doe@student.com" "student123")
if [ -n "$JOHN_TOKEN" ] && [ "$JOHN_TOKEN" != "null" ]; then
    apply_for_internship "$JOHN_TOKEN" 1 "John Doe"
    sleep 1
    apply_for_internship "$JOHN_TOKEN" 2 "John Doe"
fi

# Jane Smith applies for INT001
JANE_TOKEN=$(login_user "STUDENT" "jane.smith@student.com" "student123")
if [ -n "$JANE_TOKEN" ] && [ "$JANE_TOKEN" != "null" ]; then
    apply_for_internship "$JANE_TOKEN" 1 "Jane Smith"
fi

# Raj Kumar applies for INT002 and INT003
RAJ_TOKEN=$(login_user "STUDENT" "raj.kumar@student.com" "student123")
if [ -n "$RAJ_TOKEN" ] && [ "$RAJ_TOKEN" != "null" ]; then
    apply_for_internship "$RAJ_TOKEN" 2 "Raj Kumar"
    sleep 1
    apply_for_internship "$RAJ_TOKEN" 3 "Raj Kumar"
fi

# Priya Sharma applies for INT001
PRIYA_TOKEN=$(login_user "STUDENT" "priya.sharma@student.com" "student123")
if [ -n "$PRIYA_TOKEN" ] && [ "$PRIYA_TOKEN" != "null" ]; then
    apply_for_internship "$PRIYA_TOKEN" 1 "Priya Sharma"
fi

# Amit Patel applies for INT003
AMIT_TOKEN=$(login_user "STUDENT" "amit.patel@student.com" "student123")
if [ -n "$AMIT_TOKEN" ] && [ "$AMIT_TOKEN" != "null" ]; then
    apply_for_internship "$AMIT_TOKEN" 3 "Amit Patel"
fi

echo ""
echo "========================================="
echo "✅ Test Data Creation Complete!"
echo "========================================="
echo ""
echo "📊 Summary:"
echo "  - 1 Admin user created"
echo "  - 1 HR user created"
echo "  - 5 Student users created"
echo "  - 3 Internship posts created"
echo "  - 6 Applications submitted"
echo ""
echo "🔑 Login Credentials:"
echo ""
echo "  Admin:"
echo "    Email: admin@trackerpro.com"
echo "    Password: admin123"
echo ""
echo "  HR Manager:"
echo "    Email: hr@trackerpro.com"
echo "    Password: hr123456"
echo ""
echo "  Students:"
echo "    - john.doe@student.com / student123"
echo "    - jane.smith@student.com / student123"
echo "    - raj.kumar@student.com / student123"
echo "    - priya.sharma@student.com / student123"
echo "    - amit.patel@student.com / student123"
echo ""
echo "🌐 Access URLs:"
echo "  - Landing: http://localhost/index.html"
echo "  - Login: http://localhost/loginPage.html"
echo "  - HR Page: http://localhost/hrPage.html"
echo ""
echo "========================================="
