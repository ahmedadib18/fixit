package com.fixit.fixit.service;

import com.fixit.fixit.entity.City;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.repository.CityRepository;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private SessionRepository sessionRepository;

    // =============================================
    // FIND USER BY ID
    // =============================================
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    // =============================================
    // FIND USER BY EMAIL
    // =============================================
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // =============================================
    // UPDATE PROFILE
    // =============================================
    public User updateProfile(Long userId,
                              String firstName,
                              String lastName,
                              String email,
                              String phone,
                              Long cityId) {

        // Find existing user
        User user = findById(userId);

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        // Find city if provided
        if (cityId != null) {
            City city = cityRepository.findById(cityId)
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", cityId));
            user.setCity(city);
        }

        // Update fields
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);

        return userRepository.save(user);
    }

    // =============================================
    // GET RECENT SESSIONS
    // =============================================
    public List<Session> getRecentSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // =============================================
    // UPDATE PROFILE IMAGE
    // =============================================
    public User updateProfileImage(Long userId, String imageUrl) {
        User user = findById(userId);
        user.setProfileImageUrl(imageUrl);
        return userRepository.save(user);
    }

    // =============================================
    // GET ALL USERS (Admin)
    // =============================================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}