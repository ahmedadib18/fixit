package com.fixit.fixit.dto;

import com.fixit.fixit.entity.Session;
import com.fixit.fixit.enums.SessionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SessionResponse {
    private String id;
    private Long userId;
    private String userName;
    private Long helperId;
    private String helperName;
    private Long categoryId;
    private String categoryName;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private BigDecimal helperRate;
    private Boolean userConsentPublic;
    private Boolean helperConsentPublic;
    private LocalDateTime createdAt;

    public SessionResponse(Session session) {
        this.id = session.getId();
        
        if (session.getUser() != null) {
            this.userId = session.getUser().getId();
            this.userName = session.getUser().getFirstName() + " " + session.getUser().getLastName();
        }
        
        if (session.getHelper() != null && session.getHelper().getUser() != null) {
            this.helperId = session.getHelper().getId();
            this.helperName = session.getHelper().getUser().getFirstName() + " " + 
                            session.getHelper().getUser().getLastName();
        }
        
        if (session.getCategory() != null) {
            this.categoryId = session.getCategory().getId();
            this.categoryName = session.getCategory().getName();
        }
        
        this.status = session.getStatus();
        this.startedAt = session.getStartedAt();
        this.endedAt = session.getEndedAt();
        this.helperRate = session.getHelperRate();
        this.userConsentPublic = session.getUserConsentPublic();
        this.helperConsentPublic = session.getHelperConsentPublic();
        this.createdAt = session.getCreatedAt();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getHelperId() { return helperId; }
    public void setHelperId(Long helperId) { this.helperId = helperId; }

    public String getHelperName() { return helperName; }
    public void setHelperName(String helperName) { this.helperName = helperName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
