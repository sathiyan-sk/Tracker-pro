package com.webapp.Tracker_pro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Email Service for sending emails to candidates
 * Supports both simple text emails and HTML emails
 * Uses Spring Boot Mail with Gmail SMTP configuration
 * 
 * @author TrackerPro Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * Send simple text email to a single recipient
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (plain text)
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    /**
     * Send HTML email to a single recipient
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody Email body (HTML format)
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML
            
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    /**
     * Send bulk emails to multiple recipients (async)
     * Each recipient gets an individual email
     * 
     * @param recipients List of recipient email addresses
     * @param subject Email subject
     * @param body Email body (plain text)
     * @return Number of successfully sent emails
     */
    @Async
    public int sendBulkEmails(List<String> recipients, String subject, String body) {
        int successCount = 0;
        
        for (String recipient : recipients) {
            try {
                sendSimpleEmail(recipient, subject, body);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", recipient, e.getMessage());
                // Continue with other recipients even if one fails
            }
        }
        
        log.info("Bulk email operation completed: {}/{} emails sent successfully", 
                successCount, recipients.size());
        return successCount;
    }

    /**
     * Send bulk HTML emails to multiple recipients (async)
     * 
     * @param recipients List of recipient email addresses
     * @param subject Email subject
     * @param htmlBody Email body (HTML format)
     * @return Number of successfully sent emails
     */
    @Async
    public int sendBulkHtmlEmails(List<String> recipients, String subject, String htmlBody) {
        int successCount = 0;
        
        for (String recipient : recipients) {
            try {
                sendHtmlEmail(recipient, subject, htmlBody);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send HTML email to {}: {}", recipient, e.getMessage());
                // Continue with other recipients
            }
        }
        
        log.info("Bulk HTML email operation completed: {}/{} emails sent successfully", 
                successCount, recipients.size());
        return successCount;
    }

    /**
     * Send interview invitation email
     * 
     * @param candidateEmail Candidate's email
     * @param candidateName Candidate's name
     * @param interviewDate Interview date
     * @param interviewTime Interview time
     * @param interviewMode Interview mode (Online/Offline)
     * @param meetingLink Meeting link (for online interviews)
     * @param hrManagerName HR Manager name
     */
    public void sendInterviewInvitation(
            String candidateEmail,
            String candidateName,
            String interviewDate,
            String interviewTime,
            String interviewMode,
            String meetingLink,
            String hrManagerName) {
        
        String subject = "Interview Invitation - TrackerPro";
        
        String htmlBody = buildInterviewInvitationHtml(
            candidateName, interviewDate, interviewTime, 
            interviewMode, meetingLink, hrManagerName
        );
        
        sendHtmlEmail(candidateEmail, subject, htmlBody);
        log.info("Interview invitation sent to: {}", candidateEmail);
    }

    /**
     * Build HTML template for interview invitation
     */
    private String buildInterviewInvitationHtml(
            String candidateName,
            String interviewDate,
            String interviewTime,
            String interviewMode,
            String meetingLink,
            String hrManagerName) {
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html><head><style>")
            .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
            .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
            .append(".header { background: #008BDC; color: white; padding: 20px; text-align: center; }")
            .append(".content { padding: 20px; background: #f9f9f9; }")
            .append(".details { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #008BDC; }")
            .append(".detail-item { margin: 10px 0; }")
            .append(".label { font-weight: bold; color: #008BDC; }")
            .append(".button { display: inline-block; padding: 12px 30px; background: #008BDC; color: white; text-decoration: none; border-radius: 5px; margin: 15px 0; }")
            .append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }")
            .append("</style></head><body>")
            .append("<div class='container'>")
            .append("<div class='header'><h1>Interview Invitation</h1></div>")
            .append("<div class='content'>")
            .append("<p>Dear ").append(candidateName).append(",</p>")
            .append("<p>Congratulations! We are pleased to invite you for an interview with our team.</p>")
            .append("<div class='details'>")
            .append("<div class='detail-item'><span class='label'>Date:</span> ").append(interviewDate).append("</div>")
            .append("<div class='detail-item'><span class='label'>Time:</span> ").append(interviewTime).append("</div>")
            .append("<div class='detail-item'><span class='label'>Mode:</span> ").append(interviewMode).append("</div>");
        
        if (meetingLink != null && !meetingLink.isEmpty() && interviewMode.equalsIgnoreCase("Online")) {
            html.append("<div class='detail-item'><span class='label'>Meeting Link:</span> ")
                .append("<a href='").append(meetingLink).append("'>").append(meetingLink).append("</a></div>");
        }
        
        html.append("<div class='detail-item'><span class='label'>HR Manager:</span> ").append(hrManagerName).append("</div>")
            .append("</div>")
            .append("<p>Please confirm your availability for this interview. If you have any questions or need to reschedule, please contact us.</p>")
            .append("<p>We look forward to speaking with you!</p>")
            .append("<p>Best regards,<br/>").append(fromName).append("</p>")
            .append("</div>")
            .append("<div class='footer'>")
            .append("<p>This is an automated email. Please do not reply to this email.</p>")
            .append("</div>")
            .append("</div></body></html>");
        
        return html.toString();
    }

    /**
     * Send application status email (rejection/acceptance)
     * 
     * @param candidateEmail Candidate's email
     * @param candidateName Candidate's name
     * @param subject Email subject
     * @param body Email body
     */
    public void sendApplicationStatusEmail(
            String candidateEmail,
            String candidateName,
            String subject,
            String body) {
        
        // Replace placeholders in the body
        String personalizedBody = body.replace("{candidateName}", candidateName);
        
        sendSimpleEmail(candidateEmail, subject, personalizedBody);
        log.info("Application status email sent to: {}", candidateEmail);
    }
}
