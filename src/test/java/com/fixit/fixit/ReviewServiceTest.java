package com.fixit.fixit;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Review;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.SessionStatus;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.ReviewRepository;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.UserRepository;
import com.fixit.fixit.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HelperRepository helperRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Session testSession;
    private User testUser;
    private Helper testHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");

        testHelper = new Helper();
        testHelper.setId(1L);

        testSession = new Session();
        testSession.setId("SES-TESTID1");
        testSession.setUser(testUser);
        testSession.setHelper(testHelper);
        testSession.setStatus(SessionStatus.ENDED);
    }

    // =============================================
    // SUBMIT REVIEW TESTS
    // =============================================

    @Test
    void testSubmitReview_Success() {
        // Arrange
        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setRating(5);
        savedReview.setReviewText("Excellent service!");
        savedReview.setIsPublic(true);

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-TESTID1"))
                .thenReturn(false);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(helperRepository.findById(1L))
                .thenReturn(Optional.of(testHelper));
        when(reviewRepository.save(any(Review.class)))
                .thenReturn(savedReview);

        // Act
        Review result = reviewService.submitReview(
                "SES-TESTID1", 1L, 1L, 5, "Excellent service!");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excellent service!", result.getReviewText());
        assertTrue(result.getIsPublic());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testSubmitReview_SessionNotEnded_ThrowsException() {
        // Arrange
        testSession.setStatus(SessionStatus.CONNECTED);
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reviewService.submitReview(
                        "SES-TESTID1", 1L, 1L, 5, "Great!")
        );
        assertTrue(exception.getMessage().contains("ended"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testSubmitReview_DuplicateReview_ThrowsException() {
        // Arrange
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-TESTID1"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                reviewService.submitReview(
                        "SES-TESTID1", 1L, 1L, 4, "Good!")
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testSubmitReview_InvalidRatingTooHigh_ThrowsException() {
        // Arrange
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-TESTID1"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                reviewService.submitReview(
                        "SES-TESTID1", 1L, 1L, 6, "Amazing!")
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testSubmitReview_InvalidRatingTooLow_ThrowsException() {
        // Arrange
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-TESTID1"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                reviewService.submitReview(
                        "SES-TESTID1", 1L, 1L, 0, "Bad!")
        );
        verify(reviewRepository, never()).save(any());
    }

    // =============================================
    // GET HELPER REVIEWS TESTS
    // =============================================

    @Test
    void testGetHelperReviews_Success() {
        // Arrange
        Review r1 = new Review();
        r1.setId(1L);
        r1.setRating(5);

        Review r2 = new Review();
        r2.setId(2L);
        r2.setRating(4);

        when(reviewRepository.findByHelperIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(r1, r2));

        // Act
        List<Review> result = reviewService.getHelperReviews(1L);

        // Assert
        assertEquals(2, result.size());
        verify(reviewRepository).findByHelperIdOrderByCreatedAtDesc(1L);
    }

    // =============================================
    // GET AVERAGE RATING TESTS
    // =============================================

    @Test
    void testGetHelperAverageRating_Success() {
        // Arrange
        when(reviewRepository.findAverageRatingByHelperId(1L))
                .thenReturn(4.5);

        // Act
        Double result = reviewService.getHelperAverageRating(1L);

        // Assert
        assertEquals(4.5, result);
    }

    @Test
    void testGetHelperAverageRating_NoReviews_ReturnsZero() {
        // Arrange
        when(reviewRepository.findAverageRatingByHelperId(1L))
                .thenReturn(null);

        // Act
        Double result = reviewService.getHelperAverageRating(1L);

        // Assert
        assertEquals(0.0, result);
    }

    // =============================================
    // UPDATE REVIEW VISIBILITY TESTS
    // =============================================

    @Test
    void testUpdateReviewVisibility_MakePrivate() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setIsPublic(true);

        Review updatedReview = new Review();
        updatedReview.setId(1L);
        updatedReview.setIsPublic(false);

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class)))
                .thenReturn(updatedReview);

        // Act
        Review result = reviewService.updateReviewVisibility(1L, false);

        // Assert
        assertFalse(result.getIsPublic());
    }

    // =============================================
    // DELETE REVIEW TESTS
    // =============================================

    @Test
    void testDeleteReview_Success() {
        // Arrange
        when(reviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1L);

        // Act & Assert
        assertDoesNotThrow(() -> reviewService.deleteReview(1L));
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void testDeleteReview_NotFound_ThrowsException() {
        // Arrange
        when(reviewRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                reviewService.deleteReview(99L)
        );
        verify(reviewRepository, never()).deleteById(any());
    }
}