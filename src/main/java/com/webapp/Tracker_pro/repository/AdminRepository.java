package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Admin entity
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * Find admin by email
     * @param email Admin email
     * @return Optional Admin
     */
    Optional<Admin> findByEmail(String email);
    
    /**
     * Check if admin exists by email
     * @param email Admin email
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if admin exists by mobile number
     * @param mobileNo Mobile number
     * @return true if exists
     */
    boolean existsByMobileNo(String mobileNo);
}
