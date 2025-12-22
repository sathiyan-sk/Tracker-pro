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
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email (for professional formatting)
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.info("HTML Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send application status update email
     */
    @Async
    public void sendApplicationStatusEmail(String toEmail, String studentName,
                                           String internshipTitle, String newStatus) {
        String subject = "Application Status Update - " + internshipTitle;

        String htmlContent = buildStatusEmailTemplate(studentName, internshipTitle, newStatus);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Send interview invitation email
     */
    @Async
    public void sendInterviewInvitationEmail(String toEmail, String studentName,
                                             String internshipTitle, String interviewDetails) {
        String subject = "Interview Invitation - " + internshipTitle;

        String htmlContent = buildInterviewEmailTemplate(studentName, internshipTitle, interviewDetails);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Build HTML template for status update emails
     */
    private String buildStatusEmailTemplate(String studentName, String internshipTitle, String status) {
        String statusColor = getStatusColor(status);
        String statusMessage = getStatusMessage(status);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                    .status-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: bold; color: white; background: %s; }
                    .footer { background: #f3f4f6; padding: 15px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>TrackerPro</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your application status for <strong>%s</strong> has been updated.</p>
                        <p>New Status: <span class="status-badge">%s</span></p>
                        <p>%s</p>
                        <p>Best regards,<br>TrackerPro HR Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from TrackerPro. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(statusColor, studentName, internshipTitle, status, statusMessage);
    }

    /**
     * Build HTML template for interview invitation
     */
    private String buildInterviewEmailTemplate(String studentName, String internshipTitle, String details) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                    .details-box { background: white; padding: 15px; border-left: 4px solid #059669; margin: 20px 0; }
                    .footer { background: #f3f4f6; padding: 15px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Interview Invitation</h1>
                    </div>
                    <div class="content">
                        <h2>Congratulations %s!</h2>
                        <p>We are pleased to invite you for an interview for the position of <strong>%s</strong>.</p>
                        <div class="details-box">
                            <h3>Interview Details:</h3>
                            <p>%s</p>
                        </div>
                        <p>Please confirm your availability by replying to this email.</p>
                        <p>Best regards,<br>TrackerPro HR Team</p>
                    </div>
                    <div class="footer">
                        <p>TrackerPro - Your Career Partner</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(studentName, internshipTitle, details);
    }

    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "shortlisted" -> "#f59e0b";
            case "accepted" -> "#10b981";
            case "rejected" -> "#ef4444";
            case "under review" -> "#3b82f6";
            case "interview scheduled" -> "#8b5cf6";
            default -> "#6b7280";
        };
    }

    private String getStatusMessage(String status) {
        return switch (status.toLowerCase()) {
            case "shortlisted" -> "Congratulations! You have been shortlisted. Our HR team will contact you soon with next steps.";
            case "accepted" -> "Congratulations! Your application has been accepted. Welcome aboard!";
            case "rejected" -> "We appreciate your interest. Unfortunately, we have decided to move forward with other candidates. We encourage you to apply for future opportunities.";
            case "under review" -> "Your application is currently being reviewed by our team.";
            case "interview scheduled" -> "An interview has been scheduled. Please check your email for details.";
            default -> "Please log in to TrackerPro for more details.";
        };
    }
}