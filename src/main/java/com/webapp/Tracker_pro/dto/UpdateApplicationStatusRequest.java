package com.webapp.Tracker_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating application status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusRequest {
    
    @NotBlank(message = "Status is required")
    private String status;  // Pending, Under Review, Shortlisted, Accepted, Rejected
    
    private String hrNotes;  // Optional notes to add with status update
}
