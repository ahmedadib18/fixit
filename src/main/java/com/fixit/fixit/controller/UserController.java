package com.fixit.fixit.controller;

import com.fixit.fixit.dto.AddPaymentMethodRequest;
import com.fixit.fixit.dto.UpdateProfileRequest;
import com.fixit.fixit.dto.UserProfileResponse;
import com.fixit.fixit.entity.PaymentMethod;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.service.FileStorageService;
import com.fixit.fixit.service.PaymentService;
import com.fixit.fixit.service.UserService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            UserProfileResponse response = new UserProfileResponse(user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = userService.updateProfile(
                    userId,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getCityId()
            );
            UserProfileResponse response = new UserProfileResponse(updatedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{userId}/sessions")
    public ResponseEntity<?> getUserSessions(@PathVariable Long userId) {
        try {
            List<Session> sessions = userService.getRecentSessions(userId);
            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/{userId}/payment-methods")
    public ResponseEntity<?> addPaymentMethod(
            @PathVariable Long userId,
            @Valid @RequestBody AddPaymentMethodRequest request) {
        try {
            PaymentMethod paymentMethod = paymentService.addPaymentMethod(
                    userId,
                    request.getStripePaymentMethodId(),
                    request.getCardLastFour(),
                    request.getCardBrand()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethod);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping("/{userId}/payment-methods/{paymentMethodId}")
    public ResponseEntity<?> deletePaymentMethod(
            @PathVariable Long userId,
            @PathVariable Long paymentMethodId) {
        try {
            paymentService.deletePaymentMethod(paymentMethodId);
            return ResponseEntity.ok(new SuccessResponse("Payment method deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{userId}/payment-methods")
    public ResponseEntity<?> getPaymentMethods(@PathVariable Long userId) {
        try {
            List<PaymentMethod> paymentMethods = paymentService.getPaymentMethods(userId);
            return ResponseEntity.ok(paymentMethods);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.storeProfileImage(file, userId);
            User user = userService.updateProfileImage(userId, imageUrl);
            UserProfileResponse response = new UserProfileResponse(user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for responses
    private static class ErrorResponse {
        private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
    }

    private static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
