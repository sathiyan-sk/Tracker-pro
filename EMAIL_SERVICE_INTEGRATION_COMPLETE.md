# Email Service Integration - Complete Implementation

## Overview
This document details the complete email service integration for the TrackerPro HR module, including backend services, API endpoints, and frontend integration.

## ✅ Components Implemented

### 1. Backend Services

#### **EmailService.java** (`/app/src/main/java/com/webapp/Tracker_pro/service/EmailService.java`)
- **Purpose**: Core email sending service using Spring Boot Mail
- **Features**:
  - Send simple text emails
  - Send HTML formatted emails
  - Bulk email sending (async)
  - Interview invitation emails with HTML templates
  - Application status emails (rejection/acceptance)
- **Configuration**: Uses Gmail SMTP settings from `application.yaml`

#### **EmailController.java** (`/app/src/main/java/com/webapp/Tracker_pro/controller/EmailController.java`)
- **Purpose**: REST API endpoints for email operations
- **Endpoints**:
  - `POST /api/hr/emails/send-bulk` - Send bulk emails to multiple candidates
  - `POST /api/hr/emails/send-interview-invite` - Send interview invitations
  - `POST /api/hr/emails/test` - Test email configuration
- **Security**: Requires HR or ADMIN role (`@PreAuthorize("hasAnyRole('HR', 'ADMIN')")`)

### 2. Data Transfer Objects (DTOs)

#### **SendBulkEmailRequest.java** (`/app/src/main/java/com/webapp/Tracker_pro/dto/SendBulkEmailRequest.java`)
```java
{
    "recipients": [
        {
            "email": "candidate@example.com",
            "name": "Candidate Name"
        }
    ],
    "subject": "Email Subject",
    "body": "Email Body"
}
```

#### **SendInterviewInviteRequest.java** (`/app/src/main/java/com/webapp/Tracker_pro/dto/SendInterviewInviteRequest.java`)
```java
{
    "candidateEmail": "candidate@example.com",
    "candidateName": "Candidate Name",
    "interviewDate": "2025-01-15",
    "interviewTime": "10:00 AM - 11:00 AM",
    "interviewMode": "Online",
    "meetingLink": "https://meet.google.com/xxx-xxxx-xxx",
    "hrManagerName": "HR Manager Name"
}
```

### 3. Frontend Integration

#### **hrpage-api.js** Updates
Added new `HREmailAPI` object with methods:
- `sendBulkEmails(recipients, subject, body)` - Send bulk emails
- `sendInterviewInvite(inviteData)` - Send interview invitation
- `sendTestEmail(email)` - Test email configuration

#### **hrPage.html** Updates
1. **Updated `sendEmailTemplate()` function** (Line ~2356):
   - Now makes actual API call to send emails
   - Shows success/error messages
   - Handles response properly

2. **Updated `sendInvite()` function** (Line ~2967):
   - Now makes actual API call to send interview invitations
   - Sends complete interview details including meeting link
   - Shows success/error messages

### 4. Email Configuration

#### **application.yaml** (Already Configured)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: sathiyan.pm.inzoho@gmail.com
    password: gdgkpozyyhxbvbdn
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

app:
  mail:
    from: sathiyan.pm.inzoho@gmail.com
    from-name: TrackerPro HR Team
```

## 🔐 JWT Authentication Configuration

### JWT Secret Verification
- **Location**: `application.yaml` (Line 36-38)
- **Secret Key**: `3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b`
- **Expiration**: 86400000 ms (24 hours)

### Authentication Flow
1. **Login**: User logs in via `/api/auth/login`
2. **Token Storage**: JWT token stored in `localStorage` as 'authToken'
3. **HR Page Access**: 
   - `hrPage.html` checks for token on load (Line 1298-1318)
   - Verifies user has HR or ADMIN role
   - Redirects to login if not authenticated

4. **API Calls**:
   - `hrpage-api.js` adds `Authorization: Bearer <token>` header to all requests (Line 99)
   - Backend validates token via `JwtAuthenticationFilter` (Line 39-82)

### JWT Components
- **JwtService.java**: Generates and validates tokens
- **JwtAuthenticationFilter.java**: Intercepts requests and validates JWT
- **SecurityConfig.java**: Configures JWT-based authentication

## 📧 Email Features Available in HR Page

### 1. Bulk Email Sending
**Location**: Shortlisted Candidates Page → Select candidates → Send Email button

**Features**:
- Rejection email template
- Acceptance email template
- Custom subject and body editing
- Personalized emails (replaces {candidateName} placeholder)
- Bulk sending to multiple candidates

**Usage**:
1. Select candidates from shortlisted list
2. Click "✉️ Send Email" button
3. Choose template (Rejection/Acceptance)
4. Edit subject and body if needed
5. Click "Send Emails"

### 2. Interview Invitation Emails
**Location**: Interviews Page → Scheduled Interviews → Send Invite button

**Features**:
- Professional HTML formatted email
- Includes interview date, time, and mode
- Meeting link for online interviews
- HR manager name
- Auto-generated from interview details

**Usage**:
1. Schedule an interview for a candidate
2. Click "Send Invite" button
3. Email automatically sent with all interview details

## 🚀 How to Run and Test

### 1. Start the Application
```bash
cd /app
./run-app.sh
```

This will:
- Set JAVA_HOME to Java 17
- Start Spring Boot application with Maven
- Application runs on http://localhost:8080

### 2. Test Email Configuration
You can test the email setup using the test endpoint:

```bash
curl -X POST "http://localhost:8080/api/hr/emails/test?email=YOUR_EMAIL@example.com" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Access HR Page
1. Navigate to: http://localhost:8080/loginPage.html
2. Login with HR credentials
3. Access HR Dashboard: http://localhost:8080/hrPage.html

