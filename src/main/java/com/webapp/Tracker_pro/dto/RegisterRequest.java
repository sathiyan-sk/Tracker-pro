package com.webapp.Tracker_pro.dto;

import com.webapp.Tracker_pro.model.UserType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration requests.
 * Contains all necessary fields with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name can only contain letters and spaces")
    private String firstName;

    @Size(max = 30, message = "Last name cannot exceed 30 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name can only contain letters and spaces")
    private String lastName;

    @NotNull(message = "User type is required")
    private UserType userType;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(min = 6, max = 100, message = "Email must be between 6 and 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 30, message = "Password must be between 6 and 30 characters")
    private String password;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Please enter a valid 10-digit mobile number")
    private String mobileNo;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    private String dob;

    @NotNull(message = "Age is required")
    @Min(value = 20, message = "Age must be at least 20 years")
    @Max(value = 25, message = "Age cannot exceed 25 years")
    private Integer age;

    @Size(max = 50, message = "Location cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s,.-]*$", message = "Location can only contain letters, spaces, and punctuation")
    private String location;
}
