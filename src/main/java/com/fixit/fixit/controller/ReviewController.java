package com.fixit.fixit.controller;

import com.fixit.fixit.dto.SubmitReviewRequest;
import com.fixit.fixit.entity.Review;
import com.fixit.fixit.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/sessions/{sessionId}")
    public ResponseEntity<?> submitReview(
            @PathVariable String sessionId,
            @Valid @RequestBody SubmitReviewRequest request) {
        try {
            Review review = reviewService.submitReview(
                    sessionId,
                    request.getUserId(),
                    request.getHelperId(),
                    request.getRating(),
                    request.getReviewText()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/helpers/{helperId}")
    public ResponseEntity<?> getHelperReviews(@PathVariable Long helperId) {
        try {
            List<Review> reviews = reviewService.getHelperReviews(helperId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/helpers/{helperId}/rating")
    public ResponseEntity<?> getHelperAverageRating(@PathVariable Long helperId) {
        try {
            Double avgRating = reviewService.getHelperAverageRating(helperId);
            RatingResponse response = new RatingResponse(avgRating);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for responses
    private static class RatingResponse {
        private Double averageRating;

        public RatingResponse(Double averageRating) {
            this.averageRating = averageRating;
        }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
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
