package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for HR application summary response (list view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRApplicationSummaryResponse {
    
    private Long id;
    private String applicantId;  // Generated: "APID" + formatted ID
    
    // Student details (nested)
    private StudentInfo student;
    
    // Internship details (nested)
    private InternshipInfo internship;
    
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
    private String reviewedDate;
    
    /**
     * Nested DTO for student information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String mobileNo;
        private String gender;
        private String dob;
        private String fatherName;  // Will be empty string if not available
        private String address;     // Maps to location field
        private String pincode;     // Will be extracted or empty
    }
    
    /**
     * Nested DTO for internship information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipInfo {
        private Long id;
        private String code;     // Internship ID like "INT001"
        private String title;
        private Integer duration;
        private String workMode;
    }
}
