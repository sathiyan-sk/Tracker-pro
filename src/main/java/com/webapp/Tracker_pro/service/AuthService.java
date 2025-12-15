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
     * Register a new user (Admin, Student, HR, or Faculty)
     * @param request Registration request containing user details
     * @return AuthResponse with JWT token and user information
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserType userType = request.getUserType();
        
        // Check if email already exists in any table
        if (adminRepository.existsByEmail(email) || 
            studentRepository.existsByEmail(email) || 
            hrFacultyUserRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                "Email already registered. Please use a different email or login."
            );
        }

        // Check if mobile number already exists
        if (adminRepository.existsByMobileNo(request.getMobileNo()) ||
            studentRepository.existsByMobileNo(request.getMobileNo()) ||
            hrFacultyUserRepository.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException(
                "Mobile number already registered. Please use a different number."
            );
        }

        // Handle registration based on user type
        switch (userType) {
            case ADMIN:
                return registerAdmin(request, email);
            case STUDENT:
                return registerStudent(request, email);
            case HR:
            case FACULTY:
                return registerHROrFaculty(request, email, userType);
            default:
                throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    /**
     * Register a new Admin user
     */
    private AuthResponse registerAdmin(RegisterRequest request, String email) {
        Admin admin = new Admin();
        admin.setFirstName(request.getFirstName().trim());
        admin.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setMobileNo(request.getMobileNo());
        admin.setGender(request.getGender());
        admin.setDob(request.getDob());
        admin.setAge(request.getAge());
        admin.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        admin.setIsActive(true);

        Admin savedAdmin = adminRepository.save(admin);
        String jwtToken = jwtService.generateToken(savedAdmin);

        return AuthResponse.builder()
                .success(true)
                .message("Admin registration successful")
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(savedAdmin.getId())
                        .firstName(savedAdmin.getFirstName())
                        .lastName(savedAdmin.getLastName())
                        .email(savedAdmin.getEmail())
                        .userType(UserType.ADMIN)
                        .mobileNo(savedAdmin.getMobileNo())
                        .build())
                .build();
    }

    /**
     * Register a new Student user
     */
    private AuthResponse registerStudent(RegisterRequest request, String email) {
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

        Student savedStudent = studentRepository.save(student);
        String jwtToken = jwtService.generateToken(savedStudent);

        return AuthResponse.builder()
                .success(true)
                .message("Student registration successful")
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
     * Register a new HR or Faculty user
     */
    private AuthResponse registerHROrFaculty(RegisterRequest request, String email, UserType userType) {
        HRFacultyUser hrFacultyUser = new HRFacultyUser();
        hrFacultyUser.setFirstName(request.getFirstName().trim());
        hrFacultyUser.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        hrFacultyUser.setEmail(email);
        hrFacultyUser.setPassword(passwordEncoder.encode(request.getPassword()));
        hrFacultyUser.setMobileNo(request.getMobileNo());
        hrFacultyUser.setGender(request.getGender());
        hrFacultyUser.setDob(request.getDob());
        hrFacultyUser.setAge(request.getAge());
        hrFacultyUser.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        hrFacultyUser.setUserType(userType); // Set the correct user type (HR or FACULTY)
        hrFacultyUser.setIsActive(true);

        HRFacultyUser savedHRFacultyUser = hrFacultyUserRepository.save(hrFacultyUser);
        String jwtToken = jwtService.generateToken(savedHRFacultyUser);

        return AuthResponse.builder()
                .success(true)
                .message(userType.name() + " registration successful")
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(savedHRFacultyUser.getId())
                        .firstName(savedHRFacultyUser.getFirstName())
                        .lastName(savedHRFacultyUser.getLastName())
                        .email(savedHRFacultyUser.getEmail())
                        .userType(userType) // Return the correct user type
                        .mobileNo(savedHRFacultyUser.getMobileNo())
                        .build())
                .build();
    }

    /**
     * Authenticate user and generate JWT token
     * Handles Admin, Student, HR, and Faculty authentication across normalized tables
     * @param request Login request containing email and password
     * @return AuthResponse with JWT token and user information
     */
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        // Try Admin table first
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            if (!passwordEncoder.matches(password, admin.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            String jwtToken = jwtService.generateToken(admin);
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

        // Try Student table
        Optional<Student> studentOptional = studentRepository.findByEmail(email);
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            if (!passwordEncoder.matches(password, student.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            String jwtToken = jwtService.generateToken(student);
            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(jwtToken)
                    .user(AuthResponse.UserInfo.builder()
                            .id(student.getId())
                            .firstName(student.getFirstName())
                            .lastName(student.getLastName())
                            .email(student.getEmail())
                            .userType(UserType.STUDENT)
                            .mobileNo(student.getMobileNo())
                            .build())
                    .build();
        }

        // Try HR/Faculty table
        Optional<HRFacultyUser> hrFacultyOptional = hrFacultyUserRepository.findByEmail(email);
        if (hrFacultyOptional.isPresent()) {
            HRFacultyUser hrFacultyUser = hrFacultyOptional.get();
            if (!passwordEncoder.matches(password, hrFacultyUser.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            String jwtToken = jwtService.generateToken(hrFacultyUser);
            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(jwtToken)
                    .user(AuthResponse.UserInfo.builder()
                            .id(hrFacultyUser.getId())
                            .firstName(hrFacultyUser.getFirstName())
                            .lastName(hrFacultyUser.getLastName())
                            .email(hrFacultyUser.getEmail())
                            .userType(hrFacultyUser.getUserType())
                            .mobileNo(hrFacultyUser.getMobileNo())
                            .build())
                    .build();
        }

        // No user found in any table
        throw new InvalidCredentialsException("Invalid email or password");
    }
}
