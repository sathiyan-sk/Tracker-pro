package com.webapp.Tracker_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk updating multiple applications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateApplicationRequest {
    
    @NotEmpty(message = "Application IDs list cannot be empty")
    private List<Long> applicationIds;
    
    @NotBlank(message = "Status is required")
    private String status;  // Pending, Under Review, Shortlisted, Accepted, Rejected
}
