package com.fixit.fixit.dto;

import com.fixit.fixit.entity.Helper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class HelperSearchResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String professionalHeadline;
    private String languagesSpoken;
    private Boolean isAvailable;
    private String cityName;
    private String countryName;
    private List<SpecializationInfo> specializations;

    public HelperSearchResponse(Helper helper) {
        this.id = helper.getId();
        if (helper.getUser() != null) {
            this.userId = helper.getUser().getId();
            this.firstName = helper.getUser().getFirstName();
            this.lastName = helper.getUser().getLastName();
            if (helper.getUser().getCity() != null) {
                this.cityName = helper.getUser().getCity().getName();
                if (helper.getUser().getCity().getCountry() != null) {
                    this.countryName = helper.getUser().getCity().getCountry().getName();
                }
            }
        }
        this.professionalHeadline = helper.getProfessionalHeadline();
        this.languagesSpoken = helper.getLanguagesSpoken();
        this.isAvailable = helper.getIsAvailable();
        
        if (helper.getCategories() != null) {
            this.specializations = helper.getCategories().stream()
                .map(hc -> new SpecializationInfo(
                    hc.getCategory() != null ? hc.getCategory().getName() : null,
                    hc.getHourlyRate(),
                    hc.getYearsExperience()
                ))
                .collect(Collectors.toList());
        }
    }

    public static class SpecializationInfo {
        private String categoryName;
        private BigDecimal hourlyRate;
        private Integer yearsExperience;

        public SpecializationInfo(String categoryName, BigDecimal hourlyRate, Integer yearsExperience) {
            this.categoryName = categoryName;
            this.hourlyRate = hourlyRate;
            this.yearsExperience = yearsExperience;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

        public Integer getYearsExperience() { return yearsExperience; }
        public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
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

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public List<SpecializationInfo> getSpecializations() { return specializations; }
    public void setSpecializations(List<SpecializationInfo> specializations) { 
        this.specializations = specializations; 
    }
}
