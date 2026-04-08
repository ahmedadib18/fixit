package com.fixit.fixit.service;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Review;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.SessionStatus;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.InvalidOperationException;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.ReviewRepository;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HelperRepository helperRepository;

    // =============================================
    // SUBMIT REVIEW
    // =============================================
    public Review submitReview(String sessionId,
                               Long userId,
                               Long helperId,
                               Integer rating,
                               String reviewText) {

        // Validate session exists and is ended
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (session.getStatus() != SessionStatus.ENDED) {
            throw new InvalidOperationException("Cannot review a session that has not ended");
        }

        // Check if review already exists for this session
        if (reviewRepository.existsBySessionId(sessionId)) {
            throw new DuplicateResourceException("Review already submitted for session: " + sessionId);
        }

        // Validate rating is between 1 and 5
        if (rating < 1 || rating > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5");
        }

        // Find user and helper
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Helper helper = helperRepository.findById(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper", "id", helperId));

        // Create review
        Review review = new Review();
        review.setSession(session);
        review.setUser(user);
        review.setHelper(helper);
        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setIsPublic(true);

        return reviewRepository.save(review);
    }

    // =============================================
    // GET HELPER WITH REVIEWS
    // =============================================
    public Helper getHelperWithReviews(Long helperId) {
        return helperRepository.findByIdWithUser(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper", "id", helperId));
    }

    // =============================================
    // GET HELPER REVIEWS
    // =============================================
    public List<Review> getHelperReviews(Long helperId) {
        return reviewRepository
                .findByHelperIdOrderByCreatedAtDesc(helperId);
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
    // GET REVIEW BY SESSION
    // =============================================
    public Review getReviewBySession(String sessionId) {
        return reviewRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for session: " + sessionId));
    }

    // =============================================
    // GET USER REVIEWS
    // =============================================
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    // =============================================
    // UPDATE REVIEW VISIBILITY
    // =============================================
    public Review updateReviewVisibility(Long reviewId,
                                         Boolean isPublic) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        review.setIsPublic(isPublic);
        return reviewRepository.save(review);
    }

    // =============================================
    // DELETE REVIEW
    // =============================================
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }
}
