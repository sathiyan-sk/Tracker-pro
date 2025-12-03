# TrackerPro - Detailed Implementation Plan for Student & HR Modules

**Date:** December 3, 2025  
**Status:** Ready for Implementation  
**Priority:** Student Module → HR Module

---

## 🎯 PHASE 2: STUDENT MODULE - DETAILED IMPLEMENTATION PLAN

### Module Overview:
A comprehensive student portal where registered students can:
- Log in with their credentials
- Browse available internship postings
- Apply for internships with resume submission
- Track application status in real-time
- Manage their profile and documents

---

## PART A: BACKEND DEVELOPMENT

### 1. Database Schema Design

#### New Table: `applications`
```sql
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    career_post_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',  -- Pending, Under Review, Shortlisted, Accepted, Rejected
    applied_date TIMESTAMP NOT NULL,
    cover_letter TEXT,
    resume_url VARCHAR(500),
    additional_skills VARCHAR(500),
    availability VARCHAR(100),
    expected_stipend VARCHAR(50),
    hr_notes TEXT,
    reviewed_date TIMESTAMP,
    reviewed_by BIGINT,  -- HR/Faculty user ID
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (career_post_id) REFERENCES career_posts(id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_career_post_id (career_post_id),
    INDEX idx_status (status),
    INDEX idx_applied_date (applied_date)
);
```

#### New Table: `student_documents`
```sql
CREATE TABLE student_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,  -- Resume, Cover Letter, Certificate, ID Proof
    document_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    uploaded_date TIMESTAMP NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,  -- For primary resume
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_document_type (document_type)
);
```

#### New Table: `notifications`
```sql
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    user_type VARCHAR(20) NOT NULL,  -- STUDENT, HR, FACULTY, ADMIN
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),  -- APPLICATION_STATUS, NEW_INTERNSHIP, INTERVIEW, SYSTEM
    related_entity_id BIGINT,  -- ID of related application/internship
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_user_type (user_type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);
```

#### Modifications to Existing Tables:

**students table - Add columns:**
```sql
ALTER TABLE students ADD COLUMN profile_photo_url VARCHAR(500);
ALTER TABLE students ADD COLUMN primary_resume_url VARCHAR(500);
ALTER TABLE students ADD COLUMN linkedin_url VARCHAR(200);
ALTER TABLE students ADD COLUMN github_url VARCHAR(200);
ALTER TABLE students ADD COLUMN skills TEXT;
ALTER TABLE students ADD COLUMN bio TEXT;
ALTER TABLE students ADD COLUMN profile_completion_percentage INT DEFAULT 0;
```

---

### 2. Java Model Classes

#### Application.java
```java
package com.webapp.Tracker_pro.model;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "career_post_id", nullable = false)
    private Long careerPostId;
    
    @Column(nullable = false, length = 20)
    private String status = "Pending";
    
    @Column(name = "applied_date", nullable = false)
    private LocalDateTime appliedDate;
    
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;
    
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;
    
    @Column(name = "additional_skills", length = 500)
    private String additionalSkills;
    
    @Column(length = 100)
    private String availability;
    
    @Column(name = "expected_stipend", length = 50)
    private String expectedStipend;
    
    @Column(name = "hr_notes", columnDefinition = "TEXT")
    private String hrNotes;
    
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Transient fields for response
    @Transient
    private String studentName;
    
    @Transient
    private String internshipTitle;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        appliedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### StudentDocument.java
```java
package com.webapp.Tracker_pro.model;

@Entity
@Table(name = "student_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;
    
    @Column(name = "document_name", nullable = false)
    private String documentName;
    
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "uploaded_date", nullable = false)
    private LocalDateTime uploadedDate;
    
    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        uploadedDate = LocalDateTime.now();
    }
}
```

#### Notification.java
```java
package com.webapp.Tracker_pro.model;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "notification_type", length = 50)
    private String notificationType;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

### 3. API Endpoints to Implement

#### StudentController.java

