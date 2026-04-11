package com.fixit.fixit.service;

import com.fixit.fixit.entity.Session;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebRTCSignalingService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SessionRepository sessionRepository;

    // =============================================
    // SEND OFFER
    // =============================================
    public void sendOffer(String sessionId, String sdp, Long senderId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        SignalingMessage message = new SignalingMessage();
        message.setType("offer");
        message.setSdp(sdp);
        message.setSenderId(senderId);
        message.setSessionId(sessionId);

        // Send to session topic
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    // =============================================
    // SEND ANSWER
    // =============================================
    public void sendAnswer(String sessionId, String sdp, Long senderId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        SignalingMessage message = new SignalingMessage();
        message.setType("answer");
        message.setSdp(sdp);
        message.setSenderId(senderId);
        message.setSessionId(sessionId);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    // =============================================
    // SEND ICE CANDIDATE
    // =============================================
    public void sendIceCandidate(String sessionId, Map<String, Object> candidate, Long senderId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        SignalingMessage message = new SignalingMessage();
        message.setType("ice-candidate");
        message.setCandidate(candidate);
        message.setSenderId(senderId);
        message.setSessionId(sessionId);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    // =============================================
    // NOTIFY USER JOINED
    // =============================================
    public void notifyUserJoined(String sessionId, Long userId, String userName) {
        SignalingMessage message = new SignalingMessage();
        message.setType("user-joined");
        message.setSenderId(userId);
        message.setSessionId(sessionId);
        message.setUserName(userName);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    // =============================================
    // NOTIFY USER LEFT
    // =============================================
    public void notifyUserLeft(String sessionId, Long userId) {
        SignalingMessage message = new SignalingMessage();
        message.setType("user-left");
        message.setSenderId(userId);
        message.setSessionId(sessionId);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    // =============================================
    // BROADCAST CHAT MESSAGE
    // =============================================
    public void broadcastChatMessage(String sessionId, Map<String, Object> payload) {
        String destination = "/topic/session/" + sessionId;
        messagingTemplate.convertAndSend(destination, (Object) payload);
    }

    // =============================================
    // BROADCAST SESSION END
    // =============================================
    public void broadcastSessionEnd(String sessionId, Map<String, Object> payload) {
        String destination = "/topic/session/" + sessionId;
        messagingTemplate.convertAndSend(destination, (Object) payload);
    }

    // =============================================
    // BROADCAST SESSION STATUS UPDATE
    // =============================================
    public void broadcastSessionStatusUpdate(String sessionId, String status) {
        Map<String, Object> payload = Map.of(
            "type", "SESSION_STATUS_UPDATE",
            "sessionId", sessionId,
            "status", status
        );
        String destination = "/topic/session/" + sessionId;
        messagingTemplate.convertAndSend(destination, (Object) payload);
    }

    // =============================================
    // INNER CLASS: SIGNALING MESSAGE
    // =============================================
    public static class SignalingMessage {
        private String type;
        private String sdp;
        private Map<String, Object> candidate;
        private Long senderId;
        private String sessionId;
        private String userName;

        public SignalingMessage() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getSdp() { return sdp; }
        public void setSdp(String sdp) { this.sdp = sdp; }

        public Map<String, Object> getCandidate() { return candidate; }
        public void setCandidate(Map<String, Object> candidate) { this.candidate = candidate; }

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}
