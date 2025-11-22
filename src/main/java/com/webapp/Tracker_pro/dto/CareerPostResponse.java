package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for career post responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerPostResponse {
    
    private Long id;
    private String code;
    private String title;
    private Integer duration;
    private String workMode;
    private String prerequisites;
    private String description;
    private String status;
    private Integer applicationsCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
