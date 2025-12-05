package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for HR application detailed response (single application view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRApplicationDetailResponse {
    
    private Long id;
    private String applicantId;  // Generated: "APID" + formatted ID
    
    // Complete student details
    private StudentDetails student;
    
    // Complete internship details
    private InternshipDetails internship;
    
    // Application details
    private String status;
    private String appliedDate;
    private String coverLetter;
    private String resumeUrl;
    private String resumeFilename;
    private String additionalSkills;
    private String availability;
    private String expectedStipend;
    private String hrNotes;
    private Long reviewedBy;
    private String reviewedByName;  // Name of HR who reviewed
    private String reviewedDate;
    private String createdAt;
    private String updatedAt;
    
    /**
     * Complete student details for detailed view
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDetails {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String mobileNo;
        private String gender;
        private String dob;
        private Integer age;
        private String location;
        private String fatherName;     // Will be empty string if not available
        private String address;        // Maps to location field
        private String pincode;        // Will be extracted or empty
        private String profilePhotoUrl;
        private String primaryResumeUrl;
        private String linkedinUrl;
        private String githubUrl;
        private String skills;
        private String bio;
    }
    
    /**
     * Complete internship details for detailed view
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipDetails {
        private Long id;
        private String code;         // Internship ID like "INT001"
        private String title;
        private Integer duration;
        private String workMode;
        private String prerequisites;
        private String description;
        private String status;
        private Integer applicationsCount;
    }
}
