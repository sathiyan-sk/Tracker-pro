# HR Module - Frontend Analysis & Backend Requirements

**Date:** December 5, 2025  
**Status:** ✅ Frontend HTML Loaded | 🔨 Backend Development Required

---

## 📁 Files Status

- ✅ **Source File:** `/app/src/main/resources/static/hrPage.html` (191KB)
- ✅ **Backup File:** `/app/src/main/resources/static/hrPage.html.bak` (191KB)

---

## 🎯 HR Module Pages Overview

### 1. **Applications Page** (`#applications`)
**Purpose:** View and manage all student applications for internships

**UI Elements:**
- Application cards displaying candidate information
- Date filter (All Time, Today, Last 7 Days, Last 30 Days, Custom Range)
- Search by name/email
- Select All checkbox
- Bulk action bar (Shortlist Selected, Mark Under Review, Reject Selected)

**Data Displayed Per Application:**
- Applicant ID
- Candidate Name
- Father Name
- Gender
- Date of Birth
- Email
- Phone
- Address & Pincode
- Internship ID & Title
- Applied Date
- Status Badge (New, Under Review, On Hold, etc.)
- Resume URL (if available)

**Actions Per Application:**
- View Details (modal)
- View Resume (modal)
- Shortlist
- Mark Under Review
- Reject

---

### 2. **Shortlisted Page** (`#shortlisted`)
**Purpose:** Manage shortlisted candidates in table format

**Tabs:**
1. **All Applications** - All shortlisted candidates
2. **Applied** - Candidates who applied through the system
3. **Imported** - Candidates imported from PMIS

**UI Elements:**
- Table with sortable/draggable columns
- Select All checkbox
- Bulk actions: Schedule Interviews, Send Email
- Edit dropdown: Alter Columns, Delete marked
- Import button for adding external candidates

**Table Columns:**
- Candidate ID
- Candidate Name
- Father Name
- Gender
- Mobile
- Email
- Address
- Pincode
- Internship ID
- Internship Title
- Category (PMIS, ZSGS)
- Type (Applied, Imported)
- Next Steps (Action dropdown)

**Actions Per Row:**
- Schedule Interview
- Send Email
- Remove from shortlist

---

### 3. **Interviews Page** (`#interviews`)
**Purpose:** Track and manage scheduled interviews

**Tabs:**
1. **Upcoming** - Future scheduled interviews
2. **Past** - Completed interviews

**UI Elements:**
- Search candidates by name
- View Interview Dates (calendar modal)
- Interview table

**Table Columns:**
- Candidate (Name, Email)
- Date & Time
- Status (Awaiting Confirmation, In Progress, Completed, Rescheduled)
- HR Manager
- Mode (Online, Offline)
- Actions (View Details, Reschedule, Add Feedback, Mark Complete)

**Interview Details (Expanded Row):**
- Interview Notes
- Feedback
- Marks/Rating
- Duration

---

### 4. **Hired Students Page** (`#hired`)
**Purpose:** Manage hired/onboarded students

**UI Elements:**
- Search by name, email, or candidate ID
- Filter by Category (PMIS, ZSGS)
- Sort options (Name, Date, Status, Category)
- Records per page selector
- Import ZSGS button
- Direct Hire button
- Pagination

**Table Columns:**
- Candidate ID
- Candidate Name
- Gender
- Internship Title
- Internship ID
- Hired Date
- HR Manager
- Category
- Status (Onboarding, Onboarded)
- Documents (with completion icons)
- Actions (View, Edit, Generate ID Card, Remove)

---

## 📊 Data Structure Requirements

### Application Object (Frontend Expected Structure)
```javascript
{
  applicantId: 'APID001',           // Application ID
  candidateName: 'Raj Sekar',       // Student name
  fatherName: 'Kumar Sekar',        // Father's name
  gender: 'Male',                   // Gender
  dob: '15/06/2001',                // Date of birth
  email: 'raj.sekar@example.com',   // Email
  phone: '9876543210',              // Phone number
  address: '123 Main St, Madurai',  // Full address
  pincode: '625001',                // Pincode
  internshipId: 'INT001',           // Career post code
  internshipTitle: 'Software Dev',  // Career post title
  appliedDate: '2025-11-05',        // Application date
  status: 'New',                    // Application status
  resumeUrl: 'https://...',         // Resume URL
  coverLetter: '...',               // Cover letter text
  additionalSkills: '...',          // Additional skills
  availability: 'Immediate',        // Availability
  expectedStipend: '15000'          // Expected stipend
}
```

