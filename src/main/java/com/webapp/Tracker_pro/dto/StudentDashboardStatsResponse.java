package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardStatsResponse {
    
    private Long totalApplications;
    private Long pendingApplications;
    private Long underReviewApplications;
    private Long shortlistedApplications;
    private Long acceptedApplications;
    private Long rejectedApplications;
    private Integer profileCompletionPercentage;
    private Long availableInternships;
    private Long unreadNotifications;
}
