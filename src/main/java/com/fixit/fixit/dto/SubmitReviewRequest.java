package com.fixit.fixit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubmitReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String reviewText;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Helper ID is required")
    private Long helperId;

    // =====================
    // Getters and Setters
    // =====================

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getHelperId() { return helperId; }
    public void setHelperId(Long helperId) { this.helperId = helperId; }
}