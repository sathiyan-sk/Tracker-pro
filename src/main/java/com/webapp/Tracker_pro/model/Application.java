package com.webapp.Tracker_pro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application Entity representing student internship applications
 */
@Entity
@Table(name = "applications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_application_student_career", 
            columnNames = {"student_id", "career_post_id"}
        )
    },
    indexes = {
        @Index(name = "idx_application_student_id", columnList = "student_id"),
        @Index(name = "idx_application_career_post_id", columnList = "career_post_id"),
        @Index(name = "idx_application_status", columnList = "status"),
        @Index(name = "idx_application_applied_date", columnList = "applied_date"),
        @Index(name = "idx_application_student_career", columnList = "student_id, career_post_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "career_post_id", nullable = false)
    private Long careerPostId;
    
    @Column(nullable = false, length = 20)
    private String status = "Pending";  // Pending, Under Review, Shortlisted, Accepted, Rejected
    
    @Column(name = "applied_date", nullable = false)
    private LocalDateTime appliedDate;
    
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;
    
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;
    
    @Column(name = "resume_filename", length = 255)
    private String resumeFilename;
    
    @Column(name = "additional_skills", length = 500)
    private String additionalSkills;
    
    @Column(length = 100)
    private String availability;
    
    @Column(name = "expected_stipend", length = 50)
    private String expectedStipend;
    
    @Column(name = "hr_notes", columnDefinition = "TEXT")
    private String hrNotes;
    
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;  // HR/Faculty user ID
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        appliedDate = LocalDateTime.now();
        if (status == null) {
            status = "Pending";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
