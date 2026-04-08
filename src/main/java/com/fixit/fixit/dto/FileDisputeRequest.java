package com.fixit.fixit.dto;

import com.fixit.fixit.enums.DisputeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FileDisputeRequest {

    @NotNull(message = "Dispute type is required")
    private DisputeType disputeType;

    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Respondent ID is required")
    private Long respondentId;

    // =====================
    // Getters and Setters
    // =====================

    public DisputeType getDisputeType() { return disputeType; }
    public void setDisputeType(DisputeType disputeType) {
        this.disputeType = disputeType;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRespondentId() { return respondentId; }
    public void setRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }
}