package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Application entity operations
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Find all applications by student ID
     */
    List<Application> findByStudentIdOrderByAppliedDateDesc(Long studentId);

    /**
     * Find applications by student ID and status
     */
    List<Application> findByStudentIdAndStatusOrderByAppliedDateDesc(Long studentId, String status);

    /**
     * Find all applications for a career post
     */
    List<Application> findByCareerPostIdOrderByAppliedDateDesc(Long careerPostId);

    /**
     * Check if student has already applied for a career post
     */
    boolean existsByStudentIdAndCareerPostId(Long studentId, Long careerPostId);

    /**
     * Find application by student ID and career post ID
     */
    Optional<Application> findByStudentIdAndCareerPostId(Long studentId, Long careerPostId);

    /**
     * Count applications by student ID
     */
    long countByStudentId(Long studentId);

    /**
     * Count applications by student ID and status
     */
    long countByStudentIdAndStatus(Long studentId, String status);

    /**
     * Count total applications for a career post
     */
    long countByCareerPostId(Long careerPostId);

    /**
     * Find all applications by status
     */
    List<Application> findByStatusOrderByAppliedDateDesc(String status);

    /**
     * Get applications with student and internship details
     */
    @Query("SELECT a FROM Application a WHERE a.studentId = :studentId ORDER BY a.appliedDate DESC")
    List<Application> findApplicationsByStudentId(@Param("studentId") Long studentId);

    /**
     * Count applications by status (useful for HR dashboard)
     */
    long countByStatus(String status);
}
