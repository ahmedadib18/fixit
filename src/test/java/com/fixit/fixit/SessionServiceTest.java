package com.fixit.fixit;

import com.fixit.fixit.entity.Category;
import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.SessionChatMessage;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.SessionStatus;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.exception.SessionException;
import com.fixit.fixit.repository.CategoryRepository;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.SessionChatMessageRepository;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.UserRepository;
import com.fixit.fixit.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HelperRepository helperRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SessionService sessionService;

    private User testUser;
    private Helper testHelper;
    private Session testSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");

        testHelper = new Helper();
        testHelper.setId(1L);
        testHelper.setCategories(new ArrayList<>());

        testSession = new Session();
        testSession.setId("SES-TESTID1");
        testSession.setUser(testUser);
        testSession.setHelper(testHelper);
        testSession.setStatus(SessionStatus.INITIATED);
    }

    // =============================================
    // INITIATE SESSION TESTS
    // =============================================

    @Test
    void testInitiateSession_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(helperRepository.findById(1L)).thenReturn(Optional.of(testHelper));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // Act
        Session result = sessionService.initiateSession(1L, 1L, null);

        // Assert
        assertNotNull(result);
        assertEquals("SES-TESTID1", result.getId());
        assertEquals(SessionStatus.INITIATED, result.getStatus());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void testInitiateSession_WithCategory_Success() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("Home Appliances");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(helperRepository.findById(1L)).thenReturn(Optional.of(testHelper));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // Act
        Session result = sessionService.initiateSession(1L, 1L, 1L);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void testInitiateSession_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.initiateSession(99L, 1L, null)
        );
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void testInitiateSession_HelperNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(helperRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.initiateSession(1L, 99L, null)
        );
        verify(sessionRepository, never()).save(any());
    }

    // =============================================
    // ACCEPT SESSION TESTS
    // =============================================

    @Test
    void testAcceptSession_Success() {
        // Arrange
        testSession.setStatus(SessionStatus.INITIATED);
        Session acceptedSession = new Session();
        acceptedSession.setId("SES-TESTID1");
        acceptedSession.setStatus(SessionStatus.CONNECTED);
        acceptedSession.setStartedAt(LocalDateTime.now());

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class)))
                .thenReturn(acceptedSession);

        // Act
        Session result = sessionService.acceptSession("SES-TESTID1");

        // Assert
        assertEquals(SessionStatus.CONNECTED, result.getStatus());
        assertNotNull(result.getStartedAt());
    }

    @Test
    void testAcceptSession_NotInitiated_ThrowsException() {
        // Arrange
        testSession.setStatus(SessionStatus.CONNECTED);
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(SessionException.class, () ->
                sessionService.acceptSession("SES-TESTID1")
        );
    }

    // =============================================
    // REJECT SESSION TESTS
    // =============================================

    @Test
    void testRejectSession_Success() {
        // Arrange
        testSession.setStatus(SessionStatus.INITIATED);
        Session rejectedSession = new Session();
        rejectedSession.setId("SES-TESTID1");
        rejectedSession.setStatus(SessionStatus.REJECTED);

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class)))
                .thenReturn(rejectedSession);

        // Act
        Session result = sessionService.rejectSession("SES-TESTID1");

        // Assert
        assertEquals(SessionStatus.REJECTED, result.getStatus());
    }

    @Test
    void testRejectSession_NotInitiated_ThrowsException() {
        // Arrange
        testSession.setStatus(SessionStatus.ENDED);
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(SessionException.class, () ->
                sessionService.rejectSession("SES-TESTID1")
        );
    }

    // =============================================
    // END SESSION TESTS
    // =============================================

    @Test
    void testEndSession_Success() {
        // Arrange
        testSession.setStatus(SessionStatus.CONNECTED);
        testSession.setStartedAt(LocalDateTime.now().minusMinutes(30));

        Session endedSession = new Session();
        endedSession.setId("SES-TESTID1");
        endedSession.setStatus(SessionStatus.ENDED);
        endedSession.setEndedAt(LocalDateTime.now());

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class)))
                .thenReturn(endedSession);

        // Act
        Session result = sessionService.endSession("SES-TESTID1");

        // Assert
        assertEquals(SessionStatus.ENDED, result.getStatus());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void testEndSession_NotConnected_ThrowsException() {
        // Arrange
        testSession.setStatus(SessionStatus.INITIATED);
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(SessionException.class, () ->
                sessionService.endSession("SES-TESTID1")
        );
    }

    // =============================================
    // CANCEL SESSION TESTS
    // =============================================

    @Test
    void testCancelSession_Success() {
        // Arrange
        testSession.setStatus(SessionStatus.INITIATED);
        Session cancelledSession = new Session();
        cancelledSession.setStatus(SessionStatus.CANCELLED);

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class)))
                .thenReturn(cancelledSession);

        // Act
        Session result = sessionService.cancelSession("SES-TESTID1");

        // Assert
        assertEquals(SessionStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelSession_AlreadyEnded_ThrowsException() {
        // Arrange
        testSession.setStatus(SessionStatus.ENDED);
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(SessionException.class, () ->
                sessionService.cancelSession("SES-TESTID1")
        );
    }

    @Test
    void testCancelSession_AlreadyCancelled_ThrowsException() {
        // Arrange
        testSession.setStatus(SessionStatus.CANCELLED);
        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        assertThrows(SessionException.class, () ->
                sessionService.cancelSession("SES-TESTID1")
        );
    }

    // =============================================
    // SEND MESSAGE TESTS
    // =============================================

    @Test
    void testSendMessage_Success() {
        // Arrange
        SessionChatMessage message = new SessionChatMessage();
        message.setId(1L);
        message.setMessageText("Hello!");

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(SessionChatMessage.class)))
                .thenReturn(message);

        // Act
        SessionChatMessage result = sessionService.sendMessage(
                "SES-TESTID1", 1L, "Hello!");

        // Assert
        assertNotNull(result);
        assertEquals("Hello!", result.getMessageText());
        verify(chatMessageRepository).save(any(SessionChatMessage.class));
    }

    @Test
    void testSendMessage_SessionNotFound_ThrowsException() {
        // Arrange
        when(sessionRepository.findById("INVALID"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.sendMessage("INVALID", 1L, "Hello!")
        );
    }

    // =============================================
    // SESSION DURATION TESTS
    // =============================================

    @Test
    void testGetSessionDurationMinutes_Success() {
        // Arrange
        testSession.setStartedAt(LocalDateTime.now().minusMinutes(45));
        testSession.setEndedAt(LocalDateTime.now());

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act
        long duration = sessionService.getSessionDurationMinutes("SES-TESTID1");

        // Assert
        assertTrue(duration >= 44 && duration <= 46);
    }

    @Test
    void testGetSessionDurationMinutes_NotStarted_ReturnsZero() {
        // Arrange
        testSession.setStartedAt(null);
        testSession.setEndedAt(null);

        when(sessionRepository.findById("SES-TESTID1"))
                .thenReturn(Optional.of(testSession));

        // Act
        long duration = sessionService.getSessionDurationMinutes("SES-TESTID1");

        // Assert
        assertEquals(0, duration);
    }
}