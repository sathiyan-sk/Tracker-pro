package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.HRFacultyUser;
import com.webapp.Tracker_pro.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for HRFacultyUser entity operations
 */
@Repository
public interface HRFacultyUserRepository extends JpaRepository<HRFacultyUser, Long> {

    /**
     * Find user by email
     */
    Optional<HRFacultyUser> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if mobile number exists
     */
    boolean existsByMobileNo(String mobileNo);

    /**
     * Find users by type (HR or FACULTY)
     */
    List<HRFacultyUser> findByUserType(UserType userType);

    /**
     * Count users by type
     */
    long countByUserType(UserType userType);

    /**
     * Search HR/Faculty users by name or email
     */
    @Query("SELECT u FROM HRFacultyUser u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<HRFacultyUser> searchUsers(@Param("search") String search);

    /**
     * Search by user type and name/email
     */
    @Query("SELECT u FROM HRFacultyUser u WHERE u.userType = :userType AND (" +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<HRFacultyUser> searchByUserType(@Param("search") String search, @Param("userType") UserType userType);

    /**
     * Find all active users
     */
    List<HRFacultyUser> findByIsActive(Boolean isActive);
}
