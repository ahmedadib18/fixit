package com.fixit.fixit.dto;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Review;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class HelperDetailResponse {
    private HelperInfo helper;
    private Double averageRating;
    private List<ReviewInfo> reviews;

    public HelperDetailResponse(Helper helper, Double averageRating, List<Review> reviews) {
        this.helper = new HelperInfo(helper);
        this.averageRating = averageRating;
        this.reviews = reviews != null ? reviews.stream()
                .map(ReviewInfo::new)
                .collect(Collectors.toList()) : null;
    }

    public static class HelperInfo {
        private Long id;
        private UserInfo user;
        private String professionalHeadline;
        private String languagesSpoken;
        private Boolean isAvailable;
        private List<CategoryInfo> helperCategories;

        public HelperInfo(Helper helper) {
            this.id = helper.getId();
            if (helper.getUser() != null) {
                this.user = new UserInfo(helper.getUser().getId(), 
                                        helper.getUser().getFirstName(), 
                                        helper.getUser().getLastName());
            }
            this.professionalHeadline = helper.getProfessionalHeadline();
            this.languagesSpoken = helper.getLanguagesSpoken();
            this.isAvailable = helper.getIsAvailable();
            
            if (helper.getCategories() != null) {
                this.helperCategories = helper.getCategories().stream()
                    .map(hc -> new CategoryInfo(
                        hc.getId(),
                        hc.getCategory() != null ? hc.getCategory().getId() : null,
                        hc.getCategory() != null ? hc.getCategory().getName() : null,
                        hc.getHourlyRate(),
                        hc.getYearsExperience()
                    ))
                    .collect(Collectors.toList());
            }
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public UserInfo getUser() { return user; }
        public void setUser(UserInfo user) { this.user = user; }

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

        public List<CategoryInfo> getHelperCategories() { return helperCategories; }
        public void setHelperCategories(List<CategoryInfo> helperCategories) { 
            this.helperCategories = helperCategories; 
        }
    }

    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;

        public UserInfo(Long id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class CategoryInfo {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private BigDecimal hourlyRate;
        private Integer yearsExperience;

        public CategoryInfo(Long id, Long categoryId, String categoryName, 
                          BigDecimal hourlyRate, Integer yearsExperience) {
            this.id = id;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.hourlyRate = hourlyRate;
            this.yearsExperience = yearsExperience;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

        public Integer getYearsExperience() { return yearsExperience; }
        public void setYearsExperience(Integer yearsExperience) { 
            this.yearsExperience = yearsExperience; 
        }
    }

    public static class ReviewInfo {
        private Long id;
        private Integer rating;
        private String reviewText;
        private String createdAt;

        public ReviewInfo(Review review) {
            this.id = review.getId();
            this.rating = review.getRating();
            this.reviewText = review.getReviewText();
            this.createdAt = review.getCreatedAt() != null ? 
                           review.getCreatedAt().toString() : null;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getReviewText() { return reviewText; }
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    // Getters and Setters
    public HelperInfo getHelper() { return helper; }
    public void setHelper(HelperInfo helper) { this.helper = helper; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public List<ReviewInfo> getReviews() { return reviews; }
    public void setReviews(List<ReviewInfo> reviews) { this.reviews = reviews; }
}
