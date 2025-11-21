package com.webapp.Tracker_pro.config;

import com.webapp.Tracker_pro.model.User;
import com.webapp.Tracker_pro.model.UserType;
import com.webapp.Tracker_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initializer to seed default admin user on application startup.
 * This component runs automatically when the application starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (!userRepository.existsByEmail("admin@trackerpro.com")) {
            // Create default admin user
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setUserType(UserType.ADMIN);
            admin.setEmail("admin@trackerpro.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setMobileNo("9999999999");
            admin.setGender("Other");
            admin.setDob("01/01/1990");
            admin.setAge(35);
            admin.setLocation("System");
            admin.setIsActive(true);

            userRepository.save(admin);
            
            log.info("✅ Default Admin User Created Successfully!");
            log.info("   Email: admin@trackerpro.com");
            log.info("   Password: admin123");
            log.info("   Role: ADMIN");
        } else {
            log.info("ℹ️  Admin user already exists");
        }
    }
}
