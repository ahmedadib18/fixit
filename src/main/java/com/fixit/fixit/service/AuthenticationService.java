package com.fixit.fixit.service;

import com.fixit.fixit.entity.City;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.InvalidCredentialsException;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.exception.UnauthorizedException;
import com.fixit.fixit.repository.CityRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =============================================
    // REGISTER
    // =============================================
    public User register(String email,
                         String password,
                         String firstName,
                         String lastName,
                         UserType userType,
                         Long cityId) {

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        // Find city
        City city = null;
        if (cityId != null) {
            city = cityRepository.findById(cityId)
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", cityId));
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserType(userType);
        user.setCity(city);
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    // =============================================
    // LOGIN
    // =============================================
    public User login(String email, String password) {

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check account status
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new UnauthorizedException("Account is banned");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    // =============================================
    // GOOGLE OAUTH LOGIN
    // =============================================
    public User loginWithGoogle(String googleId,
                                String email,
                                String firstName,
                                String lastName,
                                UserType userType) {

        // Check if user already exists with this Google ID
        return userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {

                    // Check if email already exists
                    if (userRepository.existsByEmail(email)) {
                        // Link Google ID to existing account
                        User existingUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
                        existingUser.setGoogleId(googleId);
                        existingUser.setOauthProvider("GOOGLE");
                        existingUser.setLastLogin(LocalDateTime.now());
                        return userRepository.save(existingUser);
                    }

                    // Create new user from Google
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setGoogleId(googleId);
                    newUser.setOauthProvider("GOOGLE");
                    newUser.setUserType(userType);
                    newUser.setAccountStatus(AccountStatus.ACTIVE);
                    newUser.setLastLogin(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }
}