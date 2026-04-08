package com.fixit.fixit.controller;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Review;
import com.fixit.fixit.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
            List<Helper> helpers = searchService.searchHelpers(
                    categoryId,
                    minRating,
                    maxPrice,
                    language,
                    availableNow,
                    cityId
            );
            return ResponseEntity.ok(helpers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/helpers/{helperId}")
    public ResponseEntity<?> getHelperProfile(@PathVariable Long helperId) {
        try {
            Helper helper = searchService.getHelperProfile(helperId);
            Double avgRating = searchService.getHelperAverageRating(helperId);
            List<Review> reviews = searchService.getHelperReviews(helperId);

            HelperProfileResponse response = new HelperProfileResponse(helper, avgRating, reviews);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for responses
    private static class HelperProfileResponse {
        private Helper helper;
        private Double averageRating;
        private List<Review> reviews;

        public HelperProfileResponse(Helper helper, Double averageRating, List<Review> reviews) {
            this.helper = helper;
            this.averageRating = averageRating;
            this.reviews = reviews;
        }

        public Helper getHelper() { return helper; }
        public void setHelper(Helper helper) { this.helper = helper; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public List<Review> getReviews() { return reviews; }
        public void setReviews(List<Review> reviews) { this.reviews = reviews; }
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
