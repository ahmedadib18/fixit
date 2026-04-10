package com.fixit.fixit.service;

import com.fixit.fixit.entity.*;
import com.fixit.fixit.enums.SessionStatus;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.exception.SessionException;
import com.fixit.fixit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HelperRepository helperRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SessionChatMessageRepository chatMessageRepository;

    @InjectMocks
    private SessionService sessionService;

    private User testUser;
    private Helper testHelper;
    private Category testCategory;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        User helperUser = new User();
        helperUser.setId(2L);

        testHelper = new Helper();
        testHelper.setId(1L);
        testHelper.setUser(helperUser);
        testHelper.setCategories(new java.util.ArrayList<>()); // Initialize empty list

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testSession = new Session();
        testSession.setId("SES-12345678");
        testSession.setUser(testUser);
        testSession.setHelper(testHelper);
        testSession.setCategory(testCategory);
        testSession.setStatus(SessionStatus.INITIATED);
        testSession.setHelperRate(new BigDecimal("50.00"));
    }

    @Test
    void initiateSession_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(helperRepository.findById(1L)).thenReturn(Optional.of(testHelper));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session result = sessionService.initiateSession(1L, 1L, 1L);

        assertNotNull(result);
        assertEquals(SessionStatus.INITIATED, result.getStatus());
        assertTrue(result.getId().startsWith("SES-"));
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void initiateSession_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.initiateSession(999L, 1L, 1L)
        );
    }

    @Test
    void acceptSession_Success() {
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session result = sessionService.acceptSession("SES-12345678");

        assertNotNull(result);
        assertEquals(SessionStatus.CONNECTED, result.getStatus());
        assertNotNull(result.getStartedAt());
    }

    @Test
    void acceptSession_InvalidStatus_ThrowsException() {
        testSession.setStatus(SessionStatus.ENDED);
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));

        assertThrows(SessionException.class, () ->
                sessionService.acceptSession("SES-12345678")
        );
    }

    @Test
    void rejectSession_Success() {
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session result = sessionService.rejectSession("SES-12345678");

        assertNotNull(result);
        assertEquals(SessionStatus.REJECTED, result.getStatus());
    }

    @Test
    void endSession_Success() {
        testSession.setStatus(SessionStatus.CONNECTED);
        testSession.setStartedAt(LocalDateTime.now().minusHours(1));
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session result = sessionService.endSession("SES-12345678");

        assertNotNull(result);
        assertEquals(SessionStatus.ENDED, result.getStatus());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void endSession_NotConnected_ThrowsException() {
        testSession.setStatus(SessionStatus.INITIATED);
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));

        assertThrows(SessionException.class, () ->
                sessionService.endSession("SES-12345678")
        );
    }

    @Test
    void cancelSession_Success() {
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session result = sessionService.cancelSession("SES-12345678");

        assertNotNull(result);
        assertEquals(SessionStatus.CANCELLED, result.getStatus());
    }

    @Test
    void getSessionDurationMinutes_Success() {
        testSession.setStartedAt(LocalDateTime.now().minusMinutes(30));
        testSession.setEndedAt(LocalDateTime.now());
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));

        long duration = sessionService.getSessionDurationMinutes("SES-12345678");

        assertTrue(duration >= 29 && duration <= 31); // Allow 1 minute tolerance
    }

    @Test
    void getSessionDurationMinutes_NoEndTime_ReturnsZero() {
        testSession.setStartedAt(LocalDateTime.now());
        testSession.setEndedAt(null);
        when(sessionRepository.findById("SES-12345678")).thenReturn(Optional.of(testSession));

        long duration = sessionService.getSessionDurationMinutes("SES-12345678");

        assertEquals(0, duration);
    }
}
