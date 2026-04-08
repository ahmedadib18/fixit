package com.fixit.fixit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SendMessageRequest {

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotBlank(message = "Message cannot be empty")
    private String messageText;

    @NotNull(message = "User ID is required")
    private Long userId;

    // =====================
    // Getters and Setters
    // =====================

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}