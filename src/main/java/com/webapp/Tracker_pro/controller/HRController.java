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
    private final com.webapp.Tracker_pro.service.EmailService emailService;

    // ==================== Applications Management ====================

    /**
     * Test email functionality
     * Sends a test email to verify email configuration
     * 
     * @param toEmail Email address to send test email to
     * @return Success/failure message
     * @endpoint POST /api/hr/test-email
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(
            @RequestParam String toEmail) {
        
        log.info("HR testing email service - sending test email to: {}", toEmail);
        
        try {
            String subject = "TrackerPro Email Service Test";
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                        .success-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: bold; color: white; background: #10b981; }
                        .footer { background: #f3f4f6; padding: 15px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ TrackerPro Email Test</h1>
                        </div>
                        <div class="content">
                            <h2>Email Service is Working!</h2>
                            <p>This is a test email from your TrackerPro HR system.</p>
                            <p><span class="success-badge">Configuration Successful</span></p>
                            <p>If you're reading this email, your email service is correctly configured and working properly.</p>
                            <p>You can now:</p>
                            <ul>
                                <li>Send application status updates to candidates</li>
                                <li>Send interview invitations</li>
                                <li>Communicate with applicants via email</li>
                            </ul>
                            <p>Best regards,<br>TrackerPro System</p>
                        </div>
                        <div class="footer">
                            <p>This is a test message from TrackerPro Email Service</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
            
            emailService.sendHtmlEmail(toEmail, subject, htmlContent);
            
            log.info("Test email sent successfully to: {}", toEmail);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test email sent successfully to " + toEmail,
                    "note", "Please check the inbox (and spam folder) for the test email"
            ));
        } catch (Exception e) {
            log.error("Error sending test email to {}: {}", toEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to send test email: " + e.getMessage(),
                            "error", e.getClass().getSimpleName(),
                            "troubleshooting", Map.of(
                                    "step1", "Verify email credentials in application.yaml",
                                    "step2", "Ensure App Password is generated for Gmail (not regular password)",
                                    "step3", "Check if 2-Factor Authentication is enabled on Gmail",
                                    "step4", "Verify SMTP settings: smtp.gmail.com:587",
                                    "step5", "Check application logs for detailed error messages"
                            )
                    ));
        }
    }

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
