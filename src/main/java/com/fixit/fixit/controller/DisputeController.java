package com.fixit.fixit.controller;

import com.fixit.fixit.dto.FileDisputeRequest;
import com.fixit.fixit.entity.Dispute;
import com.fixit.fixit.service.DisputeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    @PostMapping("/sessions/{sessionId}")
    public ResponseEntity<?> fileDispute(
            @PathVariable String sessionId,
            @Valid @RequestBody FileDisputeRequest request) {
        try {
            Dispute dispute = disputeService.fileDispute(
                    sessionId,
                    request.getComplainantId(),
                    request.getRespondentId(),
                    request.getDisputeType(),
                    request.getAmount(),
                    request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionDisputes(@PathVariable String sessionId) {
        try {
            List<Dispute> disputes = disputeService.getDisputesBySession(sessionId);
            return ResponseEntity.ok(disputes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDisputes(@PathVariable Long userId) {
        try {
            List<Dispute> disputes = disputeService.getDisputesByComplainant(userId);
            return ResponseEntity.ok(disputes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner class for error responses
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
