package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for application response
 */
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
    private String resumeFilename;
    private String additionalSkills;
    private String availability;
    private String expectedStipend;
    private String hrNotes;
    private String reviewedDate;
    private String internshipWorkMode;
    private Integer internshipDuration;
    private String internshipDescription;
    private String internshipPrerequisites;
}
