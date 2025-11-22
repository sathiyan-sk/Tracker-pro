package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.AuthResponse;
import com.webapp.Tracker_pro.dto.LoginRequest;
import com.webapp.Tracker_pro.dto.RegisterRequest;
import com.webapp.Tracker_pro.exception.InvalidCredentialsException;
import com.webapp.Tracker_pro.exception.UserAlreadyExistsException;
import com.webapp.Tracker_pro.model.Admin;
import com.webapp.Tracker_pro.model.User;
import com.webapp.Tracker_pro.model.UserType;
import com.webapp.Tracker_pro.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for authentication operations including registration and login.
 * Handles both Admin and User authentication.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     * @param request Registration request containing user details
     * @return AuthResponse with JWT token and user information
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userService.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                "Email already registered. Please use a different email or login."
            );
        }

        // Check if mobile number already exists
        if (userService.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException(
                "Mobile number already registered. Please use a different number."
            );
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        user.setUserType(request.getUserType());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNo(request.getMobileNo());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setAge(request.getAge());
        user.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        user.setIsActive(true);

        // Save user to database
        User savedUser = userService.saveUser(user);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(savedUser);

        // Build and return response
        return AuthResponse.builder()
                .success(true)
                .message("Registration successful")
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(savedUser.getId())
                        .firstName(savedUser.getFirstName())
                        .lastName(savedUser.getLastName())
                        .email(savedUser.getEmail())
                        .userType(savedUser.getUserType())
                        .mobileNo(savedUser.getMobileNo())
                        .build())
                .build();
    }

    /**
     * Authenticate user and generate JWT token
     * @param request Login request containing email and password
     * @return AuthResponse with JWT token and user information
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().trim().toLowerCase(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Load user details
        User user = userService.findByEmail(request.getEmail().trim().toLowerCase());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        // Build and return response
        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .userType(user.getUserType())
                        .mobileNo(user.getMobileNo())
                        .build())
                .build();
    }
}
