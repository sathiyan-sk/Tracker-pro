package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.SendBulkEmailRequest;
import com.webapp.Tracker_pro.dto.SendInterviewInviteRequest;
import com.webapp.Tracker_pro.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Email Operations
 * Handles email sending functionality for HR module
 * 
 * All endpoints require HR or ADMIN role
 * 
 * Endpoints:
 * - POST /api/hr/emails/send-bulk              - Send bulk emails to multiple candidates
 * - POST /api/hr/emails/send-interview-invite  - Send interview invitation to a candidate
 * 
 * @author TrackerPro Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/hr/emails")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@PreAuthorize("hasAnyRole('HR', 'ADMIN')")
public class EmailController {

    private final EmailService emailService;

    /**
     * Send bulk emails to multiple candidates
     * Used for rejection, acceptance, or other bulk notifications
     * 
     * @param request Bulk email request with recipients, subject, and body
     * @return Response with success status and count of emails sent
     * @endpoint POST /api/hr/emails/send-bulk
     */
    @PostMapping("/send-bulk")
    public ResponseEntity<Map<String, Object>> sendBulkEmails(
            @Valid @RequestBody SendBulkEmailRequest request) {
        
        log.info("HR sending bulk emails to {} recipients", request.getRecipients().size());
        
        try {
            // Extract email addresses from recipients
            List<String> emails = request.getRecipients().stream()
                    .map(SendBulkEmailRequest.RecipientInfo::getEmail)
                    .collect(Collectors.toList());
            
            // Send emails
            int successCount = 0;
            int failedCount = 0;
            
            for (SendBulkEmailRequest.RecipientInfo recipient : request.getRecipients()) {
                try {
                    // Personalize email body with candidate name if placeholder exists
                    String personalizedBody = request.getBody();
                    if (recipient.getName() != null && !recipient.getName().isEmpty()) {
                        personalizedBody = personalizedBody.replace("{candidateName}", recipient.getName());
                        personalizedBody = personalizedBody.replace("Dear Candidate", "Dear " + recipient.getName());
                    }
                    
                    emailService.sendSimpleEmail(
                        recipient.getEmail(),
                        request.getSubject(),
                        personalizedBody
                    );
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send email to {}: {}", recipient.getEmail(), e.getMessage());
                    failedCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Emails sent successfully to %d/%d recipients", 
                    successCount, request.getRecipients().size()));
            response.put("totalRecipients", request.getRecipients().size());
            response.put("successCount", successCount);
            response.put("failedCount", failedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error sending bulk emails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error sending emails: " + e.getMessage()
                    ));
        }
    }

    /**
     * Send interview invitation to a candidate
     * 
     * @param request Interview invitation request with candidate and interview details
     * @return Response with success status
     * @endpoint POST /api/hr/emails/send-interview-invite
     */
    @PostMapping("/send-interview-invite")
    public ResponseEntity<Map<String, Object>> sendInterviewInvite(
            @Valid @RequestBody SendInterviewInviteRequest request) {
        
        log.info("HR sending interview invitation to: {}", request.getCandidateEmail());
        
        try {
            // Validate meeting link for online interviews
            if ("Online".equalsIgnoreCase(request.getInterviewMode())) {
                if (request.getMeetingLink() == null || request.getMeetingLink().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "success", false,
                                    "message", "Meeting link is required for online interviews"
                            ));
                }
            }
            
            // Send interview invitation email
            emailService.sendInterviewInvitation(
                request.getCandidateEmail(),
                request.getCandidateName(),
                request.getInterviewDate(),
                request.getInterviewTime(),
                request.getInterviewMode(),
                request.getMeetingLink(),
                request.getHrManagerName()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Interview invitation sent successfully to " + request.getCandidateName()
            ));
            
        } catch (Exception e) {
            log.error("Error sending interview invitation to {}", request.getCandidateEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error sending interview invitation: " + e.getMessage()
                    ));
        }
    }

    /**
     * Test endpoint to verify email configuration
     * Can be removed in production
     * 
     * @param email Test recipient email
     * @return Response with success status
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String email) {
        log.info("Testing email configuration by sending to: {}", email);
        
        try {
            emailService.sendSimpleEmail(
                email,
                "TrackerPro Email Test",
                "This is a test email from TrackerPro HR System. If you receive this, email configuration is working correctly!"
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test email sent successfully to " + email
            ));
            
        } catch (Exception e) {
            log.error("Error sending test email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error sending test email: " + e.getMessage()
                    ));
        }
    }
}
