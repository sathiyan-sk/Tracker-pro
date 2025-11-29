package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.service.AdminServiceV2;
import com.webapp.Tracker_pro.service.CareerPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Production-Ready REST Controller for Admin Operations
 * Version 2.0 - Refactored for normalized database tables
 * 
 * Features:
 * - Clean separation of concerns
 * - Comprehensive error handling
 * - Consistent API response format
 * - RESTful endpoint design
 * - Proper logging
 * 
 * @author TrackerPro Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AdminControllerV2 {

    private final AdminServiceV2 adminService;
    private final CareerPostService careerPostService;

    // ==================== Dashboard Endpoints ====================

    /**
     * Get dashboard statistics
     * 
     * @return Dashboard stats including total students, faculty/HR, posts, and new students
     * @endpoint GET /api/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        try {
            DashboardStatsResponse stats = adminService.getDashboardStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "totalStudents", stats.getTotalStudents(),
                "totalFacultyHR", stats.getTotalFacultyHR(),
                "publishedPosts", stats.getPublishedPosts(),
                "newStudentsThisWeek", stats.getNewStudentsThisWeek()
            ));
        } catch (Exception e) {
            log.error("Error fetching dashboard stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error fetching dashboard statistics"));
        }
    }

    // ==================== Student Registration Endpoints ====================

    /**
     * Get all student registrations with optional search filter
     * 
     * @param search Optional search term for filtering by name or email
     * @param role Optional role filter (kept for compatibility, always filters for STUDENT)
     * @return List of all student registrations
     * @endpoint GET /api/registrations?search=&role=
     */
    @GetMapping("/registrations")
    public ResponseEntity<Map<String, Object>> getAllRegistrations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        log.info("Fetching all registrations - search: '{}', role: '{}'", search, role);
        try {
            List<StudentResponse> students = adminService.getAllStudents(search);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", students,
                "total", students.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching registrations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error fetching registrations"));
        }
    }

    /**
     * Get registration by ID
     * 
     * @param id Student registration ID
     * @return Student details
     * @endpoint GET /api/registrations/{id}
     */
    @GetMapping("/registrations/{id}")
    public ResponseEntity<Map<String, Object>> getRegistrationById(@PathVariable Long id) {
        log.info("Fetching registration by ID: {}", id);
        try {
            StudentResponse student = adminService.getStudentById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", student
            ));
        } catch (Exception e) {
            log.error("Error fetching registration with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Registration not found"));
        }
    }

    /**
     * Delete student registration
     * 
     * @param id Student registration ID to delete
     * @return Success message
     * @endpoint DELETE /api/registrations/{id}
     */
    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Map<String, Object>> deleteRegistration(@PathVariable Long id) {
        log.info("Deleting registration with ID: {}", id);
        try {
            adminService.deleteStudent(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registration deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting registration with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting registration"));
        }
    }

    /**
     * Delete multiple registrations
     * 
     * @param payload JSON body containing array of IDs to delete
     * @return Success message with count
     * @endpoint POST /api/registrations/delete-multiple
     */
    @PostMapping("/registrations/delete-multiple")
    public ResponseEntity<Map<String, Object>> deleteMultipleRegistrations(
            @RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids");
        log.info("Deleting multiple registrations, count: {}", ids.size());
        try {
            adminService.deleteMultipleStudents(ids);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ids.size() + " registration(s) deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting multiple registrations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting registrations"));
        }
    }

    /**
     * Export registrations data
     * 
     * @return All registrations data for export
     * @endpoint GET /api/registrations/export
     */
    @GetMapping("/registrations/export")
    public ResponseEntity<Map<String, Object>> exportRegistrations() {
        log.info("Exporting registrations data");
        try {
            List<StudentResponse> students = adminService.getAllStudents(null);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", students,
                "message", "Data exported successfully"
            ));
        } catch (Exception e) {
            log.error("Error exporting registrations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error exporting data"));
        }
    }

    // ==================== User Management Endpoints (HR/Faculty) ====================

    /**
     * Get all users (HR and Faculty only)
     * 
     * @param filter Optional filter: 'hr', 'faculty', or null for all
     * @return List of HR and Faculty users
     * @endpoint GET /api/users?filter=
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(required = false) String filter) {
        log.info("Fetching users with filter: '{}'", filter);
        try {
            List<HRFacultyUserResponse> users = adminService.getAllHRFacultyUsers(filter);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users,
                "total", users.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error fetching users"));
        }
    }

    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User details
     * @endpoint GET /api/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);
        try {
            HRFacultyUserResponse user = adminService.getHRFacultyUserById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user
            ));
        } catch (Exception e) {
            log.error("Error fetching user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "User not found"));
        }
    }

    /**
     * Create new user (HR or Faculty)
     * 
     * @param request User creation request with all required fields
     * @return Created user details
     * @endpoint POST /api/users
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        try {
            HRFacultyUserResponse user = adminService.createHRFacultyUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "User created successfully",
                "data", user
            ));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update existing user
     * 
     * @param id User ID to update
     * @param request User update request
     * @return Updated user details
     * @endpoint PUT /api/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        try {
            HRFacultyUserResponse user = adminService.updateHRFacultyUser(id, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User updated successfully",
                "data", user
            ));
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Delete user
     * 
     * @param id User ID to delete
     * @return Success message
     * @endpoint DELETE /api/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        try {
            adminService.deleteHRFacultyUser(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting user"));
        }
    }

    /**
     * Toggle user status (Enable/Disable)
     * 
     * @param id User ID
     * @param payload JSON body with isEnabled field
     * @return Updated user details
     * @endpoint PATCH /api/users/{id}/toggle-status
     */
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        Boolean isEnabled = payload.get("isEnabled");
        log.info("Toggling user status - ID: {}, isEnabled: {}", id, isEnabled);
        try {
            HRFacultyUserResponse user = adminService.toggleHRFacultyUserStatus(id, isEnabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User status updated successfully",
                "data", user
            ));
        } catch (Exception e) {
            log.error("Error toggling user status for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating user status"));
        }
    }

    // ==================== Internship/Career Post Endpoints ====================

    /**
     * Get all internships/career posts
     * 
     * @param status Optional status filter: 'Posted' or 'Draft'
     * @param workMode Optional work mode filter: 'Online', 'Offline', or 'Hybrid'
     * @return List of career posts
     * @endpoint GET /api/internships?status=&workMode=
     */
    @GetMapping("/internships")
    public ResponseEntity<Map<String, Object>> getAllInternships(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workMode) {
        log.info("Fetching all internships - status: '{}', workMode: '{}'", status, workMode);
        try {
            List<CareerPostResponse> posts = careerPostService.getAllPosts();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", posts,
                "total", posts.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching internships", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error fetching internships"));
        }
    }

    /**
     * Get internship by ID
     * 
     * @param id Internship ID
     * @return Internship details
     * @endpoint GET /api/internships/{id}
     */
    @GetMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> getInternshipById(@PathVariable Long id) {
        log.info("Fetching internship by ID: {}", id);
        try {
            CareerPostResponse post = careerPostService.getPostById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", post
            ));
        } catch (Exception e) {
            log.error("Error fetching internship with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Internship not found"));
        }
    }

    /**
     * Create new internship/career post
     * 
     * @param request Career post creation request
     * @param adminId Admin ID (should be extracted from JWT in production)
     * @return Created career post
     * @endpoint POST /api/internships
     */
    @PostMapping("/internships")
    public ResponseEntity<Map<String, Object>> createInternship(
            @Valid @RequestBody CareerPostRequest request,
            @RequestParam(required = false, defaultValue = "1") Long adminId) {
        log.info("Creating new internship post");
        try {
            CareerPostResponse post = careerPostService.createPost(request, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Internship created successfully",
                "data", post
            ));
        } catch (Exception e) {
            log.error("Error creating internship", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update existing internship
     * 
     * @param id Internship ID
     * @param request Career post update request
     * @return Updated career post
     * @endpoint PUT /api/internships/{id}
     */
    @PutMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> updateInternship(
            @PathVariable Long id,
            @Valid @RequestBody CareerPostRequest request) {
        log.info("Updating internship with ID: {}", id);
        try {
            CareerPostResponse post = careerPostService.updatePost(id, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Internship updated successfully",
                "data", post
            ));
        } catch (Exception e) {
            log.error("Error updating internship with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Delete internship
     * 
     * @param id Internship ID to delete
     * @return Success message
     * @endpoint DELETE /api/internships/{id}
     */
    @DeleteMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> deleteInternship(@PathVariable Long id) {
        log.info("Deleting internship with ID: {}", id);
        try {
            careerPostService.deletePost(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Internship deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting internship with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting internship"));
        }
    }

    /**
     * Toggle internship status (Publish/Unpublish)
     * 
     * @param id Internship ID
     * @param payload JSON body with status field ('Posted' or 'Draft')
     * @return Updated career post
     * @endpoint PATCH /api/internships/{id}/toggle-status
     */
    @PatchMapping("/internships/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleInternshipStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        log.info("Toggling internship status - ID: {}, status: {}", id, status);
        try {
            // Update the status through the update endpoint
            CareerPostResponse post = careerPostService.getPostById(id);
            CareerPostRequest updateRequest = CareerPostRequest.builder()
                .code(post.getCode())
                .title(post.getTitle())
                .duration(post.getDuration())
                .workMode(post.getWorkMode())
                .prerequisites(post.getPrerequisites())
                .description(post.getDescription())
                .status(status)
                .build();
            
            CareerPostResponse updatedPost = careerPostService.updatePost(id, updateRequest);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Internship status updated successfully",
                "data", updatedPost
            ));
        } catch (Exception e) {
            log.error("Error toggling internship status for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating internship status"));
        }
    }

    /**
     * Search internships by title or code
     * 
     * @param term Search term
     * @return List of matching career posts
     * @endpoint GET /api/internships/search?term=
     */
    @GetMapping("/internships/search")
    public ResponseEntity<Map<String, Object>> searchInternships(@RequestParam String term) {
        log.info("Searching internships with term: '{}'", term);
        try {
            List<CareerPostResponse> posts = careerPostService.searchPosts(term);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", posts,
                "total", posts.size()
            ));
        } catch (Exception e) {
            log.error("Error searching internships", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error searching internships"));
        }
    }
}
