package com.fixit.fixit.service;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.HelperCategory;
import com.fixit.fixit.entity.Category;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.repository.CategoryRepository;
import com.fixit.fixit.repository.HelperCategoryRepository;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class HelperService {

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private HelperCategoryRepository helperCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // FIND HELPER BY ID
    // =============================================
    public Helper findById(Long helperId) {
        return helperRepository.findById(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper", "id", helperId));
    }

    // =============================================
    // FIND HELPER BY USER ID
    // =============================================
    public Helper findByUserId(Long userId) {
        return helperRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper profile not found for user: " + userId));
    }

    // =============================================
    // CREATE HELPER PROFILE
    // =============================================
    public Helper createHelperProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if helper profile already exists
        if (helperRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateResourceException("Helper profile already exists for user: " + userId);
        }

        Helper helper = new Helper();
        helper.setUser(user);
        helper.setIsAvailable(false);
        return helperRepository.save(helper);
    }

    // =============================================
    // SAVE HELPER PROFILE
    // =============================================
    public Helper saveHelperProfile(Long helperId,
                                    String headline,
                                    String languages) {
        Helper helper = findById(helperId);
        helper.setProfessionalHeadline(headline);
        helper.setLanguagesSpoken(languages);
        return helperRepository.save(helper);
    }

    // =============================================
    // SYNC CATEGORY SPECIALIZATIONS
    // =============================================
    @Transactional
    public List<HelperCategory> syncCategorySpecializations(
            Long helperId,
            List<Long> categoryIds,
            List<BigDecimal> hourlyRates,
            List<Integer> yearsExperiences) {

        Helper helper = findById(helperId);
        List<HelperCategory> result = new ArrayList<>();

        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

            // Check if already exists
            List<HelperCategory> existing = helperCategoryRepository.findByHelperId(helperId);
            HelperCategory helperCategory = existing.stream()
                    .filter(hc -> hc.getCategory().getId().equals(categoryId))
                    .findFirst()
                    .orElse(new HelperCategory());

            helperCategory.setHelper(helper);
            helperCategory.setCategory(category);
            helperCategory.setHourlyRate(hourlyRates.get(i));
            helperCategory.setYearsExperience(yearsExperiences.get(i));

            result.add(helperCategoryRepository.save(helperCategory));
        }

        return result;
    }

    // =============================================
    // UPLOAD CERTIFICATE
    // =============================================
    public HelperCategory uploadCertificate(Long helperCategoryId,
                                            String certificateUrl) {
        HelperCategory helperCategory = helperCategoryRepository
                .findById(helperCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("HelperCategory", "id", helperCategoryId));

        helperCategory.setCertificateUrl(certificateUrl);
        return helperCategoryRepository.save(helperCategory);
    }

    // =============================================
    // UPDATE AVAILABILITY
    // =============================================
    public Helper updateAvailability(Long helperId, Boolean isAvailable) {
        Helper helper = findById(helperId);
        helper.setIsAvailable(isAvailable);
        return helperRepository.save(helper);
    }

    // =============================================
    // GET HELPER CATEGORIES
    // =============================================
    public List<HelperCategory> getHelperCategories(Long helperId) {
        return helperCategoryRepository.findByHelperId(helperId);
    }

    // =============================================
    // GET HELPER WITH USER
    // =============================================
    public Helper getHelperWithUser(Long helperId) {
        return helperRepository.findByIdWithUser(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper", "id", helperId));
    }
}