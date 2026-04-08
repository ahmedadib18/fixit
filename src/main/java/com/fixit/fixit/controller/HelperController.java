package com.fixit.fixit.controller;

import com.fixit.fixit.dto.UpdateHelperProfileRequest;
import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.HelperCategory;
import com.fixit.fixit.service.FileStorageService;
import com.fixit.fixit.service.HelperService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpers")
public class HelperController {

    @Autowired
    private HelperService helperService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{helperId}/profile")
    public ResponseEntity<?> getHelperProfile(@PathVariable Long helperId) {
        try {
            Helper helper = helperService.getHelperWithUser(helperId);
            return ResponseEntity.ok(helper);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{helperId}/profile")
    public ResponseEntity<?> updateHelperProfile(
            @PathVariable Long helperId,
            @Valid @RequestBody UpdateHelperProfileRequest request) {
        try {
            Helper helper = helperService.saveHelperProfile(
                    helperId,
                    request.getProfessionalHeadline(),
                    request.getLanguagesSpoken()
            );

            if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                List<HelperCategory> categories = helperService.syncCategorySpecializations(
                        helperId,
                        request.getCategoryIds(),
                        request.getHourlyRates(),
                        request.getYearsExperiences()
                );
            }

            return ResponseEntity.ok(helper);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/{helperId}/categories/{categoryId}/certificate")
    public ResponseEntity<?> uploadCertificate(
            @PathVariable Long helperId,
            @PathVariable Long categoryId,
            @RequestParam("file") MultipartFile file) {
        try {
            String certificateUrl = fileStorageService.storeCertificate(file, helperId);
            HelperCategory helperCategory = helperService.uploadCertificate(categoryId, certificateUrl);
            return ResponseEntity.ok(helperCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{helperId}/availability")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long helperId,
            @RequestBody AvailabilityRequest request) {
        try {
            Helper helper = helperService.updateAvailability(helperId, request.getIsAvailable());
            return ResponseEntity.ok(helper);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for requests and responses
    private static class CertificateUploadRequest {
        private String certificateUrl;

        public String getCertificateUrl() { return certificateUrl; }
        public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    }

    private static class AvailabilityRequest {
        private Boolean isAvailable;

        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }

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
}
