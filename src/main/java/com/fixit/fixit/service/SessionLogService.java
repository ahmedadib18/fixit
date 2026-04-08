package com.fixit.fixit.service;

import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.SessionChatMessage;
import com.fixit.fixit.exception.UnauthorizedException;
import com.fixit.fixit.repository.SessionChatMessageRepository;
import com.fixit.fixit.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SessionLogService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionChatMessageRepository chatMessageRepository;

    // =============================================
    // GET SESSION LOG
    // =============================================
    public Session getSessionLog(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException(
                        "Session not found: " + sessionId));
    }

    // =============================================
    // GET SESSION CHAT LOG
    // =============================================
    public List<SessionChatMessage> getSessionChatLog(String sessionId) {
        return chatMessageRepository
                .findBySessionIdOrderBySentAtAsc(sessionId);
    }

    // =============================================
    // UPDATE USER CONSENT
    // =============================================
    public Session updateUserConsent(String sessionId,
                                     Long userId,
                                     Boolean consentValue) {

        Session session = getSessionLog(sessionId);

        // Verify this user belongs to this session
        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("User is not part of this session: " + sessionId);
        }

        session.setUserConsentPublic(consentValue);
        return sessionRepository.save(session);
    }

    // =============================================
    // UPDATE HELPER CONSENT
    // =============================================
    public Session updateHelperConsent(String sessionId,
                                       Long helperId,
                                       Boolean consentValue) {

        Session session = getSessionLog(sessionId);

        // Verify this helper belongs to this session
        if (!session.getHelper().getId().equals(helperId)) {
            throw new UnauthorizedException("Helper is not part of this session: " + sessionId);
        }

        session.setHelperConsentPublic(consentValue);
        return sessionRepository.save(session);
    }

    // =============================================
    // IS LOG PUBLIC
    // =============================================
    public Boolean isLogPublic(String sessionId) {
        Session session = getSessionLog(sessionId);

        // Log is public only when BOTH parties consent
        // as per wireframe 08 and NFR 2.1
        return session.getUserConsentPublic() &&
                session.getHelperConsentPublic();
    }

    // =============================================
    // EXPORT SESSION LOG
    // =============================================
    public String exportSessionLog(String sessionId) {
        Session session = getSessionLog(sessionId);
        List<SessionChatMessage> messages = getSessionChatLog(sessionId);

        StringBuilder export = new StringBuilder();
        export.append("Session Log Export\n");
        export.append("==================\n");
        export.append("Session ID: ").append(session.getId()).append("\n");
        export.append("Date: ").append(session.getStartedAt()).append("\n");
        export.append("User: ")
                .append(session.getUser().getFirstName())
                .append(" ")
                .append(session.getUser().getLastName())
                .append("\n");
        export.append("Helper: ")
                .append(session.getHelper().getUser().getFirstName())
                .append(" ")
                .append(session.getHelper().getUser().getLastName())
                .append("\n");
        export.append("Duration: ")
                .append(session.getStartedAt() != null &&
                        session.getEndedAt() != null ?
                        java.time.Duration.between(
                                session.getStartedAt(),
                                session.getEndedAt()).toMinutes() : 0)
                .append(" minutes\n");
        export.append("==================\n");
        export.append("Chat Transcript:\n");

        // Add each message
        messages.forEach(msg -> export
                .append("[")
                .append(msg.getSentAt())
                .append("] ")
                .append(msg.getSender().getFirstName())
                .append(": ")
                .append(msg.getMessageText())
                .append("\n"));

        return export.toString();
    }

    // =============================================
    // REQUEST LOG DELETION
    // =============================================
    public Session requestLogDeletion(String sessionId) {
        Session session = getSessionLog(sessionId);
        session.setDeletionRequested(true);
        return sessionRepository.save(session);
    }

    // =============================================
    // UPDATE RETENTION MONTHS
    // =============================================
    public Session updateRetentionMonths(String sessionId,
                                         Integer months) {
        Session session = getSessionLog(sessionId);
        session.setRetentionMonths(months);
        return sessionRepository.save(session);
    }
}