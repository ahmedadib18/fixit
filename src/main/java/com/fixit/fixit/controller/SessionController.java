package com.fixit.fixit.controller;

import com.fixit.fixit.dto.CreateSessionRequest;
import com.fixit.fixit.dto.SendMessageRequest;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.SessionChatMessage;
import com.fixit.fixit.service.SessionLogService;
import com.fixit.fixit.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionLogService sessionLogService;

    @PostMapping
    public ResponseEntity<?> createSession(@Valid @RequestBody CreateSessionRequest request) {
        try {
            Session session = sessionService.initiateSession(
                    request.getUserId(),
                    request.getHelperId(),
                    request.getCategoryId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{sessionId}/accept")
    public ResponseEntity<?> acceptSession(@PathVariable String sessionId) {
        try {
            Session session = sessionService.acceptSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{sessionId}/reject")
    public ResponseEntity<?> rejectSession(@PathVariable String sessionId) {
        try {
            Session session = sessionService.rejectSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{sessionId}/end")
    public ResponseEntity<?> endSession(@PathVariable String sessionId) {
        try {
            Session session = sessionService.endSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{sessionId}/cancel")
    public ResponseEntity<?> cancelSession(@PathVariable String sessionId) {
        try {
            Session session = sessionService.cancelSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        try {
            SessionChatMessage message = sessionService.sendMessage(
                    sessionId,
                    request.getUserId(),
                    request.getMessageText()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{sessionId}/log")
    public ResponseEntity<?> getSessionLog(@PathVariable String sessionId) {
        try {
            Session session = sessionLogService.getSessionLog(sessionId);
            List<SessionChatMessage> messages = sessionLogService.getSessionChatLog(sessionId);
            
            SessionLogResponse response = new SessionLogResponse(session, messages);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{sessionId}/consent")
    public ResponseEntity<?> updateConsent(
            @PathVariable String sessionId,
            @RequestBody ConsentRequest request) {
        try {
            Session session;
            if (request.getUserId() != null) {
                session = sessionLogService.updateUserConsent(sessionId, request.getUserId(), request.getConsent());
            } else if (request.getHelperId() != null) {
                session = sessionLogService.updateHelperConsent(sessionId, request.getHelperId(), request.getConsent());
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse("Either userId or helperId must be provided", HttpStatus.BAD_REQUEST.value()));
            }
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{sessionId}/export")
    public ResponseEntity<?> exportSessionLog(@PathVariable String sessionId) {
        try {
            byte[] logData = sessionLogService.exportSessionLog(sessionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "session-" + sessionId + ".txt");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(logData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{sessionId}/deletion")
    public ResponseEntity<?> requestDeletion(@PathVariable String sessionId) {
        try {
            Session session = sessionLogService.requestLogDeletion(sessionId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for requests and responses
    private static class ConsentRequest {
        private Long userId;
        private Long helperId;
        private Boolean consent;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getHelperId() { return helperId; }
        public void setHelperId(Long helperId) { this.helperId = helperId; }

        public Boolean getConsent() { return consent; }
        public void setConsent(Boolean consent) { this.consent = consent; }
    }

    private static class SessionLogResponse {
        private Session session;
        private List<SessionChatMessage> messages;

        public SessionLogResponse(Session session, List<SessionChatMessage> messages) {
            this.session = session;
            this.messages = messages;
        }

        public Session getSession() { return session; }
        public void setSession(Session session) { this.session = session; }

        public List<SessionChatMessage> getMessages() { return messages; }
        public void setMessages(List<SessionChatMessage> messages) { this.messages = messages; }
    }

    private static class ErrorResponse {
        private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
    }
}
