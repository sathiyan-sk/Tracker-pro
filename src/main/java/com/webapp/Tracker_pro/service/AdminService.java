package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.CreateUserRequest;
import com.webapp.Tracker_pro.dto.DashboardStatsResponse;
import com.webapp.Tracker_pro.dto.UserListResponse;
import com.webapp.Tracker_pro.exception.ResourceNotFoundException;
import com.webapp.Tracker_pro.exception.UserAlreadyExistsException;
import com.webapp.Tracker_pro.model.User;
import com.webapp.Tracker_pro.model.UserType;
import com.webapp.Tracker_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin operations including user management and dashboard statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CareerPostService careerPostService;

    /**
     * Get dashboard statistics
     * @return Dashboard stats response
     */
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard statistics");

        // Count total students
        long totalStudents = userRepository.countByUserType(UserType.STUDENT);

        // Count total HR and Faculty
        long totalHR = userRepository.countByUserType(UserType.HR);
        long totalFaculty = userRepository.countByUserType(UserType.FACULTY);
        long totalFacultyHR = totalHR + totalFaculty;

        // Count new students in the last 7 days
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        long newStudentsThisWeek = userRepository.countByCreatedAtAfter(oneWeekAgo);

        // Published posts - for now returning 0 as internship posts feature is not yet implemented
        long publishedPosts = 0;

        log.info("Dashboard stats - Students: {}, Faculty/HR: {}, New this week: {}",
                totalStudents, totalFacultyHR, newStudentsThisWeek);

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalFacultyHR(totalFacultyHR)
                .publishedPosts(publishedPosts)
                .newStudentsThisWeek(newStudentsThisWeek)
                .success(true)
                .message("Dashboard statistics fetched successfully")
                .build();
    }

    /**
     * Get all users with optional search and role filter
     * @param search Search term for name or email
     * @param role Role filter (STUDENT, HR, FACULTY, ADMIN, or "all")
     * @return List of users
     */
    public List<UserListResponse> getAllUsers(String search, String role) {
        log.info("Fetching all users with search: '{}', role: '{}'", search, role);

        List<User> users;

        if (search != null && !search.trim().isEmpty()) {
            // Search with optional role filter
            if (role != null && !role.equalsIgnoreCase("all")) {
                UserType userType = UserType.valueOf(role.toUpperCase());
                users = userRepository.searchByUserType(search, userType);
            } else {
                users = userRepository.searchAllUsers(search);
            }
        } else if (role != null && !role.equalsIgnoreCase("all")) {
            // Filter by role only
            UserType userType = UserType.valueOf(role.toUpperCase());
            users = userRepository.findByUserType(userType);
        } else {
            // Get all users
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::mapToUserListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role (HR and Faculty only)
     * @param filter Filter parameter (can be "all", "hr", "faculty")
     * @return List of HR and Faculty users
     */
    public List<UserListResponse> getUsersByRole(String filter) {
        log.info("Fetching users by role filter: '{}'", filter);

        List<User> users;

        if (filter != null && filter.equalsIgnoreCase("hr")) {
            users = userRepository.findByUserType(UserType.HR);
        } else if (filter != null && filter.equalsIgnoreCase("faculty")) {
            users = userRepository.findByUserType(UserType.FACULTY);
        } else {
            // Get both HR and Faculty
            users = userRepository.findByUserTypeIn(Arrays.asList(UserType.HR, UserType.FACULTY));
        }

        return users.stream()
                .map(this::mapToUserListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new user (HR or Faculty)
     * @param request Create user request
     * @return Created user response
     */
    @Transactional
    public UserListResponse createUser(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());

        // Validate user type (only HR and FACULTY allowed)
        if (!request.getUserType().equalsIgnoreCase("HR") && 
            !request.getUserType().equalsIgnoreCase("FACULTY")) {
            throw new IllegalArgumentException("Only HR and FACULTY users can be created through this endpoint");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Check if mobile number already exists
        if (userRepository.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException("User with mobile number " + request.getMobileNo() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNo(request.getMobileNo());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setAge(request.getAge());
        user.setLocation(request.getLocation());
        user.setUserType(UserType.valueOf(request.getUserType().toUpperCase()));
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapToUserListResponse(savedUser);
    }

    /**
     * Get user by ID
     * @param id User ID
     * @return User details
     */
    public UserListResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return mapToUserListResponse(user);
    }

    /**
     * Update user
     * @param id User ID
     * @param request Update user request
     * @return Updated user response
     */
    @Transactional
    public UserListResponse updateUser(Long id, CreateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Check if mobile number is being changed and already exists
        if (!user.getMobileNo().equals(request.getMobileNo()) && 
            userRepository.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException("User with mobile number " + request.getMobileNo() + " already exists");
        }

        // Update user fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        
        // Only update password if it's provided and different
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        user.setMobileNo(request.getMobileNo());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setAge(request.getAge());
        user.setLocation(request.getLocation());

        // Update user type if allowed
        if (request.getUserType() != null) {
            user.setUserType(UserType.valueOf(request.getUserType().toUpperCase()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return mapToUserListResponse(updatedUser);
    }

    /**
     * Delete user by ID
     * @param id User ID
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Prevent deletion of admin users
        if (user.getUserType() == UserType.ADMIN) {
            throw new IllegalArgumentException("Admin users cannot be deleted");
        }

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }

    /**
     * Delete multiple users
     * @param ids List of user IDs to delete
     */
    @Transactional
    public void deleteMultipleUsers(List<Long> ids) {
        log.info("Deleting multiple users, count: {}", ids.size());

        for (Long id : ids) {
            try {
                deleteUser(id);
            } catch (Exception e) {
                log.error("Error deleting user with ID: {}", id, e);
                // Continue with next user even if one fails
            }
        }

        log.info("Bulk delete completed");
    }

    /**
     * Map User entity to UserListResponse DTO
     * @param user User entity
     * @return UserListResponse DTO
     */
    private UserListResponse mapToUserListResponse(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .userType(user.getUserType())
                .gender(user.getGender())
                .dob(user.getDob())
                .age(user.getAge())
                .location(user.getLocation())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
