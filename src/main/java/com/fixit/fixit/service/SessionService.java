package com.fixit.fixit.service;

import com.fixit.fixit.entity.Category;
import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.SessionChatMessage;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.MessageType;
import com.fixit.fixit.enums.SessionStatus;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.exception.SessionException;
import com.fixit.fixit.repository.CategoryRepository;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.SessionChatMessageRepository;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // =============================================
    // INITIATE SESSION
    // =============================================
    public Session initiateSession(Long userId,
                                   Long helperId,
                                   Long categoryId) {

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Find helper
        Helper helper = helperRepository.findById(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper", "id", helperId));

        // Find category if provided
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        }

        // Generate unique session ID in format SES-XXXX
        String sessionId = "SES-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        // Create session
        Session session = new Session();
        session.setId(sessionId);
        session.setUser(user);
        session.setHelper(helper);
        session.setCategory(category);
        session.setStatus(SessionStatus.INITIATED);

        // Set helper rate from their category
        if (category != null) {
            helper.getCategories().stream()
                    .filter(hc -> hc.getCategory().getId().equals(categoryId))
                    .findFirst()
                    .ifPresent(hc -> session.setHelperRate(hc.getHourlyRate()));
        }

        return sessionRepository.save(session);
    }

    // =============================================
    // ACCEPT SESSION
    // =============================================
    public Session acceptSession(String sessionId) {
        Session session = findById(sessionId);

        if (session.getStatus() != SessionStatus.INITIATED) {
            throw new SessionException("Session cannot be accepted. Current status: " + session.getStatus());
        }

        session.setStatus(SessionStatus.CONNECTED);
        session.setStartedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // =============================================
    // REJECT SESSION
    // =============================================
    public Session rejectSession(String sessionId) {
        Session session = findById(sessionId);

        if (session.getStatus() != SessionStatus.INITIATED) {
            throw new SessionException("Session cannot be rejected. Current status: " + session.getStatus());
        }

        session.setStatus(SessionStatus.REJECTED);
        return sessionRepository.save(session);
    }

    // =============================================
    // END SESSION
    // =============================================
    public Session endSession(String sessionId) {
        Session session = findById(sessionId);

        if (session.getStatus() != SessionStatus.CONNECTED) {
            throw new SessionException("Session cannot be ended. Current status: " + session.getStatus());
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // =============================================
    // CANCEL SESSION
    // =============================================
    public Session cancelSession(String sessionId) {
        Session session = findById(sessionId);

        if (session.getStatus() == SessionStatus.ENDED ||
                session.getStatus() == SessionStatus.CANCELLED) {
            throw new SessionException("Session cannot be cancelled. Current status: " + session.getStatus());
        }

        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);
    }

    // =============================================
    // SEND MESSAGE
    // =============================================
    public SessionChatMessage sendMessage(String sessionId,
                                          Long userId,
                                          String messageText) {

        Session session = findById(sessionId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        SessionChatMessage message = new SessionChatMessage();
        message.setSession(session);
        message.setSender(sender);
        message.setMessageText(messageText);
        message.setMessageType(MessageType.TEXT);

        return chatMessageRepository.save(message);
    }

    // =============================================
    // GET SESSION BY ID
    // =============================================
    public Session findById(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
    }

    // =============================================
    // GET USER SESSIONS
    // =============================================
    public List<Session> getUserSessions(Long userId) {
        return sessionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // =============================================
    // GET HELPER SESSIONS
    // =============================================
    public List<Session> getHelperSessions(Long helperId) {
        return sessionRepository
                .findByHelperIdOrderByCreatedAtDesc(helperId);
    }

    // =============================================
    // GET SESSION DURATION IN MINUTES
    // =============================================
    public long getSessionDurationMinutes(String sessionId) {
        Session session = findById(sessionId);

        if (session.getStartedAt() == null || session.getEndedAt() == null) {
            return 0;
        }

        return java.time.Duration.between(
                session.getStartedAt(),
                session.getEndedAt()).toMinutes();
    }
}