```java
package com.webapp.Tracker_pro.controller;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    // ==================== Profile Management ====================
    
    /**
     * Get student profile
     * @endpoint GET /api/student/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile();
    
    /**
     * Update student profile
     * @endpoint PUT /api/student/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody StudentProfileRequest request);
    
    /**
     * Upload profile photo
     * @endpoint POST /api/student/profile/photo
     */
    @PostMapping("/profile/photo")
    public ResponseEntity<Map<String, Object>> uploadProfilePhoto(@RequestParam("file") MultipartFile file);
    
    // ==================== Document Management ====================
    
    /**
     * Upload document (resume, certificate, etc.)
     * @endpoint POST /api/student/documents
     */
    @PostMapping("/documents")
    public ResponseEntity<Map<String, Object>> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("documentType") String documentType);
    
    /**
     * Get all my documents
     * @endpoint GET /api/student/documents
     */
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> getMyDocuments();
    
    /**
     * Delete document
     * @endpoint DELETE /api/student/documents/{id}
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long id);
    
    /**
     * Set primary resume
     * @endpoint PATCH /api/student/documents/{id}/set-primary
     */
    @PatchMapping("/documents/{id}/set-primary")
    public ResponseEntity<Map<String, Object>> setPrimaryResume(@PathVariable Long id);
    
    // ==================== Internship Browsing ====================
    
    /**
     * Get all published internships (students see only published posts)
     * @endpoint GET /api/student/internships
     */
    @GetMapping("/internships")
    public ResponseEntity<Map<String, Object>> getAvailableInternships(
        @RequestParam(required = false) String workMode,
        @RequestParam(required = false) String search);
    
    /**
     * Get internship details
     * @endpoint GET /api/student/internships/{id}
     */
    @GetMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> getInternshipDetails(@PathVariable Long id);
    
    /**
     * Check if already applied
     * @endpoint GET /api/student/internships/{id}/check-application
     */
    @GetMapping("/internships/{id}/check-application")
    public ResponseEntity<Map<String, Object>> checkIfApplied(@PathVariable Long id);
    
    // ==================== Application Management ====================
    
    /**
     * Apply for internship
     * @endpoint POST /api/student/applications
     */
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> applyForInternship(@RequestBody ApplicationRequest request);
    
    /**
     * Get all my applications
     * @endpoint GET /api/student/applications
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getMyApplications(
        @RequestParam(required = false) String status);
    
    /**
     * Get application details
     * @endpoint GET /api/student/applications/{id}
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationDetails(@PathVariable Long id);
    
    /**
     * Withdraw application
     * @endpoint DELETE /api/student/applications/{id}
     */
    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> withdrawApplication(@PathVariable Long id);
    
    // ==================== Dashboard ====================
    
    /**
     * Get dashboard statistics
     * @endpoint GET /api/student/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats();
    
    // ==================== Notifications ====================
    
    /**
     * Get my notifications
     * @endpoint GET /api/student/notifications
     */
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications(
        @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly);
    
    /**
     * Mark notification as read
     * @endpoint PATCH /api/student/notifications/{id}/read
     */
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id);
    
    /**
     * Mark all notifications as read
     * @endpoint PATCH /api/student/notifications/read-all
     */
    @PatchMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead();
}
```

---

### 4. Request/Response DTOs

#### ApplicationRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    @NotNull(message = "Career post ID is required")
    private Long careerPostId;
    
    @NotBlank(message = "Cover letter is required")
    @Size(min = 50, max = 2000, message = "Cover letter must be between 50 and 2000 characters")
    private String coverLetter;
    
    @NotBlank(message = "Resume URL is required")
    private String resumeUrl;
    
    private String additionalSkills;
    
    @NotBlank(message = "Availability is required")
    private String availability;
    
    private String expectedStipend;
}
```

#### ApplicationResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long careerPostId;
    private String internshipTitle;
    private String internshipCode;
    private String status;
    private String appliedDate;
    private String coverLetter;
    private String resumeUrl;
    private String additionalSkills;
    private String availability;
    private String expectedStipend;
    private String hrNotes;
    private String reviewedDate;
    private String internshipWorkMode;
    private Integer internshipDuration;
}
```

#### StudentProfileRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileRequest {
    private String firstName;
    private String lastName;
    private String mobileNo;
    private String gender;
    private String dob;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String skills;
    private String bio;
}
```

---

## PART B: FRONTEND DEVELOPMENT

### 1. Student Dashboard Page (`/studentDashboard.html`)

**Layout:**
```
Header Bar (with logout, notifications)
├── Welcome Section (Student Name, Profile Photo)
├── Quick Stats Cards
│   ├── Total Applications
│   ├── Pending Applications
│   ├── Accepted Applications
│   └── Profile Completion %
├── Recent Applications Table
├── Recommended Internships Section
└── Recent Notifications
```

**Key Features:**
- Profile completion progress bar
- Quick action buttons (Browse Internships, View Applications)
- Application status summary
- Recent activity timeline
- Notification bell with badge

---

### 2. Browse Internships Page (`/studentInternships.html`)

**Layout:**
```
Search & Filter Bar
├── Search by title/skills
├── Filter by Work Mode (Online/Offline/Hybrid)
├── Filter by Duration
└── Sort by (Latest, Duration, Title)

