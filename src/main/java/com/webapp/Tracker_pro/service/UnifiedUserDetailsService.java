package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.model.Admin;
import com.webapp.Tracker_pro.model.HRFacultyUser;
import com.webapp.Tracker_pro.model.Student;
import com.webapp.Tracker_pro.repository.AdminRepository;
import com.webapp.Tracker_pro.repository.HRFacultyUserRepository;
import com.webapp.Tracker_pro.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Unified User Details Service
 * Loads user details from all three normalized tables: admins, students, hr_faculty_users
 * Used by Spring Security for authentication and authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final HRFacultyUserRepository hrFacultyUserRepository;

    /**
     * Load user by username (email) from any of the three tables
     * 
     * @param username The email address of the user
     * @return UserDetails object
     * @throws UsernameNotFoundException if user is not found in any table
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username.trim().toLowerCase();
        
        log.debug("Loading user by email: {}", email);

        // Try to find in Admin table
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isPresent()) {
            log.debug("User found in Admin table: {}", email);
            return adminOptional.get();
        }

        // Try to find in Student table
        Optional<Student> studentOptional = studentRepository.findByEmail(email);
        if (studentOptional.isPresent()) {
            log.debug("User found in Student table: {}", email);
            return studentOptional.get();
        }

        // Try to find in HRFacultyUser table
        Optional<HRFacultyUser> hrFacultyOptional = hrFacultyUserRepository.findByEmail(email);
        if (hrFacultyOptional.isPresent()) {
            log.debug("User found in HR/Faculty table: {}", email);
            return hrFacultyOptional.get();
        }

        // User not found in any table
        log.warn("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
