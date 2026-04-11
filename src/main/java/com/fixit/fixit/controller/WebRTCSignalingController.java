package com.fixit.fixit.controller;

import com.fixit.fixit.service.WebRTCSignalingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebRTCSignalingController {

    @Autowired
    private WebRTCSignalingService signalingService;

    // =============================================
    // HANDLE OFFER
    // =============================================
    @MessageMapping("/session/{sessionId}/offer")
    public void handleOffer(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        String sdp = (String) payload.get("sdp");
        Long senderId = ((Number) payload.get("senderId")).longValue();
        
        signalingService.sendOffer(sessionId, sdp, senderId);
    }

    // =============================================
    // HANDLE ANSWER
    // =============================================
    @MessageMapping("/session/{sessionId}/answer")
    public void handleAnswer(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        String sdp = (String) payload.get("sdp");
        Long senderId = ((Number) payload.get("senderId")).longValue();
        
        signalingService.sendAnswer(sessionId, sdp, senderId);
    }

    // =============================================
    // HANDLE ICE CANDIDATE
    // =============================================
    @MessageMapping("/session/{sessionId}/ice-candidate")
    public void handleIceCandidate(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        @SuppressWarnings("unchecked")
        Map<String, Object> candidate = (Map<String, Object>) payload.get("candidate");
        Long senderId = ((Number) payload.get("senderId")).longValue();
        
        signalingService.sendIceCandidate(sessionId, candidate, senderId);
    }

    // =============================================
    // HANDLE USER JOINED
    // =============================================
    @MessageMapping("/session/{sessionId}/join")
    public void handleUserJoined(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        Long userId = ((Number) payload.get("userId")).longValue();
        String userName = (String) payload.get("userName");
        
        signalingService.notifyUserJoined(sessionId, userId, userName);
    }

    // =============================================
    // HANDLE USER LEFT
    // =============================================
    @MessageMapping("/session/{sessionId}/leave")
    public void handleUserLeft(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        Long userId = ((Number) payload.get("userId")).longValue();
        
        signalingService.notifyUserLeft(sessionId, userId);
    }

    // =============================================
    // HANDLE CHAT MESSAGE
    // =============================================
    @MessageMapping("/session/{sessionId}/chat")
    public void handleChatMessage(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        signalingService.broadcastChatMessage(sessionId, payload);
    }

    // =============================================
    // HANDLE SESSION END
    // =============================================
    @MessageMapping("/session/{sessionId}/end")
    public void handleSessionEnd(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {
        
        signalingService.broadcastSessionEnd(sessionId, payload);
    }
}
