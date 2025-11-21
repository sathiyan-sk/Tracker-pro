package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     * @param email User's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by mobile number
     * @param mobileNo User's mobile number
     * @return Optional containing user if found
     */
    Optional<User> findByMobileNo(String mobileNo);

    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if mobile number already exists
     * @param mobileNo Mobile number to check
     * @return true if exists, false otherwise
     */
    boolean existsByMobileNo(String mobileNo);
}
