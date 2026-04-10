package com.fixit.fixit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SendMessageRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Message cannot be empty")
    private String messageText;

    // =====================
    // Getters and Setters
    // =====================

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}