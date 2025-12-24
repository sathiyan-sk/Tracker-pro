package com.webapp.Tracker_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending interview invitation email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendInterviewInviteRequest {
    
    @NotBlank(message = "Candidate email is required")
    private String candidateEmail;
    
    @NotBlank(message = "Candidate name is required")
    private String candidateName;
    
    @NotBlank(message = "Interview date is required")
    private String interviewDate;
    
    @NotBlank(message = "Interview time is required")
    private String interviewTime;
    
    @NotBlank(message = "Interview mode is required")
    private String interviewMode; // Online or Offline
    
    private String meetingLink; // Required for online interviews
    
    @NotBlank(message = "HR manager name is required")
    private String hrManagerName;
    
    private String interviewTitle;
    
    private String additionalNotes;
}
