package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.CareerPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CareerPost entity
 */
@Repository
public interface CareerPostRepository extends JpaRepository<CareerPost, Long> {
    
    /**
     * Find career post by code
     * @param code Post code
     * @return Optional CareerPost
     */
    Optional<CareerPost> findByCode(String code);
    
    /**
     * Check if post exists by code
     * @param code Post code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Find all posts by status
     * @param status Post status (Draft or Posted)
     * @return List of posts
     */
    List<CareerPost> findByStatus(String status);
    
    /**
     * Count posts by status
     * @param status Post status
     * @return count
     */
    long countByStatus(String status);
    
    /**
     * Search posts by title or code
     * @param searchTerm Search term
     * @return List of matching posts
     */
    @Query("SELECT cp FROM CareerPost cp WHERE " +
           "LOWER(cp.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cp.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CareerPost> searchPosts(@Param("searchTerm") String searchTerm);
}
