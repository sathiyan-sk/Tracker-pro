package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notification entity operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user
     */
    List<Notification> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, String userType);

    /**
     * Find unread notifications for a user
     */
    List<Notification> findByUserIdAndUserTypeAndIsReadOrderByCreatedAtDesc(Long userId, String userType, Boolean isRead);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndUserTypeAndIsRead(Long userId, String userType, Boolean isRead);

    /**
     * Delete all notifications for a user
     */
    void deleteByUserIdAndUserType(Long userId, String userType);
}
