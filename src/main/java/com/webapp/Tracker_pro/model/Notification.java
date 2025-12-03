package com.webapp.Tracker_pro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification Entity for user notifications
 */
@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_user_type", columnList = "user_type"),
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;  // STUDENT, HR, FACULTY, ADMIN
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "notification_type", length = 50)
    private String notificationType;  // APPLICATION_STATUS, NEW_INTERNSHIP, INTERVIEW, SYSTEM
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;  // ID of related application/internship
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}
