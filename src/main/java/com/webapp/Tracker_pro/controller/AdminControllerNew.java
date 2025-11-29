package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.service.CareerPostService;
import com.webapp.Tracker_pro.service.HRFacultyUserService;
import com.webapp.Tracker_pro.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API Controller matching admin-api.js structure
 * Base path: /api (not /api/v1)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminControllerNew {

    private final StudentService studentService;
    private final HRFacultyUserService hrFacultyUserService;
    private final CareerPostService careerPostService;

    // ==================== DASHBOARD ====================

    /**
     * Get dashboard statistics
     * GET /api/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalStudents = studentService.countStudents();
        long totalHR = hrFacultyUserService.countHRUsers();
        long totalFaculty = hrFacultyUserService.countFacultyUsers();
        long totalFacultyHR = totalHR + totalFaculty;
        long publishedPosts = careerPostService.getPostedPostsCount();
        long newStudentsThisWeek = studentService.countNewStudents();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "totalStudents", totalStudents,
            "totalFacultyHR", totalFacultyHR,
            "publishedPosts", publishedPosts,
            "newStudentsThisWeek", newStudentsThisWeek
        ));
    }

    // ==================== REGISTRATIONS (STUDENTS) ====================

    /**
     * Get all student registrations
     * GET /api/registrations
     */
    @GetMapping("/registrations")
    public ResponseEntity<Map<String, Object>> getAllRegistrations(
            @RequestParam(required = false) String search) {
        List<StudentResponse> students = studentService.getAllStudents(search);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", students,
            "total", students.size()
        ));
    }

    /**
     * Get registration by ID
     * GET /api/registrations/{id}
     */
    @GetMapping("/registrations/{id}")
    public ResponseEntity<Map<String, Object>> getRegistrationById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", student
        ));
    }

    /**
     * Delete single registration
     * DELETE /api/registrations/{id}
     */
    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Map<String, Object>> deleteRegistration(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Registration deleted successfully"
        ));
    }

    /**
     * Delete multiple registrations
     * POST /api/registrations/delete-multiple
     */
    @PostMapping("/registrations/delete-multiple")
    public ResponseEntity<Map<String, Object>> deleteMultipleRegistrations(
            @RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids");
        studentService.deleteMultipleStudents(ids);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", ids.size() + " registration(s) deleted successfully"
        ));
    }

    /**
     * Export registrations data
     * GET /api/registrations/export
     */
    @GetMapping("/registrations/export")
    public ResponseEntity<Map<String, Object>> exportRegistrations() {
        List<StudentResponse> students = studentService.getAllStudents(null);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", students,
            "message", "Data exported successfully"
        ));
    }

    // ==================== USERS (HR/FACULTY) ====================

    /**
     * Get all HR/Faculty users
     * GET /api/users?role=...
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(required = false) String role) {
        List<HRFacultyUserResponse> users = hrFacultyUserService.getAllUsers(role);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", users,
            "total", users.size()
        ));
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        HRFacultyUserResponse user = hrFacultyUserService.getUserById(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", user
        ));
    }

    /**
     * Create new user (HR/Faculty)
     * POST /api/users
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        HRFacultyUserResponse user = hrFacultyUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "User created successfully",
            "data", user
        ));
    }

    /**
     * Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        HRFacultyUserResponse user = hrFacultyUserService.updateUser(id, request);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User updated successfully",
            "data", user
        ));
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        hrFacultyUserService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User deleted successfully"
        ));
    }

    /**
     * Toggle user status
     * PATCH /api/users/{id}/toggle-status
     */
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        Boolean isEnabled = payload.get("isEnabled");
        HRFacultyUserResponse user = hrFacultyUserService.toggleUserStatus(id, isEnabled);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User status updated successfully",
            "data", user
        ));
    }

    // ==================== INTERNSHIPS (CAREER POSTS) ====================

    /**
     * Get all internships
     * GET /api/internships
     */
    @GetMapping("/internships")
    public ResponseEntity<Map<String, Object>> getAllInternships(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workMode) {
        List<CareerPostResponse> posts = careerPostService.getAllPosts();
        
        // Apply filters if provided
        if (status != null && !status.isEmpty()) {
            posts = posts.stream()
                .filter(p -> p.getStatus().equalsIgnoreCase(status))
                .toList();
        }
        if (workMode != null && !workMode.isEmpty()) {
            posts = posts.stream()
                .filter(p -> p.getWorkMode().equalsIgnoreCase(workMode))
                .toList();
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", posts,
            "total", posts.size()
        ));
    }

    /**
     * Get internship by ID
     * GET /api/internships/{id}
     */
    @GetMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> getInternshipById(@PathVariable Long id) {
        CareerPostResponse post = careerPostService.getPostById(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", post
        ));
    }

    /**
     * Create new internship
     * POST /api/internships
     */
    @PostMapping("/internships")
    public ResponseEntity<Map<String, Object>> createInternship(
            @Valid @RequestBody CareerPostRequest request,
            @RequestParam(required = false, defaultValue = "1") Long adminId) {
        CareerPostResponse post = careerPostService.createPost(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "Internship created successfully",
            "data", post
        ));
    }

    /**
     * Update internship
     * PUT /api/internships/{id}
     */
    @PutMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> updateInternship(
            @PathVariable Long id,
            @Valid @RequestBody CareerPostRequest request) {
        CareerPostResponse post = careerPostService.updatePost(id, request);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Internship updated successfully",
            "data", post
        ));
    }

    /**
     * Delete internship
     * DELETE /api/internships/{id}
     */
    @DeleteMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> deleteInternship(@PathVariable Long id) {
        careerPostService.deletePost(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Internship deleted successfully"
        ));
    }

    /**
     * Toggle internship status
     * PATCH /api/internships/{id}/toggle-status
     */
    @PatchMapping("/internships/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleInternshipStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        CareerPostRequest updateRequest = new CareerPostRequest();
        // We need to get the existing post first
        CareerPostResponse existingPost = careerPostService.getPostById(id);
        updateRequest.setCode(existingPost.getCode());
        updateRequest.setTitle(existingPost.getTitle());
        updateRequest.setDuration(existingPost.getDuration());
        updateRequest.setWorkMode(existingPost.getWorkMode());
        updateRequest.setPrerequisites(existingPost.getPrerequisites());
        updateRequest.setDescription(existingPost.getDescription());
        updateRequest.setStatus(status);
        
        CareerPostResponse post = careerPostService.updatePost(id, updateRequest);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Internship status updated successfully",
            "data", post
        ));
    }

    // ==================== COMPLAINTS (Future Feature) ====================

    /**
     * Get all complaints
     * GET /api/complaints
     */
    @GetMapping("/complaints")
    public ResponseEntity<Map<String, Object>> getAllComplaints() {
        // Future implementation
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", List.of(),
            "total", 0,
            "message", "Complaints feature coming soon"
        ));
    }
}
