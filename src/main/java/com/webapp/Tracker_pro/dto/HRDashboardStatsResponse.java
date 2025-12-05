package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for HR dashboard statistics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRDashboardStatsResponse {
    
    private long totalApplications;
    private long pending;
    private long underReview;
    private long shortlisted;
    private long accepted;
    private long rejected;
    
    // Applications grouped by internship
    private List<InternshipApplicationCount> byInternship;
    
    /**
     * Application count per internship
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipApplicationCount {
        private Long internshipId;
        private String internshipTitle;
        private String internshipCode;
        private long applicationCount;
    }
}
