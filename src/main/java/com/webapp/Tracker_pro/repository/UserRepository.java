package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.User;
import com.webapp.Tracker_pro.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Count users by user type
     * @param userType User type to count
     * @return Count of users
     */
    long countByUserType(UserType userType);

    /**
     * Find all users by user type
     * @param userType User type to filter
     * @return List of users
     */
    List<User> findByUserType(UserType userType);

    /**
     * Find users by user type in a list
     * @param userTypes List of user types
     * @return List of users
     */
    List<User> findByUserTypeIn(List<UserType> userTypes);

    /**
     * Count users created after a specific date
     * @param date Date to compare
     * @return Count of users
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Search users by name or email with user type filter
     * @param searchTerm Search term
     * @param userType User type
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchByUserType(@Param("searchTerm") String searchTerm, @Param("userType") UserType userType);

    /**
     * Search users by name or email across all user types
     * @param searchTerm Search term
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchAllUsers(@Param("searchTerm") String searchTerm);
}
