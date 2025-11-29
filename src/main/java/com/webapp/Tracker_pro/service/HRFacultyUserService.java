package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.CreateUserRequest;
import com.webapp.Tracker_pro.dto.HRFacultyUserResponse;
import com.webapp.Tracker_pro.exception.ResourceNotFoundException;
import com.webapp.Tracker_pro.exception.UserAlreadyExistsException;
import com.webapp.Tracker_pro.model.HRFacultyUser;
import com.webapp.Tracker_pro.model.UserType;
import com.webapp.Tracker_pro.repository.HRFacultyUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for HR and Faculty user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HRFacultyUserService {

    private final HRFacultyUserRepository hrFacultyUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all HR/Faculty users with optional filter
     */
    public List<HRFacultyUserResponse> getAllUsers(String filter) {
        log.info("Fetching HR/Faculty users with filter: '{}'", filter);

        List<HRFacultyUser> users;
        if (filter != null && filter.equalsIgnoreCase("hr")) {
            users = hrFacultyUserRepository.findByUserType(UserType.HR);
        } else if (filter != null && filter.equalsIgnoreCase("faculty")) {
            users = hrFacultyUserRepository.findByUserType(UserType.FACULTY);
        } else {
            users = hrFacultyUserRepository.findAll();
        }

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public HRFacultyUserResponse getUserById(Long id) {
        log.info("Fetching HR/Faculty user by ID: {}", id);
        HRFacultyUser user = hrFacultyUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    /**
     * Create new HR or Faculty user
     */
    @Transactional
    public HRFacultyUserResponse createUser(CreateUserRequest request) {
        log.info("Creating new HR/Faculty user: {}", request.getEmail());

        // Validate user type
        if (!request.getUserType().equalsIgnoreCase("HR") && 
            !request.getUserType().equalsIgnoreCase("FACULTY")) {
            throw new IllegalArgumentException("Only HR and FACULTY users can be created");
        }

        // Check if email exists
        if (hrFacultyUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Check if mobile exists
        if (hrFacultyUserRepository.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException("User with mobile number " + request.getMobileNo() + " already exists");
        }

        HRFacultyUser user = new HRFacultyUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNo(request.getMobileNo());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setAge(request.getAge());
        user.setLocation(request.getLocation());
        user.setUserType(UserType.valueOf(request.getUserType().toUpperCase()));
        user.setIsActive(true);

        HRFacultyUser savedUser = hrFacultyUserRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapToResponse(savedUser);
    }

    /**
     * Update existing HR/Faculty user
     */
    @Transactional
    public HRFacultyUserResponse updateUser(Long id, CreateUserRequest request) {
        log.info("Updating HR/Faculty user with ID: {}", id);

        HRFacultyUser user = hrFacultyUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            hrFacultyUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Check if mobile is being changed and already exists
        if (!user.getMobileNo().equals(request.getMobileNo()) && 
            hrFacultyUserRepository.existsByMobileNo(request.getMobileNo())) {
            throw new UserAlreadyExistsException("User with mobile number " + request.getMobileNo() + " already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        
        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        user.setMobileNo(request.getMobileNo());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setAge(request.getAge());
        user.setLocation(request.getLocation());
        
        if (request.getUserType() != null) {
            user.setUserType(UserType.valueOf(request.getUserType().toUpperCase()));
        }

        HRFacultyUser updatedUser = hrFacultyUserRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return mapToResponse(updatedUser);
    }

    /**
     * Delete HR/Faculty user
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting HR/Faculty user with ID: {}", id);
        HRFacultyUser user = hrFacultyUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        hrFacultyUserRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }

    /**
     * Toggle user status (active/inactive)
     */
    @Transactional
    public HRFacultyUserResponse toggleUserStatus(Long id, Boolean isEnabled) {
        log.info("Toggling status for user ID: {} to {}", id, isEnabled);
        HRFacultyUser user = hrFacultyUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setIsActive(isEnabled);
        HRFacultyUser updatedUser = hrFacultyUserRepository.save(user);
        return mapToResponse(updatedUser);
    }

    /**
     * Count HR users
     */
    public long countHRUsers() {
        return hrFacultyUserRepository.countByUserType(UserType.HR);
    }

    /**
     * Count Faculty users
     */
    public long countFacultyUsers() {
        return hrFacultyUserRepository.countByUserType(UserType.FACULTY);
    }

    /**
     * Map HRFacultyUser entity to HRFacultyUserResponse DTO
     */
    private HRFacultyUserResponse mapToResponse(HRFacultyUser user) {
        return HRFacultyUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .gender(user.getGender())
                .dob(user.getDob())
                .age(user.getAge())
                .location(user.getLocation())
                .role(user.getUserType().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .status(user.getIsActive() ? "Active" : "Inactive")
                .lastLogin(null) // Can be implemented later with login tracking
                .build();
    }
}
