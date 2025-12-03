package com.webapp.Tracker_pro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StudentDocument Entity representing uploaded documents by students
 */
@Entity
@Table(name = "student_documents",
    indexes = {
        @Index(name = "idx_document_student_id", columnList = "student_id"),
        @Index(name = "idx_document_type", columnList = "document_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;  // Resume, Cover Letter, Certificate, ID Proof
    
    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;
    
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "uploaded_date", nullable = false)
    private LocalDateTime uploadedDate;
    
    @Column(name = "is_primary")
    private Boolean isPrimary = false;  // For primary resume
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        uploadedDate = LocalDateTime.now();
        if (isPrimary == null) {
            isPrimary = false;
        }
    }
}
