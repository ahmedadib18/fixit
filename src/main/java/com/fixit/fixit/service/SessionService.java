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
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    @Lazy
    private com.fixit.fixit.service.BillingService billingService;

    @Autowired
    private WebRTCSignalingService webRTCSignalingService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // =============================================
    // INITIATE SESSION
    // =============================================
    @Transactional
    public Session initiateSession(Long userId,
                                   Long helperId,
                                   Long categoryId) {

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Find helper
        Helper helper = helperRepository.findById(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper", "id", helperId));

        // Force initialization of helper categories
        if (helper.getCategories() != null) {
            helper.getCategories().size();
        }

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
        if (category != null && helper.getCategories() != null) {
            helper.getCategories().stream()
                    .filter(hc -> hc.getCategory() != null && hc.getCategory().getId().equals(categoryId))
                    .findFirst()
                    .ifPresent(hc -> session.setHelperRate(hc.getHourlyRate()));
            
            // Validate that helper has a rate for this category
            if (session.getHelperRate() == null) {
                throw new SessionException("Helper does not have a rate set for category: " + category.getName());
            }
        }

        Session savedSession = sessionRepository.save(session);
        
        // Notify helper of new session request via WebSocket
        try {
            String userName = user.getFirstName() + " " + user.getLastName();
            String categoryName = category != null ? category.getName() : "General";
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_SESSION_REQUEST");
            notification.put("sessionId", sessionId);
            notification.put("userName", userName);
            notification.put("categoryName", categoryName);
            notification.put("userId", userId);
            notification.put("helperId", helperId);
            
            // Send to helper-specific topic
            String destination = "/topic/helper/" + helperId + "/notifications";
            messagingTemplate.convertAndSend(destination, (Object) notification);
            
            System.out.println("✅ Sent notification to helper " + helperId + " for session " + sessionId);
        } catch (Exception e) {
            System.err.println("❌ Failed to send notification to helper: " + e.getMessage());
            e.printStackTrace();
            // Don't fail session creation if notification fails
        }
        
        return savedSession;
    }

    // =============================================
    // ACCEPT SESSION
    // =============================================
    @Transactional
    public Session acceptSession(String sessionId) {
        Session session = findById(sessionId);

        if (session.getStatus() != SessionStatus.INITIATED) {
            throw new SessionException("Session cannot be accepted. Current status: " + session.getStatus());
        }

        session.setStatus(SessionStatus.CONNECTED);
        session.setStartedAt(LocalDateTime.now());
        Session savedSession = sessionRepository.save(session);
        
        // Broadcast status update to all participants
        webRTCSignalingService.broadcastSessionStatusUpdate(sessionId, "CONNECTED");
        
        // Notify user that session was accepted
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SESSION_STATUS_UPDATE");
            notification.put("sessionId", sessionId);
            notification.put("status", "CONNECTED");
            
            messagingTemplate.convertAndSend(
                "/topic/user/" + session.getUser().getId() + "/session-updates",
                (Object) notification
            );
            
            System.out.println("✅ Sent session status update to user " + session.getUser().getId());
        } catch (Exception e) {
            System.err.println("❌ Failed to send session status update: " + e.getMessage());
        }
        
        return savedSession;
    }

    // =============================================
    // REJECT SESSION
    // =============================================
    @Transactional
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
    @Transactional
    public Session endSession(String sessionId) {
        Session session = findById(sessionId);

        // Allow ending from INITIATED, CONNECTED, or IN_PROGRESS status
        if (session.getStatus() == SessionStatus.ENDED || 
            session.getStatus() == SessionStatus.CANCELLED ||
            session.getStatus() == SessionStatus.REJECTED) {
            throw new SessionException("Session cannot be ended. Current status: " + session.getStatus());
        }

        // Validate session was actually started
        if (session.getStartedAt() == null) {
            throw new SessionException("Cannot end session that was never started. Session ID: " + sessionId);
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        Session savedSession = sessionRepository.save(session);

        // Create transaction for billing
        try {
            billingService.processSessionPayment(sessionId);
        } catch (Exception e) {
            System.err.println("Failed to process payment for session " + sessionId + ": " + e.getMessage());
            // Don't fail the session end if payment processing fails
        }

        return savedSession;
    }

    // =============================================
    // CANCEL SESSION
    // =============================================
    @Transactional
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
    @Transactional
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
    @Transactional(readOnly = true)
    public Session findById(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        
        // Force initialization of lazy collections
        if (session.getUser() != null) {
            session.getUser().getFirstName();
        }
        if (session.getHelper() != null && session.getHelper().getUser() != null) {
            session.getHelper().getUser().getFirstName();
        }
        if (session.getCategory() != null) {
            session.getCategory().getName();
        }
        
        return session;
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