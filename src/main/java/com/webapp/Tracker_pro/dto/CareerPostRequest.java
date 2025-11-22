package com.webapp.Tracker_pro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating career posts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerPostRequest {
    
    @NotBlank(message = "Post code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer duration;
    
    @NotBlank(message = "Work mode is required")
    private String workMode;
    
    @Size(max = 500, message = "Prerequisites must not exceed 500 characters")
    private String prerequisites;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotBlank(message = "Status is required")
    private String status;
}
