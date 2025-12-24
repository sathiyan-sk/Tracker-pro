package com.webapp.Tracker_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for sending bulk emails to multiple candidates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendBulkEmailRequest {
    
    @NotEmpty(message = "Recipient list cannot be empty")
    private List<RecipientInfo> recipients;
    
    @NotBlank(message = "Email subject is required")
    private String subject;
    
    @NotBlank(message = "Email body is required")
    private String body;
    
    /**
     * Recipient information with email and name
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        @NotBlank(message = "Email is required")
        private String email;
        
        private String name;
    }
}
