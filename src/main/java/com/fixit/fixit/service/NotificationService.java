package com.fixit.fixit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // =============================================
    // NOTIFY HELPER OF SESSION REQUEST
    // =============================================
    public void notifyHelper(Long helperId, String sessionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SESSION_REQUEST");
        payload.put("sessionId", sessionId);
        payload.put("helperId", helperId);

        messagingTemplate.convertAndSend(
                "/topic/helper/" + helperId,
                (Object) payload);
    }

    // =============================================
    // PUSH SESSION REQUEST
    // =============================================
    public void pushSessionRequest(String sessionId,
                                   String userName,
                                   String category) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "INCOMING_SESSION");
        payload.put("sessionId", sessionId);
        payload.put("userName", userName);
        payload.put("category", category);

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                (Object) payload);
    }

    // =============================================
    // NOTIFY SESSION ACCEPTED
    // =============================================
    public void notifySessionAccepted(String sessionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SESSION_ACCEPTED");
        payload.put("sessionId", sessionId);

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                (Object) payload);
    }

    // =============================================
    // NOTIFY SESSION REJECTED
    // =============================================
    public void notifySessionRejected(String sessionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SESSION_REJECTED");
        payload.put("sessionId", sessionId);

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                (Object) payload);
    }

    // =============================================
    // NOTIFY SESSION ENDED
    // =============================================
    public void notifySessionEnded(String sessionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SESSION_ENDED");
        payload.put("sessionId", sessionId);

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                (Object) payload);
    }

    // =============================================
    // NOTIFY BILLING COMPLETED
    // =============================================
    public void notifyBillingCompleted(Long userId,
                                       String sessionId,
                                       String receiptNumber) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "BILLING_COMPLETED");
        payload.put("sessionId", sessionId);
        payload.put("receiptNumber", receiptNumber);

        messagingTemplate.convertAndSend(
                "/topic/user/" + userId,
                (Object) payload);
    }
}