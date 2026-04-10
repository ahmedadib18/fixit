package com.fixit.fixit.dto;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.HelperCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class HelperProfileResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String professionalHeadline;
    private String languagesSpoken;
    private Boolean isAvailable;
    private List<CategorySpecialization> specializations;

    public HelperProfileResponse(Helper helper) {
        this.id = helper.getId();
        if (helper.getUser() != null) {
            this.userId = helper.getUser().getId();
            this.firstName = helper.getUser().getFirstName();
            this.lastName = helper.getUser().getLastName();
            this.email = helper.getUser().getEmail();
        }
        this.professionalHeadline = helper.getProfessionalHeadline();
        this.languagesSpoken = helper.getLanguagesSpoken();
        this.isAvailable = helper.getIsAvailable();
        
        if (helper.getCategories() != null) {
            this.specializations = helper.getCategories().stream()
                .map(CategorySpecialization::new)
                .collect(Collectors.toList());
        }
    }

    // Inner class for category specializations
    public static class CategorySpecialization {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private BigDecimal hourlyRate;
        private BigDecimal fixedRate;
        private Integer yearsExperience;
        private String certificateUrl;

        public CategorySpecialization(HelperCategory hc) {
            this.id = hc.getId();
            if (hc.getCategory() != null) {
                this.categoryId = hc.getCategory().getId();
                this.categoryName = hc.getCategory().getName();
            }
            this.hourlyRate = hc.getHourlyRate();
            this.fixedRate = hc.getFixedRate();
            this.yearsExperience = hc.getYearsExperience();
            this.certificateUrl = hc.getCertificateUrl();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

        public BigDecimal getFixedRate() { return fixedRate; }
        public void setFixedRate(BigDecimal fixedRate) { this.fixedRate = fixedRate; }

        public Integer getYearsExperience() { return yearsExperience; }
        public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

        public String getCertificateUrl() { return certificateUrl; }
        public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfessionalHeadline() { return professionalHeadline; }
    public void setProfessionalHeadline(String professionalHeadline) { 
        this.professionalHeadline = professionalHeadline; 
    }

    public String getLanguagesSpoken() { return languagesSpoken; }
    public void setLanguagesSpoken(String languagesSpoken) { 
        this.languagesSpoken = languagesSpoken; 
    }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public List<CategorySpecialization> getSpecializations() { return specializations; }
    public void setSpecializations(List<CategorySpecialization> specializations) { 
        this.specializations = specializations; 
    }
}
