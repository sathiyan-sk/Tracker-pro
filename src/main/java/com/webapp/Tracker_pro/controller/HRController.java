package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.service.HRApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for HR Module Operations
 * Handles student application management from HR perspective
 * 
 * All endpoints require HR or ADMIN role
 * 
 * Endpoints:
 * - GET    /api/hr/applications               - Get all applications with filters
 * - GET    /api/hr/applications/{id}          - Get single application details
 * - PUT    /api/hr/applications/{id}/status   - Update application status
 * - PUT    /api/hr/applications/{id}/notes    - Update HR notes
 * - PUT    /api/hr/applications/bulk-update   - Bulk status update
 * - GET    /api/hr/applications/shortlisted   - Get shortlisted applications
 * - GET    /api/hr/dashboard/stats            - Get dashboard statistics
 * 
 * @author TrackerPro Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@PreAuthorize("hasAnyRole('HR', 'ADMIN')")
public class HRController {

    private final HRApplicationService hrApplicationService;

    // ==================== Applications Management ====================

    /**
     * Get all applications with optional filters
     * 
     * @param search       Optional search term for name/email
     * @param status       Optional status filter
     * @param dateFrom     Optional start date filter (yyyy-MM-dd)
     * @param dateTo       Optional end date filter (yyyy-MM-dd)
     * @param internshipId Optional internship ID filter
     * @return List of applications
     * @endpoint GET /api/hr/applications
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getApplications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long internshipId) {
        
        log.info("HR fetching applications - search: '{}', status: '{}', dateFrom: {}, dateTo: {}, internshipId: {}",
                search, status, dateFrom, dateTo, internshipId);
        
        try {
            List<HRApplicationSummaryResponse> applications = hrApplicationService.getAllApplications(
                    search, status, dateFrom, dateTo, internshipId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Applications retrieved successfully",
                    "total", applications.size(),
                    "data", applications
            ));
        } catch (Exception e) {
            log.error("Error fetching applications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error fetching applications: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get single application by ID with full details
     * 
     * @param id Application ID
     * @return Application details
     * @endpoint GET /api/hr/applications/{id}
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationById(@PathVariable Long id) {
        log.info("HR fetching application details for ID: {}", id);
        
        try {
            HRApplicationDetailResponse application = hrApplicationService.getApplicationById(id);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", application
            ));
        } catch (Exception e) {
            log.error("Error fetching application with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "Application not found with ID: " + id
                    ));
        }
    }

    /**
     * Update application status
     * 
     * @param id      Application ID
     * @param request Status update request with new status and optional notes
     * @return Updated application
     * @endpoint PUT /api/hr/applications/{id}/status
     */
    @PutMapping("/applications/{id}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        
        log.info("HR updating application {} status to '{}'", id, request.getStatus());
        
        try {
            Long hrUserId = getCurrentUserId();
            HRApplicationDetailResponse application = hrApplicationService.updateApplicationStatus(
                    id, request.getStatus(), request.getHrNotes(), hrUserId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Application status updated successfully",
                    "data", application
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error updating application status for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error updating application status: " + e.getMessage()
                    ));
        }
    }

    /**
     * Update HR notes on application
     * 
     * @param id      Application ID
     * @param request Notes update request
     * @return Success message
     * @endpoint PUT /api/hr/applications/{id}/notes
     */
    @PutMapping("/applications/{id}/notes")
    public ResponseEntity<Map<String, Object>> updateApplicationNotes(
            @PathVariable Long id,
            @RequestBody UpdateApplicationNotesRequest request) {
        
        log.info("HR updating notes for application {}", id);
        
        try {
            Long hrUserId = getCurrentUserId();
            HRApplicationDetailResponse application = hrApplicationService.updateApplicationNotes(
                    id, request.getHrNotes(), hrUserId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notes updated successfully",
                    "data", application
            ));
        } catch (Exception e) {
            log.error("Error updating notes for application ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error updating notes: " + e.getMessage()
                    ));
        }
    }

    /**
     * Bulk update status for multiple applications
     * 
     * @param request Bulk update request with IDs and new status
     * @return Count of updated applications
     * @endpoint PUT /api/hr/applications/bulk-update
     */
    @PutMapping("/applications/bulk-update")
    public ResponseEntity<Map<String, Object>> bulkUpdateApplications(
            @Valid @RequestBody BulkUpdateApplicationRequest request) {
        
        log.info("HR bulk updating {} applications to status '{}'", 
                request.getApplicationIds().size(), request.getStatus());
        
        try {
            Long hrUserId = getCurrentUserId();
            int updatedCount = hrApplicationService.bulkUpdateStatus(
                    request.getApplicationIds(), request.getStatus(), hrUserId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", updatedCount + " application(s) updated successfully",
                    "updated", updatedCount
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid bulk update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Error bulk updating applications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error bulk updating applications: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get shortlisted applications
     * 
     * @param type Optional filter: 'Applied' or 'Imported'
     * @return List of shortlisted applications
     * @endpoint GET /api/hr/applications/shortlisted
     */
    @GetMapping("/applications/shortlisted")
    public ResponseEntity<Map<String, Object>> getShortlistedApplications(
            @RequestParam(required = false) String type) {
        
        log.info("HR fetching shortlisted applications with type: '{}'", type);
        
        try {
            List<HRApplicationSummaryResponse> applications = hrApplicationService.getShortlistedApplications(type);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "total", applications.size(),
                    "data", applications
            ));
        } catch (Exception e) {
            log.error("Error fetching shortlisted applications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error fetching shortlisted applications: " + e.getMessage()
                    ));
        }
    }

    // ==================== Dashboard Statistics ====================

    /**
     * Get HR dashboard statistics
     * 
     * @return Dashboard statistics
     * @endpoint GET /api/hr/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("HR fetching dashboard statistics");
        
        try {
            HRDashboardStatsResponse stats = hrApplicationService.getDashboardStatistics();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", stats
            ));
        } catch (Exception e) {
            log.error("Error fetching dashboard statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error fetching dashboard statistics: " + e.getMessage()
                    ));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Get current authenticated user ID
     * Returns 1L as default if user ID cannot be determined (for backward compatibility)
     */
    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                Object principal = auth.getPrincipal();
                
                // Check if principal has getId method (for Admin, HRFacultyUser, Student)
                if (principal instanceof com.webapp.Tracker_pro.model.Admin) {
                    return ((com.webapp.Tracker_pro.model.Admin) principal).getId();
                } else if (principal instanceof com.webapp.Tracker_pro.model.HRFacultyUser) {
                    return ((com.webapp.Tracker_pro.model.HRFacultyUser) principal).getId();
                }
            }
        } catch (Exception e) {
            log.warn("Could not get current user ID: {}", e.getMessage());
        }
        return 1L;  // Default fallback
    }
}