### 4. Test Email Features

#### Test Bulk Email:
1. Go to "Shortlisted" page
2. Select one or more candidates
3. Click "✉️ Send Email"
4. Choose template and click "Send Emails"
5. Check backend logs for email sending confirmation

#### Test Interview Invitation:
1. Go to "Interviews" page
2. Schedule an interview for a candidate
3. Click "Send Invite" button
4. Check backend logs for email sending confirmation

## 📊 Database and Dependencies

### Required Dependencies (Already in pom.xml)
```xml
<!-- Spring Boot Mail Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### No Database Changes Required
- Email service doesn't require database tables
- All email operations are stateless

## 🔍 Debugging and Logs

### Check Email Sending Logs
```bash
# View backend logs
tail -f /app/logs/application.log

# Or if using console output:
# Look for lines starting with:
# - "HTML email sent successfully to: <email>"
# - "Interview invitation sent to: <email>"
# - "Bulk email operation completed: X/Y emails sent successfully"
```

### Common Issues and Solutions

#### 1. **Emails Not Sending**
**Check**:
- Gmail App Password is correct in application.yaml
- SMTP ports (587) are not blocked
- "Less secure app access" is enabled (if using regular password)
- Check backend logs for detailed error messages

**Solution**: Use Gmail App Password instead of regular password:
1. Go to Google Account → Security
2. Enable 2-Factor Authentication
3. Generate App Password
4. Use that in application.yaml

#### 2. **JWT Authentication Issues**
**Check**:
- Token exists in localStorage (Open browser DevTools → Application → Local Storage)
- Token format is correct (3 parts separated by dots)
- Token not expired (24-hour expiration)

**Solution**: Clear localStorage and login again

#### 3. **Authorization Header Not Sent**
**Check**:
- Browser console for CORS errors
- Network tab in DevTools to verify Authorization header is present

**Solution**: Already handled in SecurityConfig.java (CORS enabled)

## 📝 API Documentation

### Send Bulk Emails
```
POST /api/hr/emails/send-bulk
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
    "recipients": [
        {"email": "candidate1@example.com", "name": "Candidate 1"},
        {"email": "candidate2@example.com", "name": "Candidate 2"}
    ],
    "subject": "Application Status Update",
    "body": "Dear {candidateName},\n\nThank you for applying..."
}

Response:
{
    "success": true,
    "message": "Emails sent successfully to 2/2 recipients",
    "totalRecipients": 2,
    "successCount": 2,
    "failedCount": 0
}
```

### Send Interview Invitation
```
POST /api/hr/emails/send-interview-invite
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
    "candidateEmail": "candidate@example.com",
    "candidateName": "John Doe",
    "interviewDate": "2025-01-15",
    "interviewTime": "10:00 AM - 11:00 AM",
    "interviewMode": "Online",
    "meetingLink": "https://meet.google.com/xxx-xxxx-xxx",
    "hrManagerName": "Jane Smith"
}

Response:
{
    "success": true,
    "message": "Interview invitation sent successfully to John Doe"
}
```

## ✅ Testing Checklist

- [ ] Application starts without errors
- [ ] Can login with HR credentials
- [ ] HR page loads correctly with authentication
- [ ] Can access shortlisted candidates page
- [ ] Can select candidates and open email modal
- [ ] Email templates load correctly
- [ ] Can send bulk emails to selected candidates
- [ ] Can schedule an interview
- [ ] Can send interview invitation
- [ ] Emails received in candidate inbox
- [ ] Check backend logs for success messages
- [ ] Verify JWT authentication works for API calls

## 🎯 Summary

All email integration features have been successfully implemented:

✅ **Backend**:
- EmailService with Gmail SMTP integration
- Email Controller with REST API endpoints
- DTOs for email requests
- JWT authentication for secure access

✅ **Frontend**:
- Updated hrpage-api.js with email API methods
- Updated hrPage.html to use email APIs
- Proper error handling and user feedback

✅ **Configuration**:
- Gmail SMTP configured in application.yaml
- JWT authentication properly configured
- Security settings allow authenticated HR users

✅ **Features**:
- Bulk email sending (rejection/acceptance)
- Interview invitation emails
- HTML formatted professional emails
- Personalized email content

The system is ready for testing and deployment!
