package com.fixit.fixit.service;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Review;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // =============================================
    // SEARCH HELPERS
    // =============================================
    @Transactional(readOnly = true)
    public List<Helper> searchHelpers(Long categoryId,
                                      Double minRating,
                                      BigDecimal maxPrice,
                                      String language,
                                      Boolean availableNow,
                                      Long cityId) {

        System.out.println("=== SearchService.searchHelpers ===");
        System.out.println("Calling repository with availableNow: " + availableNow);
        
        // Get helpers based on filters
        List<Helper> helpers = helperRepository.searchHelpers(
                categoryId,
                language,
                maxPrice,
                availableNow,
                cityId);

        System.out.println("Repository returned " + helpers.size() + " helpers");
        
        // Force initialization of collections
        helpers.forEach(helper -> {
            System.out.println("Helper ID: " + helper.getId() + ", isAvailable: " + helper.getIsAvailable() + ", categories: " + (helper.getCategories() != null ? helper.getCategories().size() : 0));
            if (helper.getCategories() != null) {
                helper.getCategories().size();
            }
            if (helper.getUser() != null && helper.getUser().getCity() != null) {
                helper.getUser().getCity().getName();
                if (helper.getUser().getCity().getCountry() != null) {
                    helper.getUser().getCity().getCountry().getName();
                }
            }
        });

        // Filter by minimum rating if provided
        if (minRating != null) {
            helpers = helpers.stream()
                    .filter(helper -> {
                        Double avgRating = reviewRepository
                                .findAverageRatingByHelperId(helper.getId());
                        return avgRating != null && avgRating >= minRating;
                    })
                    .toList();
        }

        // Sort by average rating descending
        helpers = helpers.stream()
                .sorted((h1, h2) -> {
                    Double rating1 = reviewRepository
                            .findAverageRatingByHelperId(h1.getId());
                    Double rating2 = reviewRepository
                            .findAverageRatingByHelperId(h2.getId());
                    rating1 = rating1 != null ? rating1 : 0.0;
                    rating2 = rating2 != null ? rating2 : 0.0;
                    return Double.compare(rating2, rating1);
                })
                .toList();

        return helpers;
    }

    // =============================================
    // GET HELPER PROFILE
    // =============================================
    @Transactional(readOnly = true)
    public Helper getHelperProfile(Long helperId) {
        Helper helper = helperRepository.findByIdWithUser(helperId)
                .orElseThrow(() -> new RuntimeException(
                        "Helper not found: " + helperId));
        
        // Force initialization of collections
        if (helper.getCategories() != null) {
            helper.getCategories().size();
            helper.getCategories().forEach(hc -> {
                if (hc.getCategory() != null) {
                    hc.getCategory().getName();
                }
            });
        }
        if (helper.getUser() != null && helper.getUser().getCity() != null) {
            helper.getUser().getCity().getName();
            if (helper.getUser().getCity().getCountry() != null) {
                helper.getUser().getCity().getCountry().getName();
            }
        }
        
        return helper;
    }

    // =============================================
    // GET HELPER AVERAGE RATING
    // =============================================
    public Double getHelperAverageRating(Long helperId) {
        Double avgRating = reviewRepository
                .findAverageRatingByHelperId(helperId);
        return avgRating != null ? avgRating : 0.0;
    }

    // =============================================
    // GET HELPER REVIEWS
    // =============================================
    public List<Review> getHelperReviews(Long helperId) {
        return reviewRepository
                .findByHelperIdOrderByCreatedAtDesc(helperId);
    }

    // =============================================
    // GET HELPERS BY CITY
    // =============================================
    public List<Helper> getHelpersByCity(Long cityId) {
        return helperRepository.findByUserCityId(cityId);
    }

    // =============================================
    // GET AVAILABLE HELPERS
    // =============================================
    public List<Helper> getAvailableHelpers() {
        return helperRepository.findByIsAvailableTrue();
    }
}
