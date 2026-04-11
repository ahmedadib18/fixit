package com.fixit.fixit.config;

import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            logger.info("========================================");
            logger.info("Starting Data Initialization...");
            logger.info("========================================");
            
            try {
                // Check if admin exists
                if (userRepository.existsByEmail("admin@fixit.com")) {
                    logger.info("ℹ️ INFO: Admin user already exists - DELETING to recreate with correct password");
                    userRepository.findByEmail("admin@fixit.com").ifPresent(user -> {
                        userRepository.delete(user);
                        logger.info("   Old admin user deleted");
                    });
                }
                
                // Create admin user with fresh password
                User admin = new User();
                admin.setEmail("admin@fixit.com");
                String encodedPassword = passwordEncoder.encode("admin123");
                admin.setPasswordHash(encodedPassword);
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setUserType(UserType.ADMIN);
                admin.setAccountStatus(AccountStatus.ACTIVE);
                
                userRepository.save(admin);
                logger.info("✅ SUCCESS: Admin user created/recreated!");
                logger.info("   Email: admin@fixit.com");
                logger.info("   Password: admin123");
                logger.info("   Password Hash: {}", encodedPassword.substring(0, 20) + "...");
                
                // Verify the password works
                boolean passwordMatches = passwordEncoder.matches("admin123", encodedPassword);
                logger.info("   Password verification: {}", passwordMatches ? "✅ PASS" : "❌ FAIL");
                
            } catch (Exception e) {
                logger.error("❌ ERROR: Failed to create admin user", e);
            }
            
            logger.info("========================================");
            logger.info("Data Initialization Complete");
            logger.info("========================================");
        };
    }
}
