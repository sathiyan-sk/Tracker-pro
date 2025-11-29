package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Student API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String gender;
    private String dob;
    private Integer age;
    private String location;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String role; // Always "Student"
}