### Shortlisted Candidate (Additional Fields)
```javascript
{
  ...applicationData,
  type: 'Applied' | 'Imported',     // Source type
  category: 'PMIS' | 'ZSGS'         // Category
}
```

### Interview Object
```javascript
{
  id: 'INT_001',
  candidateName: '...',
  candidateEmail: '...',
  applicationId: 'APID001',
  date: '2025-12-10',
  time: '10:00 AM',
  duration: 30,                     // minutes
  mode: 'Online' | 'Offline',
  status: 'Awaiting Confirmation',
  hrManager: 'John Doe',
  hrManagerId: 123,
  notes: '...',
  feedback: '...',
  marks: 85
}
```

### Hired Student Object
```javascript
{
  candidateId: 'EMP001',
  name: '...',
  gender: '...',
  internshipTitle: '...',
  internshipId: '...',
  hiredDate: '2025-12-01',
  hrManager: '...',
  category: 'PMIS' | 'ZSGS',
  status: 'Onboarding' | 'Onboarded',
  documents: [
    { name: 'Aadhar', status: 'completed' | 'pending' },
    { name: 'PAN', status: 'completed' | 'pending' },
    // ...
  ]
}
```

---

## 🔧 Required Backend API Endpoints

### 1. Applications Management

#### GET `/api/hr/applications`
**Purpose:** Fetch all applications with optional filters  
**Query Parameters:**
- `search` (optional) - Search by name/email
- `status` (optional) - Filter by status
- `dateFrom` (optional) - Start date filter
- `dateTo` (optional) - End date filter
- `internshipId` (optional) - Filter by internship

**Response:**
```json
{
  "success": true,
  "total": 150,
  "data": [
    {
      "id": 1,
      "applicantId": "APID001",
      "student": {
        "id": 5,
        "firstName": "Raj",
        "lastName": "Sekar",
        "fatherName": "Kumar Sekar",
        "email": "raj.sekar@example.com",
        "mobileNo": "9876543210",
        "gender": "Male",
        "dob": "15/06/2001",
        "address": "123 Main St, Madurai",
        "pincode": "625001"
      },
      "internship": {
        "id": 1,
        "code": "INT001",
        "title": "Software Development",
        "duration": 6,
        "workMode": "Hybrid"
      },
      "status": "Pending",
      "appliedDate": "2025-11-05T10:30:00",
      "coverLetter": "...",
      "resumeUrl": "https://...",
      "additionalSkills": "...",
      "availability": "Immediate",
      "expectedStipend": "15000",
      "hrNotes": null,
      "reviewedBy": null,
      "reviewedDate": null
    }
  ]
}
```

---

