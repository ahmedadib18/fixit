package com.fixit.fixit.service;

import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.exception.InvalidOperationException;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // GET ALL USERS
    // =============================================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // =============================================
    // SUSPEND USER
    // =============================================
    public User suspendUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new InvalidOperationException("Cannot suspend a banned account");
        }

        user.setAccountStatus(AccountStatus.SUSPENDED);
        return userRepository.save(user);
    }

    // =============================================
    // BAN USER
    // =============================================
    public User banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        user.setAccountStatus(AccountStatus.BANNED);
        return userRepository.save(user);
    }

    // =============================================
    // REACTIVATE USER
    // =============================================
    public User reactivateUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account is already active");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    // =============================================
    // GET USERS BY STATUS
    // =============================================
    public List<User> getUsersByStatus(AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    // =============================================
    // GET USER BY ID
    // =============================================
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));
    }
}