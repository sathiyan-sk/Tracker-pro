package com.webapp.Tracker_pro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CareerPost Entity representing career outcomes/internship posts in the system.
 * Enhanced with indexes for better query performance
 */
@Entity
@Table(name = "career_posts",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
    },
    indexes = {
        @Index(name = "idx_career_post_code", columnList = "code"),
        @Index(name = "idx_career_post_status", columnList = "status"),
        @Index(name = "idx_career_post_work_mode", columnList = "work_mode"),
        @Index(name = "idx_career_post_created_at", columnList = "created_at"),
        @Index(name = "idx_career_post_created_by", columnList = "created_by")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Integer duration; // Duration in months

    @Column(name = "work_mode", nullable = false, length = 20)
    private String workMode; // Online, Offline, Hybrid

    @Column(length = 500)
    private String prerequisites;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 20)
    private String status; // Draft, Posted

    @Column(name = "applications_count")
    private Integer applicationsCount = 0;

    @Column(name = "created_by")
    private Long createdBy; // Admin ID who created this post

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (applicationsCount == null) {
            applicationsCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
