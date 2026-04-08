package com.fixit.fixit.dto;

import jakarta.validation.constraints.NotNull;

public class CreateSessionRequest {

    @NotNull(message = "Helper ID is required")
    private Long helperId;

    private Long categoryId;

    @NotNull(message = "User ID is required")
    private Long userId;

    // =====================
    // Getters and Setters
    // =====================

    public Long getHelperId() { return helperId; }
    public void setHelperId(Long helperId) { this.helperId = helperId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}