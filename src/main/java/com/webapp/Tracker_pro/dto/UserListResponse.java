package com.webapp.Tracker_pro.dto;

import com.webapp.Tracker_pro.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user list response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
    private UserType userType;
    private String gender;
    private String dob;
    private Integer age;
    private String location;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
