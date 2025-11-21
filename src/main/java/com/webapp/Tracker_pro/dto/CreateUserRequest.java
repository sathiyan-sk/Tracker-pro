package com.webapp.Tracker_pro.dto;

import com.webapp.Tracker_pro.model.UserType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating HR/Faculty users by admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name must contain only letters")
    private String firstName;

    @Size(max = 30, message = "Last name must not exceed 30 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name must contain only letters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(min = 6, max = 100, message = "Email must be between 6 and 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit Indian number")
    private String mobileNo;

    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Date of birth must be in DD/MM/YYYY format")
    private String dob;

    @Min(value = 20, message = "Age must be at least 20")
    @Max(value = 65, message = "Age must not exceed 65")
    private Integer age;

    @Size(max = 50, message = "Location must not exceed 50 characters")
    private String location;

    @NotNull(message = "User type is required")
    @Pattern(regexp = "^(HR|FACULTY)$", message = "User type must be HR or FACULTY")
    private String userType;
}
