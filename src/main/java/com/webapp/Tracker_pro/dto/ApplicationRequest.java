package com.webapp.Tracker_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an internship application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    @NotNull(message = "Career post ID is required")
    private Long careerPostId;
    
    private String coverLetter;
    
    private String resumeUrl;
    
    private String resumeFilename;
    
    private String additionalSkills;
    
    private String availability;
    
    private String expectedStipend;
}
