package com.webapp.Tracker_pro.dto;

import com.webapp.Tracker_pro.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for HR/Faculty User API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRFacultyUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String gender;
    private String dob;
    private Integer age;
    private String location;
    private String role; // "HR" or "Faculty"
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String status; // "Active" or "Inactive"
    private String lastLogin; // Can be null for now
}