#### GET `/api/hr/applications/{id}`
**Purpose:** Get detailed application information  
**Path Parameter:** `id` - Application ID

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "applicantId": "APID001",
    "student": { /* full student object */ },
    "internship": { /* full internship object */ },
    "status": "Pending",
    "appliedDate": "2025-11-05T10:30:00",
    "coverLetter": "Full cover letter text...",
    "resumeUrl": "https://...",
    "resumeFilename": "raj_sekar_resume.pdf",
    "additionalSkills": "Python, Java, React",
    "availability": "Immediate",
    "expectedStipend": "15000",
    "hrNotes": "Good candidate",
    "reviewedBy": 10,
    "reviewedByName": "John HR",
    "reviewedDate": "2025-11-06T14:30:00"
  }
}
```

---

#### PUT `/api/hr/applications/{id}/status`
**Purpose:** Update application status  
**Path Parameter:** `id` - Application ID  
**Request Body:**
```json
{
  "status": "Shortlisted",
  "hrNotes": "Selected for interview round"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Application status updated successfully",
  "data": { /* updated application object */ }
}
```

---

#### PUT `/api/hr/applications/{id}/notes`
**Purpose:** Add or update HR notes  
**Path Parameter:** `id` - Application ID  
**Request Body:**
```json
{
  "hrNotes": "Excellent technical skills, good communication"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Notes updated successfully"
}
```

---

#### PUT `/api/hr/applications/bulk-update`
**Purpose:** Update multiple applications at once  
**Request Body:**
```json
{
  "applicationIds": [1, 2, 3],
  "status": "Under Review"
}
```

**Response:**
```json
{
  "success": true,
  "message": "3 applications updated successfully",
  "updated": 3
}
```

---

### 2. Shortlisted Candidates

#### GET `/api/hr/applications/shortlisted`
**Purpose:** Get all shortlisted applications  
**Query Parameters:**
- `type` (optional) - Filter by 'Applied' or 'Imported'

**Response:**
```json
{
  "success": true,
  "total": 25,
  "data": [ /* array of applications with status='Shortlisted' */ ]
}
```

---

### 3. Dashboard & Statistics

#### GET `/api/hr/dashboard/stats`
**Purpose:** Get HR dashboard statistics

**Response:**
```json
{
  "success": true,
  "data": {
    "totalApplications": 150,
    "pending": 80,
    "underReview": 30,
    "shortlisted": 25,
    "accepted": 10,
    "rejected": 5,
    "byInternship": [
      {
        "internshipId": 1,
        "internshipTitle": "Software Development",
        "applicationCount": 45
      }
    ]
  }
}
```

---

### 4. Interview Management (Future Enhancement)

#### POST `/api/hr/interviews`
**Purpose:** Schedule an interview  
**Request Body:**
```json
{
  "applicationId": 1,
  "date": "2025-12-10",
  "time": "10:00",
  "duration": 30,
  "mode": "Online",
  "notes": "Technical round"
}
```

#### GET `/api/hr/interviews`
**Purpose:** Get scheduled interviews  
**Query Parameters:**
- `status` - 'upcoming' or 'past'

---

### 5. Hired Students (Future Enhancement)

#### POST `/api/hr/hired`
**Purpose:** Mark candidate as hired

#### GET `/api/hr/hired`
**Purpose:** Get all hired students

---

## 🔐 Security Considerations

- All endpoints must require `ROLE_HR` or `ROLE_ADMIN` authorization
- JWT token must be validated
- Only HR users should access these endpoints
- Add audit logging for status changes

---

## 🎨 Frontend Integration Points

### Key HTML Elements to Populate:

1. **Applications Container:** `#applicationsContainer`
   - Dynamically generate application cards

2. **Shortlist Tables:**
   - `#allTableBody` - All shortlisted
   - `#appliedTableBody` - Applied candidates
   - `#importedTableBody` - Imported candidates

3. **Interview Tables:**
   - `#upcomingTableBody` - Upcoming interviews
   - `#pastTableBody` - Past interviews

4. **Hired Students Table:** `#hiredTableBody`

5. **Statistics Elements:**
   - `#showingText` - Result count
   - Various count displays

### Key JavaScript Functions to Connect:

1. `loadApplications()` - Fetch and display applications
2. `shortlistCandidate()` - Update status to Shortlisted
3. `rejectCandidate()` - Update status to Rejected
4. `bulkShortlist()` - Bulk status update
5. `renderShortlistTable()` - Populate shortlist tables
6. `searchApplications()` - Client-side search/filter
7. `applyDateFilter()` - Filter by date range

---

## 📝 Implementation Priority

### Phase 1: Core Application Management (HIGH PRIORITY)
1. ✅ Create HRController
2. ✅ Create HRApplicationService
3. ✅ Create ApplicationDetailResponse DTO
4. ✅ Implement GET /api/hr/applications
5. ✅ Implement GET /api/hr/applications/{id}
6. ✅ Implement PUT /api/hr/applications/{id}/status
7. ✅ Implement PUT /api/hr/applications/{id}/notes
8. ✅ Implement GET /api/hr/dashboard/stats

### Phase 2: Frontend Integration (HIGH PRIORITY)
1. Add API URL configuration
2. Add JWT token handling
3. Implement loadApplications() with API call
4. Implement status update functions
5. Test application listing and filtering
6. Test status updates

### Phase 3: Shortlist & Bulk Operations (MEDIUM PRIORITY)
1. Implement bulk update endpoint
2. Connect bulk action buttons
3. Test shortlist functionality

### Phase 4: Interviews & Hired (LOW PRIORITY - Future)
1. Create interview management endpoints
2. Create hired students endpoints
3. Implement frontend connections

---

## ✅ Next Steps

1. **Backend Development:**
   - Create HRController with all endpoints
   - Create HRApplicationService for business logic
   - Create necessary DTOs
   - Add security configuration for HR role

2. **Frontend Integration:**
   - Add API configuration (backend URL)
   - Replace mock data with API calls
   - Add JWT token to all requests
   - Test thoroughly

3. **Testing:**
   - Test with real data
   - Verify all filters work
   - Test bulk operations
   - Check role-based access

---

**Status:** 📋 Analysis Complete | Ready for Backend Development
