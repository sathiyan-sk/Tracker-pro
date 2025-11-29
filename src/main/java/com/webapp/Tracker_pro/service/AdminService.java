package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.model.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Production-Ready Admin Service
 * Version 2.0 - Refactored for normalized database structure
 * 
 * This service acts as a facade, delegating operations to specialized services:
 * - StudentService: Handles all student-related operations
 * - HRFacultyUserService: Handles HR and Faculty user operations
 * - CareerPostService: Handles internship/career post operations
 * 
 * Benefits:
 * - Clean separation of concerns
 * - Each user type has its own dedicated table and service
 * - Better database normalization
 * - Easier to maintain and extend
 * - Proper transaction management
 * 
 * @author TrackerPro Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceV2 {

    private final StudentService studentService;
    private final HRFacultyUserService hrFacultyUserService;
    private final CareerPostService careerPostService;

    // ==================== Dashboard Statistics ====================

    /**
     * Get comprehensive dashboard statistics
     * 
     * Aggregates data from multiple sources:
     * - Total students from students table
     * - Total HR/Faculty from hr_faculty_users table
     * - Published posts from career_posts table
     * - New students registered in the last 7 days
     * 
     * @return Dashboard statistics response
     */
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating dashboard statistics");

        try {
            // Count total students from students table
            long totalStudents = studentService.countStudents();

            // Count total HR from hr_faculty_users table
            long totalHR = hrFacultyUserService.countHRUsers();

            // Count total Faculty from hr_faculty_users table
            long totalFaculty = hrFacultyUserService.countFacultyUsers();
            long totalFacultyHR = totalHR + totalFaculty;

            // Count new students in the last 7 days
            long newStudentsThisWeek = studentService.countNewStudents();

            // Get published posts count
            long publishedPosts = careerPostService.getPostedPostsCount();

            log.info("Dashboard stats generated - Students: {}, Faculty/HR: {}, Posts: {}, New this week: {}",
                    totalStudents, totalFacultyHR, publishedPosts, newStudentsThisWeek);

            return DashboardStatsResponse.builder()
                    .totalStudents(totalStudents)
                    .totalFacultyHR(totalFacultyHR)
                    .publishedPosts(publishedPosts)
                    .newStudentsThisWeek(newStudentsThisWeek)
                    .success(true)
                    .message("Dashboard statistics fetched successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error generating dashboard statistics", e);
            throw new RuntimeException("Failed to generate dashboard statistics", e);
        }
    }

    // ==================== Student Management Operations ====================

    /**
     * Get all students with optional search filter
     * 
     * @param search Optional search term for filtering by name or email
     * @return List of student responses
     */
    public List<StudentResponse> getAllStudents(String search) {
        log.info("Fetching all students with search: '{}'", search);
        return studentService.getAllStudents(search);
    }

    /**
     * Get student by ID
     * 
     * @param id Student ID
     * @return Student response
     * @throws com.webapp.Tracker_pro.exception.ResourceNotFoundException if student not found
     */
    public StudentResponse getStudentById(Long id) {
        log.info("Fetching student by ID: {}", id);
        return studentService.getStudentById(id);
    }

    /**
     * Delete student by ID
     * 
     * @param id Student ID to delete
     * @throws com.webapp.Tracker_pro.exception.ResourceNotFoundException if student not found
     */
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Deleting student with ID: {}", id);
        studentService.deleteStudent(id);
    }

    /**
     * Delete multiple students in a single transaction
     * 
     * @param ids List of student IDs to delete
     */
    @Transactional
    public void deleteMultipleStudents(List<Long> ids) {
        log.info("Deleting multiple students, count: {}", ids.size());
        studentService.deleteMultipleStudents(ids);
    }

    // ==================== HR/Faculty User Management Operations ====================

    /**
     * Get all HR and Faculty users with optional filter
     * 
     * @param filter Optional filter: 'hr', 'faculty', or null for all
     * @return List of HR/Faculty user responses
     */
    public List<HRFacultyUserResponse> getAllHRFacultyUsers(String filter) {
        log.info("Fetching HR/Faculty users with filter: '{}'", filter);
        return hrFacultyUserService.getAllUsers(filter);
    }

    /**
     * Get HR/Faculty user by ID
     * 
     * @param id User ID
     * @return HR/Faculty user response
     * @throws com.webapp.Tracker_pro.exception.ResourceNotFoundException if user not found
     */
    public HRFacultyUserResponse getHRFacultyUserById(Long id) {
        log.info("Fetching HR/Faculty user by ID: {}", id);
        return hrFacultyUserService.getUserById(id);
    }

    /**
     * Create new HR or Faculty user
     * 
     * Validates that the user type is either HR or FACULTY.
     * Checks for duplicate email and mobile number.
     * Encrypts password before storing.
     * 
     * @param request User creation request
     * @return Created user response
     * @throws com.webapp.Tracker_pro.exception.UserAlreadyExistsException if email or mobile already exists
     * @throws IllegalArgumentException if user type is not HR or FACULTY
     */
    @Transactional
    public HRFacultyUserResponse createHRFacultyUser(CreateUserRequest request) {
        log.info("Creating new HR/Faculty user: {}", request.getEmail());

        // Validate user type
        if (request.getUserType() == null ||
            (!request.getUserType().equalsIgnoreCase("HR") && 
             !request.getUserType().equalsIgnoreCase("FACULTY"))) {
            throw new IllegalArgumentException("User type must be either 'HR' or 'FACULTY'");
        }

        return hrFacultyUserService.createUser(request);
    }

    /**
     * Update existing HR/Faculty user
     * 
     * @param id User ID to update
     * @param request User update request
     * @return Updated user response
     * @throws com.webapp.Tracker_pro.exception.ResourceNotFoundException if user not found
     * @throws com.webapp.Tracker_pro.exception.UserAlreadyExistsException if email or mobile already taken
     */
    @Transactional
    public HRFacultyUserResponse updateHRFacultyUser(Long id, CreateUserRequest request) {
        log.info("Updating HR/Faculty user with ID: {}", id);
        return hrFacultyUserService.updateUser(id, request);
    }

    /**
     * Delete HR/Faculty user by ID
     * 
     * @param id User ID to delete
     * @throws com.webapp.Tracker_pro.exception.ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteHRFacultyUser(Long id) {
        log.info("Deleting HR/Faculty user with ID: {}", id);
        hrFacultyUserService.deleteUser(id);
    }

    /**
     * Toggle HR/Faculty user status (Enable/Disable)
     * 
     * @param id User ID
     * @param isEnabled True to enable, false to disable
     * @return Updated user response
     * @throws com.webapp.Tracker_pro.exception.ResourceNotFoundException if user not found
     */
    @Transactional
    public HRFacultyUserResponse toggleHRFacultyUserStatus(Long id, Boolean isEnabled) {
        log.info("Toggling HR/Faculty user status - ID: {}, isEnabled: {}", id, isEnabled);
        return hrFacultyUserService.toggleUserStatus(id, isEnabled);
    }

    // ==================== Statistics and Analytics ====================

    /**
     * Get total count of students
     * 
     * @return Total number of students
     */
    public long getTotalStudentsCount() {
        return studentService.countStudents();
    }

    /**
     * Get total count of HR users
     * 
     * @return Total number of HR users
     */
    public long getTotalHRCount() {
        return hrFacultyUserService.countHRUsers();
    }

    /**
     * Get total count of Faculty users
     * 
     * @return Total number of Faculty users
     */
    public long getTotalFacultyCount() {
        return hrFacultyUserService.countFacultyUsers();
    }

    /**
     * Get count of new students in the last 7 days
     * 
     * @return Count of new students
     */
    public long getNewStudentsCount() {
        return studentService.countNewStudents();
    }

    /**
     * Get count of published career posts
     * 
     * @return Count of published posts
     */
    public long getPublishedPostsCount() {
        return careerPostService.getPostedPostsCount();
    }
}
