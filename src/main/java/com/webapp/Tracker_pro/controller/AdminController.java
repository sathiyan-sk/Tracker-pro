package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.model.UserType;
import com.webapp.Tracker_pro.service.AdminService;
import com.webapp.Tracker_pro.service.CareerPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for admin operations
 * DEPRECATED: Replaced by AdminControllerV2 with normalized database structure
 * Keeping this file for reference only
 */
// @RestController - DISABLED: Using AdminControllerV2 instead
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final CareerPostService careerPostService;

    /**
     * Get dashboard statistics
     * GET /api/v1/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all registrations (all users)
     * GET /api/v1/registrations
     */
    @GetMapping("/registrations")
    public ResponseEntity<Map<String, Object>> getAllRegistrations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        List<UserListResponse> registrations = adminService.getAllUsers(search, role);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", registrations,
            "total", registrations.size()
        ));
    }

    /**
     * Get users (HR and Faculty only)
     * GET /api/v1/users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(required = false) String filter) {
        List<UserListResponse> users = adminService.getUsersByRole(filter);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", users,
            "total", users.size()
        ));
    }

    /**
     * Create new user (HR/Faculty)
     * POST /api/v1/users
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserListResponse user = adminService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "User created successfully",
            "data", user
        ));
    }

    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserListResponse user = adminService.getUserById(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", user
        ));
    }

    /**
     * Update user
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        UserListResponse user = adminService.updateUser(id, request);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User updated successfully",
            "data", user
        ));
    }

    /**
     * Delete user
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User deleted successfully"
        ));
    }

    /**
     * Delete registration (student)
     * DELETE /api/v1/registrations/{id}
     */
    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Map<String, Object>> deleteRegistration(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Registration deleted successfully"
        ));
    }

    /**
     * Delete multiple registrations
     * POST /api/v1/registrations/delete-multiple
     */
    @PostMapping("/registrations/delete-multiple")
    public ResponseEntity<Map<String, Object>> deleteMultipleRegistrations(
            @RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids");
        adminService.deleteMultipleUsers(ids);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", ids.size() + " registration(s) deleted successfully"
        ));
    }

    /**
     * Export registrations data
     * GET /api/v1/registrations/export
     */
    @GetMapping("/registrations/export")
    public ResponseEntity<Map<String, Object>> exportRegistrations() {
        List<UserListResponse> registrations = adminService.getAllUsers(null, null);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", registrations,
            "message", "Data exported successfully"
        ));
    }

    // ==================== Career Posts Endpoints ====================

    /**
     * Get all career posts
     * GET /api/v1/career-posts
     */
    @GetMapping("/career-posts")
    public ResponseEntity<Map<String, Object>> getAllCareerPosts() {
        List<CareerPostResponse> posts = careerPostService.getAllPosts();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", posts,
            "total", posts.size()
        ));
    }

    /**
     * Get career post by ID
     * GET /api/v1/career-posts/{id}
     */
    @GetMapping("/career-posts/{id}")
    public ResponseEntity<Map<String, Object>> getCareerPostById(@PathVariable Long id) {
        CareerPostResponse post = careerPostService.getPostById(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", post
        ));
    }

    /**
     * Create new career post
     * POST /api/v1/career-posts
     * Note: Admin ID should be extracted from JWT token in production
     */
    @PostMapping("/career-posts")
    public ResponseEntity<Map<String, Object>> createCareerPost(
            @Valid @RequestBody CareerPostRequest request,
            @RequestParam(required = false, defaultValue = "1") Long adminId) {
        CareerPostResponse post = careerPostService.createPost(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "Career post created successfully",
            "data", post
        ));
    }

    /**
     * Update career post
     * PUT /api/v1/career-posts/{id}
     */
    @PutMapping("/career-posts/{id}")
    public ResponseEntity<Map<String, Object>> updateCareerPost(
            @PathVariable Long id,
            @Valid @RequestBody CareerPostRequest request) {
        CareerPostResponse post = careerPostService.updatePost(id, request);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Career post updated successfully",
            "data", post
        ));
    }

    /**
     * Delete career post
     * DELETE /api/v1/career-posts/{id}
     */
    @DeleteMapping("/career-posts/{id}")
    public ResponseEntity<Map<String, Object>> deleteCareerPost(@PathVariable Long id) {
        careerPostService.deletePost(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Career post deleted successfully"
        ));
    }

    /**
     * Search career posts by title or code
     * GET /api/v1/career-posts/search?term=...
     */
    @GetMapping("/career-posts/search")
    public ResponseEntity<Map<String, Object>> searchCareerPosts(
            @RequestParam String term) {
        List<CareerPostResponse> posts = careerPostService.searchPosts(term);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", posts,
            "total", posts.size()
        ));
    }
}