Internships Grid
├── Internship Card 1
│   ├── Title, Code, Duration
│   ├── Work Mode Badge
│   ├── Prerequisites
│   ├── Description (truncated)
│   ├── Applications Count
│   └── "Apply Now" Button
├── Internship Card 2
└── Internship Card 3...

Pagination Controls
```

**Key Features:**
- Responsive grid layout
- Real-time search
- Filter combinations
- "Already Applied" badge on cards
- View details modal
- Direct apply from card

---

### 3. Application Form Page (`/applyInternship.html`)

**Layout:**
```
Internship Summary Card
├── Title, Code, Duration, Work Mode
└── Prerequisites, Description

Application Form
├── Select Resume (dropdown of uploaded resumes + upload new)
├── Cover Letter (textarea, 50-2000 chars)
├── Additional Skills (textarea)
├── Availability (dropdown: Immediate, 1 month, 2 months, etc.)
├── Expected Stipend (optional)
├── Terms & Conditions Checkbox
└── Submit Application Button
```

**Validation:**
- All required fields must be filled
- Cover letter min 50 characters
- Resume must be selected
- Terms acceptance required
- Prevent duplicate applications

---

### 4. My Applications Page (`/studentApplications.html`)

**Layout:**
```
Filter Tabs
├── All Applications
├── Pending
├── Under Review
├── Shortlisted
├── Accepted
└── Rejected

Applications Table
├── S.No
├── Internship Title
├── Applied Date
├── Status Badge
├── Actions (View Details, Withdraw)

Application Details Modal
├── Internship Details
├── My Application
│   ├── Cover Letter
│   ├── Resume Link
│   ├── Additional Skills
│   ├── Availability
│   ├── Expected Stipend
├── Application Status Timeline
├── HR Notes (if any)
└── Close Button
```

**Key Features:**
- Status-based filtering
- Color-coded status badges
- Timeline showing application progress
- Withdraw option (only for Pending status)
- View submitted resume

---

### 5. Student Profile Page (`/studentProfile.html`)

**Layout:**
```
Profile Header
├── Profile Photo (with upload button)
├── Student Name
├── Email, Mobile
└── Profile Completion Progress

Profile Edit Form (Tabs)
├── Personal Information Tab
│   ├── First Name, Last Name
│   ├── Mobile Number
│   ├── Gender, Date of Birth
│   ├── Location/City
│   ├── Bio (textarea)
│   └── Save Button
│
├── Professional Information Tab
│   ├── Skills (tags input)
│   ├── LinkedIn URL
│   ├── GitHub URL
│   └── Save Button
│
├── Documents Tab
│   ├── Upload Resume Button
│   ├── Uploaded Documents List
│   │   ├── Document Name, Type, Upload Date
│   │   ├── Set as Primary Resume
│   │   └── Delete Button
│
└── Security Tab
    ├── Current Password
    ├── New Password
    ├── Confirm Password
    └── Change Password Button
```

**Key Features:**
- Live profile completion percentage
- Drag-and-drop file upload
- Multiple resume versions
- Set primary resume
- Password strength indicator

---

## PART C: API INTEGRATION

### JavaScript API Service (`studentAPI.js`)

```javascript
const API_BASE_URL = 'http://localhost:8080/api';

