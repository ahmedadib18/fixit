package com.fixit.fixit.controller;

import com.fixit.fixit.dto.HelperDetailResponse;
import com.fixit.fixit.dto.HelperSearchResponse;
import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Review;
import com.fixit.fixit.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/helpers")
    public ResponseEntity<?> searchHelpers(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean availableNow,
            @RequestParam(required = false) Long cityId) {
        try {
            System.out.println("=== SEARCH HELPERS REQUEST ===");
            System.out.println("categoryId: " + categoryId);
            System.out.println("minRating: " + minRating);
            System.out.println("maxPrice: " + maxPrice);
            System.out.println("language: " + language);
            System.out.println("availableNow: " + availableNow);
            System.out.println("cityId: " + cityId);
            
            List<Helper> helpers = searchService.searchHelpers(
                    categoryId,
                    minRating,
                    maxPrice,
                    language,
                    availableNow,
                    cityId
            );
            
            System.out.println("Found " + helpers.size() + " helpers");
            helpers.forEach(h -> System.out.println("  Helper ID: " + h.getId() + ", Available: " + h.getIsAvailable()));
            
            // Convert to DTOs
            List<HelperSearchResponse> response = helpers.stream()
                    .map(HelperSearchResponse::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/helpers/{helperId}")
    public ResponseEntity<?> getHelperProfile(@PathVariable Long helperId) {
        try {
            Helper helper = searchService.getHelperProfile(helperId);
            Double avgRating = searchService.getHelperAverageRating(helperId);
            List<Review> reviews = searchService.getHelperReviews(helperId);

            HelperDetailResponse response = new HelperDetailResponse(helper, avgRating, reviews);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/debug/all-helpers")
    public ResponseEntity<?> getAllHelpersDebug() {
        try {
            List<Helper> allHelpers = searchService.searchHelpers(null, null, null, null, null, null);
            System.out.println("=== DEBUG: ALL HELPERS ===");
            allHelpers.forEach(h -> {
                System.out.println("Helper ID: " + h.getId() + 
                    ", User: " + (h.getUser() != null ? h.getUser().getEmail() : "null") +
                    ", Available: " + h.getIsAvailable() +
                    ", Categories: " + (h.getCategories() != null ? h.getCategories().size() : 0));
            });
            
            List<HelperSearchResponse> response = allHelpers.stream()
                    .map(HelperSearchResponse::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
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
}
