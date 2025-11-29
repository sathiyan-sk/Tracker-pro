package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.AuthResponse;
import com.webapp.Tracker_pro.dto.LoginRequest;
import com.webapp.Tracker_pro.dto.RegisterRequest;
import com.webapp.Tracker_pro.exception.InvalidCredentialsException;
import com.webapp.Tracker_pro.exception.UserAlreadyExistsException;
import com.webapp.Tracker_pro.model.Admin;
import com.webapp.Tracker_pro.model.Student;
import com.webapp.Tracker_pro.model.HRFacultyUser;
import com.webapp.Tracker_pro.model.UserType;
import com.webapp.Tracker_pro.repository.AdminRepository;
import com.webapp.Tracker_pro.repository.StudentRepository;
import com.webapp.Tracker_pro.repository.HRFacultyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for authentication operations including registration and login.
 * Handles Admin, Student, HR, and Faculty authentication with normalized tables.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final HRFacultyUserRepository hrFacultyUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new student user
     * @param request Registration request containing user details
     * @return AuthResponse with JWT token and user information
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        
        // Check if email already exists in any table
        if (adminRepository.existsByEmail(email) || 
            studentRepository.existsByEmail(email) || 
            hrFacultyUserRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                "Email already registered. Please use a different email or login."
            );
        }

        // Check if mobile number already exists
        if (studentRepository.existsByMobileNo(request.getMobileNo()) ||
            hrFacultyUserRepository.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException(
                "Mobile number already registered. Please use a different number."
            );
        }

        // Create new student (registration is only for students)
        Student student = new Student();
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setMobileNo(request.getMobileNo());
        student.setGender(request.getGender());
        student.setDob(request.getDob());
        student.setAge(request.getAge());
        student.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        student.setIsActive(true);

        // Save student to database
        Student savedStudent = studentRepository.save(student);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(savedStudent);

        // Build and return response
        return AuthResponse.builder()
                .success(true)
                .message("Registration successful")
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(savedStudent.getId())
                        .firstName(savedStudent.getFirstName())
                        .lastName(savedStudent.getLastName())
                        .email(savedStudent.getEmail())
                        .userType(UserType.STUDENT)
                        .mobileNo(savedStudent.getMobileNo())
                        .build())
                .build();
    }

    /**
     * Authenticate user and generate JWT token
     * Handles both Admin and User authentication
     * @param request Login request containing email and password
     * @return AuthResponse with JWT token and user information
     */
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        // First, try to find Admin
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            
            // Verify admin password
            if (!passwordEncoder.matches(password, admin.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            // Generate JWT token for admin
            String jwtToken = jwtService.generateToken(admin);

            // Build and return response for admin
            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(jwtToken)
                    .user(AuthResponse.UserInfo.builder()
                            .id(admin.getId())
                            .firstName(admin.getFirstName())
                            .lastName(admin.getLastName())
                            .email(admin.getEmail())
                            .userType(UserType.ADMIN)
                            .mobileNo(admin.getMobileNo())
                            .build())
                    .build();
        }

        // If not admin, try regular user authentication
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Load user details
        User user = userService.findByEmail(email);

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
