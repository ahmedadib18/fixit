package com.fixit.fixit.dto;

import java.math.BigDecimal;
import java.util.List;

public class UpdateHelperProfileRequest {

    private String professionalHeadline;
    private String languagesSpoken;
    private List<Long> categoryIds;
    private List<BigDecimal> hourlyRates;
    private List<Integer> yearsExperiences;

    // =====================
    // Getters and Setters
    // =====================

    public String getProfessionalHeadline() { return professionalHeadline; }
    public void setProfessionalHeadline(String professionalHeadline) {
        this.professionalHeadline = professionalHeadline;
    }

    public String getLanguagesSpoken() { return languagesSpoken; }
    public void setLanguagesSpoken(String languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<BigDecimal> getHourlyRates() { return hourlyRates; }
    public void setHourlyRates(List<BigDecimal> hourlyRates) {
        this.hourlyRates = hourlyRates;
    }

    public List<Integer> getYearsExperiences() { return yearsExperiences; }
    public void setYearsExperiences(List<Integer> yearsExperiences) {
        this.yearsExperiences = yearsExperiences;
    }
}