const StudentAPI = {
    // Get JWT token from localStorage
    getToken: () => localStorage.getItem('authToken'),
    
    // Get authenticated headers
    getHeaders: () => ({
        'Authorization': `Bearer ${StudentAPI.getToken()}`,
        'Content-Type': 'application/json'
    }),
    
    // Profile APIs
    getProfile: async () => {
        const response = await fetch(`${API_BASE_URL}/student/profile`, {
            method: 'GET',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    },
    
    updateProfile: async (profileData) => {
        const response = await fetch(`${API_BASE_URL}/student/profile`, {
            method: 'PUT',
            headers: StudentAPI.getHeaders(),
            body: JSON.stringify(profileData)
        });
        return response.json();
    },
    
    // Internship APIs
    getInternships: async (filters = {}) => {
        const params = new URLSearchParams(filters);
        const response = await fetch(`${API_BASE_URL}/student/internships?${params}`, {
            method: 'GET',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    },
    
    getInternshipDetails: async (id) => {
        const response = await fetch(`${API_BASE_URL}/student/internships/${id}`, {
            method: 'GET',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    },
    
    // Application APIs
    applyForInternship: async (applicationData) => {
        const response = await fetch(`${API_BASE_URL}/student/applications`, {
            method: 'POST',
            headers: StudentAPI.getHeaders(),
            body: JSON.stringify(applicationData)
        });
        return response.json();
    },
    
    getMyApplications: async (status = null) => {
        const url = status 
            ? `${API_BASE_URL}/student/applications?status=${status}`
            : `${API_BASE_URL}/student/applications`;
        const response = await fetch(url, {
            method: 'GET',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    },
    
    withdrawApplication: async (id) => {
        const response = await fetch(`${API_BASE_URL}/student/applications/${id}`, {
            method: 'DELETE',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    },
    
    // Dashboard APIs
    getDashboardStats: async () => {
        const response = await fetch(`${API_BASE_URL}/student/dashboard/stats`, {
            method: 'GET',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    },
    
    // Document APIs
    uploadDocument: async (file, documentType) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('documentType', documentType);
        
        const response = await fetch(`${API_BASE_URL}/student/documents`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${StudentAPI.getToken()}`
                // Don't set Content-Type for FormData, browser will set it
            },
            body: formData
        });
        return response.json();
    },
    
    getMyDocuments: async () => {
        const response = await fetch(`${API_BASE_URL}/student/documents`, {
            method: 'GET',
            headers: StudentAPI.getHeaders()
        });
        return response.json();
    }
};
```

---

## PART D: FILE UPLOAD HANDLING

### Backend File Upload Configuration

#### FileStorageService.java
```java
@Service
public class FileStorageService {
    
    private final String uploadDir = "/app/uploads/";
    
    public String storeFile(MultipartFile file, String subDir) throws IOException {
        // Create directory if not exists
        Path dirPath = Paths.get(uploadDir + subDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return URL
        return "/uploads/" + subDir + "/" + filename;
    }
    
    public void deleteFile(String fileUrl) throws IOException {
        Path filePath = Paths.get(uploadDir + fileUrl.replace("/uploads/", ""));
        Files.deleteIfExists(filePath);
    }
}
```

#### Static Resource Configuration
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/app/uploads/");
    }
}
```

---

## 🎯 PHASE 3: HR/FACULTY MODULE - OVERVIEW

### Key Features:
1. **HR Dashboard:** Stats on posted internships, applications received
2. **Post Internships:** Create/Edit/Delete internship postings
3. **Review Applications:** View applications, accept/reject
4. **Schedule Interviews:** Manage interview schedules
5. **Add Feedback:** Provide feedback on applications

### API Endpoints (High-Level):
```
GET    /api/hr/dashboard/stats
GET    /api/hr/my-internships
GET    /api/hr/applications
PATCH  /api/hr/applications/{id}/status
POST   /api/hr/applications/{id}/feedback
```

### Frontend Pages:
- `/hrDashboard.html`
- `/hrInternships.html`
- `/hrApplications.html`
- `/hrProfile.html`

---

## 📋 IMPLEMENTATION CHECKLIST

### Student Module Backend:
- [ ] Create Application model & repository
- [ ] Create StudentDocument model & repository
- [ ] Create Notification model & repository
- [ ] Implement StudentService with all business logic
- [ ] Implement StudentController with all endpoints
- [ ] Add file upload configuration
- [ ] Test all APIs with cURL/Postman
- [ ] Write unit tests (optional but recommended)

### Student Module Frontend:
- [ ] Create studentDashboard.html
- [ ] Create studentInternships.html
- [ ] Create applyInternship.html
- [ ] Create studentApplications.html
- [ ] Create studentProfile.html
- [ ] Create studentAPI.js (API service layer)
- [ ] Integrate authentication flow
- [ ] Test all pages and workflows
- [ ] Cross-browser testing

### HR Module Backend:
- [ ] Implement HRService
- [ ] Implement HRController
- [ ] Add application review logic
- [ ] Add interview scheduling
- [ ] Test all APIs

### HR Module Frontend:
- [ ] Create hrDashboard.html
- [ ] Create hrInternships.html
- [ ] Create hrApplications.html
- [ ] Create hrProfile.html
- [ ] Integrate with backend APIs

---

## 🚀 READY TO START?

**Shall we begin with the Student Module Backend implementation?**

I recommend starting with:
1. Database schema creation (Application, StudentDocument, Notification tables)
2. Model classes (Application.java, StudentDocument.java, Notification.java)
3. Repository interfaces
4. Service layer implementation
5. Controller endpoints
6. Testing

**Please confirm to proceed with the implementation!**
