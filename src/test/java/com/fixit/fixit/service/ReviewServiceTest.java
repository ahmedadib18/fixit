package com.fixit.fixit.service;

import com.fixit.fixit.entity.*;
import com.fixit.fixit.enums.SessionStatus;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.InvalidOperationException;
import com.fixit.fixit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");

        User helperUser = new User();
        helperUser.setId(2L);

        testHelper = new Helper();
        testHelper.setId(1L);
        testHelper.setUser(helperUser);

        testSession = new Session();
        testSession.setId("SES-12345678");
        testSession.setUser(testUser);
        testSession.setHelper(testHelper);
        testSession.setStatus(SessionStatus.ENDED);
    }

    @Test
    void submitReview_Success() {
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-12345678")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(helperRepository.findById(1L)).thenReturn(Optional.of(testHelper));
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review());

        Review result = reviewService.submitReview("SES-12345678", 1L, 1L, 5, "Great service!");

        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void submitReview_SessionNotEnded_ThrowsException() {
        testSession.setStatus(SessionStatus.CONNECTED);
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));

        assertThrows(InvalidOperationException.class, () ->
                reviewService.submitReview("SES-12345678", 1L, 1L, 5, "Great!")
        );
    }

    @Test
    void submitReview_DuplicateReview_ThrowsException() {
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-12345678")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                reviewService.submitReview("SES-12345678", 1L, 1L, 5, "Great!")
        );
    }

    @Test
    void submitReview_InvalidRating_ThrowsException() {
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(reviewRepository.existsBySessionId("SES-12345678")).thenReturn(false);

        assertThrows(InvalidOperationException.class, () ->
                reviewService.submitReview("SES-12345678", 1L, 1L, 6, "Great!")
        );

        assertThrows(InvalidOperationException.class, () ->
                reviewService.submitReview("SES-12345678", 1L, 1L, 0, "Great!")
        );
    }

    @Test
    void getHelperAverageRating_Success() {
        when(reviewRepository.findAverageRatingByHelperId(1L)).thenReturn(4.5);

        Double rating = reviewService.getHelperAverageRating(1L);

        assertEquals(4.5, rating);
    }

    @Test
    void getHelperAverageRating_NoReviews_ReturnsZero() {
        when(reviewRepository.findAverageRatingByHelperId(1L)).thenReturn(null);

        Double rating = reviewService.getHelperAverageRating(1L);

        assertEquals(0.0, rating);
    }
}
