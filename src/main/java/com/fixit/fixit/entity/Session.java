package com.fixit.fixit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fixit.fixit.enums.SessionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "passwordHash", "sessions", "helper"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "helper_id", nullable = false)
    @JsonIgnoreProperties({"user", "categories", "sessions"})
    private Helper helper;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"helpers"})
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "helper_rate", precision = 10, scale = 2)
    private BigDecimal helperRate;

    @Column(name = "user_consent_public")
    private Boolean userConsentPublic = false;

    @Column(name = "helper_consent_public")
    private Boolean helperConsentPublic = false;

    @Column(name = "deletion_requested")
    private Boolean deletionRequested = false;

    @Column(name = "retention_months")
    private Integer retentionMonths = 12;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================
    // Getters and Setters
    // =====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Helper getHelper() { return helper; }
    public void setHelper(Helper helper) { this.helper = helper; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public BigDecimal getHelperRate() { return helperRate; }
    public void setHelperRate(BigDecimal helperRate) { this.helperRate = helperRate; }

    public Boolean getUserConsentPublic() { return userConsentPublic; }
    public void setUserConsentPublic(Boolean userConsentPublic) {
        this.userConsentPublic = userConsentPublic;
    }

    public Boolean getHelperConsentPublic() { return helperConsentPublic; }
    public void setHelperConsentPublic(Boolean helperConsentPublic) {
        this.helperConsentPublic = helperConsentPublic;
    }

    public Boolean getDeletionRequested() { return deletionRequested; }
    public void setDeletionRequested(Boolean deletionRequested) {
        this.deletionRequested = deletionRequested;
    }

    public Integer getRetentionMonths() { return retentionMonths; }
    public void setRetentionMonths(Integer retentionMonths) {
        this.retentionMonths = retentionMonths;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}