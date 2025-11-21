package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dashboard statistics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalStudents;
    private Long totalFacultyHR;
    private Long publishedPosts;
    private Long newStudentsThisWeek;
    private boolean success;
    private String message;
}
