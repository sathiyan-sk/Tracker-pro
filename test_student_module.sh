#!/bin/bash

# TrackerPro - Student Module Comprehensive Test Script
# This script tests all student module APIs

BASE_URL="http://localhost:8080/api"
ADMIN_TOKEN=""
STUDENT_TOKEN=""

echo "========================================="
echo "TrackerPro - Student Module API Tests"
echo "========================================="
echo ""

# Test 1: Admin Login
echo "1. Testing Admin Login..."
ADMIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@trackerpro.com",
    "password": "admin123"
  }')

echo "$ADMIN_RESPONSE" | jq '.'
ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token')
echo "Admin Token: ${ADMIN_TOKEN:0:50}..."
echo ""

# Test 2: Create Internship Posts (Admin)
echo "2. Creating Test Internship Posts..."

# Internship 1: Software Development
curl -s -X POST "${BASE_URL}/admin/career-posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "SDE-2025-001",
    "title": "Software Development Internship",
    "description": "Work on cutting-edge web applications using React and Spring Boot. Gain hands-on experience in full-stack development.",
    "prerequisites": "Java, Spring Boot, React, REST APIs",
    "duration": 6,
    "workMode": "Hybrid",
    "status": "Posted"
  }' | jq '.'

# Internship 2: Data Science
curl -s -X POST "${BASE_URL}/admin/career-posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "DS-2025-002",
    "title": "Data Science Internship",
    "description": "Analyze large datasets and build ML models. Work with Python, TensorFlow, and cloud platforms.",
    "prerequisites": "Python, Machine Learning, Statistics, SQL",
    "duration": 4,
    "workMode": "Remote",
    "status": "Posted"
  }' | jq '.'

# Internship 3: UI/UX Design
curl -s -X POST "${BASE_URL}/admin/career-posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "UX-2025-003",
    "title": "UI/UX Design Internship",
    "description": "Create beautiful and intuitive user interfaces. Work on design systems and user research.",
    "prerequisites": "Figma, Adobe XD, Prototyping, User Research",
    "duration": 3,
    "workMode": "Onsite",
    "status": "Posted"
  }' | jq '.'

# Internship 4: DevOps Engineering
curl -s -X POST "${BASE_URL}/admin/career-posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "DO-2025-004",
    "title": "DevOps Engineering Internship",
    "description": "Build and maintain CI/CD pipelines. Work with Docker, Kubernetes, AWS, and automation tools.",
    "prerequisites": "Linux, Docker, Kubernetes, Jenkins, AWS",
    "duration": 5,
    "workMode": "Hybrid",
    "status": "Posted"
  }' | jq '.'

# Internship 5: Mobile App Development
curl -s -X POST "${BASE_URL}/admin/career-posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "MAD-2025-005",
    "title": "Mobile App Development Internship",
    "description": "Develop cross-platform mobile applications using React Native. Build apps for iOS and Android.",
    "prerequisites": "React Native, JavaScript, Mobile UI/UX, REST APIs",
    "duration": 6,
    "workMode": "Remote",
    "status": "Posted"
  }' | jq '.'

echo "✅ 5 Internships Created"
echo ""

# Test 3: Register Student
echo "3. Registering Test Student..."
STUDENT_REG_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@student.com",
    "password": "student123",
    "mobileNo": "9876543210",
    "gender": "Male",
    "dob": "01/01/2002",
    "age": 23,
    "location": "Mumbai",
    "userType": "STUDENT"
  }')

echo "$STUDENT_REG_RESPONSE" | jq '.'
echo ""

# Test 4: Student Login
echo "4. Testing Student Login..."
STUDENT_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@student.com",
    "password": "student123"
  }')

echo "$STUDENT_LOGIN_RESPONSE" | jq '.'
STUDENT_TOKEN=$(echo "$STUDENT_LOGIN_RESPONSE" | jq -r '.token')
echo "Student Token: ${STUDENT_TOKEN:0:50}..."
echo ""

# Test 5: Get Student Profile
echo "5. Getting Student Profile..."
curl -s -X GET "${BASE_URL}/student/profile" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 6: Update Student Profile
echo "6. Updating Student Profile..."
curl -s -X PUT "${BASE_URL}/student/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -d '{
    "skills": "Java, Spring Boot, React, JavaScript, SQL",
    "bio": "Passionate software developer interested in full-stack development",
    "linkedinUrl": "https://linkedin.com/in/johndoe",
    "githubUrl": "https://github.com/johndoe"
  }' | jq '.'
echo ""

# Test 7: Get Available Internships
echo "7. Getting Available Internships..."
curl -s -X GET "${BASE_URL}/student/internships" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 8: Get Internship Details
echo "8. Getting Internship Details (ID: 1)..."
curl -s -X GET "${BASE_URL}/student/internships/1" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 9: Check If Applied
echo "9. Checking If Already Applied..."
curl -s -X GET "${BASE_URL}/student/internships/1/check-application" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 10: Apply for Internship
echo "10. Applying for Internship (SDE-2025-001)..."
curl -s -X POST "${BASE_URL}/student/applications" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -d '{
    "careerPostId": 1,
    "coverLetter": "I am very excited to apply for the Software Development Internship position. With my strong foundation in Java, Spring Boot, and React, I believe I can contribute effectively to your team. I have completed several projects demonstrating my full-stack development skills and am eager to learn and grow in a professional environment.",
    "resumeUrl": "",
    "availability": "Immediate",
    "additionalSkills": "Docker, Git, PostgreSQL, AWS basics",
    "expectedStipend": "₹20,000/month"
  }' | jq '.'
echo ""

# Test 11: Apply for Another Internship
echo "11. Applying for Another Internship (DS-2025-002)..."
curl -s -X POST "${BASE_URL}/student/applications" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -d '{
    "careerPostId": 2,
    "coverLetter": "I am passionate about data science and machine learning. My background in Python and statistics, combined with hands-on projects, makes me a great fit for this internship.",
    "resumeUrl": "",
    "availability": "Within 1 month",
    "additionalSkills": "Pandas, NumPy, Scikit-learn, Jupyter",
    "expectedStipend": "₹25,000/month"
  }' | jq '.'
echo ""

# Test 12: Get My Applications
echo "12. Getting My Applications..."
curl -s -X GET "${BASE_URL}/student/applications" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 13: Get Application Details
echo "13. Getting Application Details (ID: 1)..."
curl -s -X GET "${BASE_URL}/student/applications/1" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 14: Get Dashboard Stats
echo "14. Getting Dashboard Statistics..."
curl -s -X GET "${BASE_URL}/student/dashboard/stats" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 15: Get Notifications
echo "15. Getting Notifications..."
curl -s -X GET "${BASE_URL}/student/notifications" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 16: Filter Internships by Work Mode
echo "16. Filtering Internships (Remote only)..."
curl -s -X GET "${BASE_URL}/student/internships?workMode=Remote" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 17: Search Internships
echo "17. Searching Internships (keyword: 'Development')..."
curl -s -X GET "${BASE_URL}/student/internships?search=Development" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

# Test 18: Mark Notification as Read
echo "18. Marking Notification as Read (ID: 1)..."
curl -s -X PATCH "${BASE_URL}/student/notifications/1/read" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq '.'
echo ""

echo "========================================="
echo "✅ All Student Module Tests Completed!"
echo "========================================="
