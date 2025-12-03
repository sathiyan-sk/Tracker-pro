package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.ApplicationRequest;
import com.webapp.Tracker_pro.dto.StudentProfileRequest;
import com.webapp.Tracker_pro.service.StudentCareerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Student Career operations
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class StudentController {

    private final StudentCareerService studentCareerService;

    // ==================== Profile Management ====================

    /**
     * Get student profile
     * @endpoint GET /api/student/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        log.info("Getting profile for student: {}", authentication.getName());
        Map<String, Object> response = studentCareerService.getStudentProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Update student profile
     * @endpoint PUT /api/student/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody StudentProfileRequest request) {
        log.info("Updating profile for student: {}", authentication.getName());
        Map<String, Object> response = studentCareerService.updateStudentProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    // ==================== Internship Browsing ====================

    /**
     * Get all published internships (students see only published posts)
     * @endpoint GET /api/student/internships
     */
    @GetMapping("/internships")
    public ResponseEntity<Map<String, Object>> getAvailableInternships(
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String search) {
        log.info("Getting available internships - workMode: {}, search: {}", workMode, search);
        Map<String, Object> response = studentCareerService.getAvailableInternships(workMode, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get internship details
     * @endpoint GET /api/student/internships/{id}
     */
    @GetMapping("/internships/{id}")
    public ResponseEntity<Map<String, Object>> getInternshipDetails(@PathVariable Long id) {
        log.info("Getting internship details for ID: {}", id);
        Map<String, Object> response = studentCareerService.getInternshipDetails(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if already applied
     * @endpoint GET /api/student/internships/{id}/check-application
     */
    @GetMapping("/internships/{id}/check-application")
    public ResponseEntity<Map<String, Object>> checkIfApplied(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Checking if student {} has applied for internship {}", authentication.getName(), id);
        Map<String, Object> response = studentCareerService.checkIfApplied(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    // ==================== Application Management ====================

    /**
     * Apply for internship
     * @endpoint POST /api/student/applications
     */
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> applyForInternship(
            Authentication authentication,
            @Valid @RequestBody ApplicationRequest request) {
        log.info("Student {} applying for internship {}", authentication.getName(), request.getCareerPostId());
        Map<String, Object> response = studentCareerService.applyForInternship(authentication.getName(), request);
        
        if (response.get("success").equals(false)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all my applications
     * @endpoint GET /api/student/applications
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getMyApplications(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        log.info("Getting applications for student: {} with status: {}", authentication.getName(), status);
        Map<String, Object> response = studentCareerService.getMyApplications(authentication.getName(), status);
        return ResponseEntity.ok(response);
    }

    /**
     * Get application details
     * @endpoint GET /api/student/applications/{id}
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationDetails(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Getting application details for ID: {} by student: {}", id, authentication.getName());
        Map<String, Object> response = studentCareerService.getApplicationDetails(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraw application
     * @endpoint DELETE /api/student/applications/{id}
     */
    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> withdrawApplication(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Student {} withdrawing application {}", authentication.getName(), id);
        Map<String, Object> response = studentCareerService.withdrawApplication(authentication.getName(), id);
        
        if (response.get("success").equals(false)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard ====================

    /**
     * Get dashboard statistics
     * @endpoint GET /api/student/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        log.info("Getting dashboard stats for student: {}", authentication.getName());
        Map<String, Object> response = studentCareerService.getDashboardStats(authentication.getName());
        return ResponseEntity.ok(response);
    }

    // ==================== Notifications ====================

    /**
     * Get my notifications
     * @endpoint GET /api/student/notifications
     */
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly) {
        log.info("Getting notifications for student: {} (unreadOnly: {})", authentication.getName(), unreadOnly);
        Map<String, Object> response = studentCareerService.getNotifications(authentication.getName(), unreadOnly);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark notification as read
     * @endpoint PATCH /api/student/notifications/{id}/read
     */
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        log.info("Marking notification {} as read for student: {}", id, authentication.getName());
        Map<String, Object> response = studentCareerService.markNotificationAsRead(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read
     * @endpoint PATCH /api/student/notifications/read-all
     */
    @PatchMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        log.info("Marking all notifications as read for student: {}", authentication.getName());
        Map<String, Object> response = studentCareerService.markAllNotificationsAsRead(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
