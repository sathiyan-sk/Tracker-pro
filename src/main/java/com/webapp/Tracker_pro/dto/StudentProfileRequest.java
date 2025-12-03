package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating student profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileRequest {
    
    private String firstName;
    private String lastName;
    private String mobileNo;
    private String gender;
    private String dob;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String skills;
    private String bio;
}
