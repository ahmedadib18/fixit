package com.fixit.fixit.controller;

import com.fixit.fixit.entity.Dispute;
import com.fixit.fixit.entity.SupportTicket;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.service.AdminService;
import com.fixit.fixit.service.DisputeService;
import com.fixit.fixit.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private SupportTicketService supportTicketService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusRequest request) {
        try {
            User user;
            switch (request.getAction().toUpperCase()) {
                case "SUSPEND":
                    user = adminService.suspendUser(userId, request.getReason());
                    break;
                case "BAN":
                    user = adminService.banUser(userId, request.getReason());
                    break;
                case "REACTIVATE":
                    user = adminService.reactivateUser(userId, request.getReason());
                    break;
                default:
                    return ResponseEntity.badRequest().body(new ErrorResponse("Invalid action. Use SUSPEND, BAN, or REACTIVATE", HttpStatus.BAD_REQUEST.value()));
            }
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/disputes")
    public ResponseEntity<?> getAllDisputes() {
        try {
            List<Dispute> disputes = disputeService.getAllDisputes();
            return ResponseEntity.ok(disputes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<?> resolveDispute(
            @PathVariable Long disputeId,
            @RequestBody ResolveDisputeRequest request) {
        try {
            Dispute dispute = disputeService.resolveDispute(
                    disputeId,
                    request.getResolution(),
                    request.getRefundAmount()
            );
            return ResponseEntity.ok(dispute);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/disputes/{disputeId}/dismiss")
    public ResponseEntity<?> dismissDispute(@PathVariable Long disputeId) {
        try {
            Dispute dispute = disputeService.dismissDispute(disputeId);
            return ResponseEntity.ok(dispute);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets() {
        try {
            List<SupportTicket> tickets = supportTicketService.getAllTickets();
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/tickets/{ticketId}/response")
    public ResponseEntity<?> addTicketResponse(
            @PathVariable Long ticketId,
            @RequestBody TicketResponseRequest request) {
        try {
            SupportTicket ticket = supportTicketService.addResponse(
                    ticketId,
                    request.getResponderId(),
                    request.getResponseText()
            );
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId) {
        try {
            SupportTicket ticket = supportTicketService.closeTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/tickets/{ticketId}/escalate")
    public ResponseEntity<?> escalateTicket(@PathVariable Long ticketId) {
        try {
            SupportTicket ticket = supportTicketService.escalateTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for requests and responses
    private static class UserStatusRequest {
        private String action;
        private String reason;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    private static class ResolveDisputeRequest {
        private String resolution;
        private BigDecimal refundAmount;

        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }

        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    }

    private static class TicketResponseRequest {
        private Long responderId;
        private String responseText;

        public Long getResponderId() { return responderId; }
        public void setResponderId(Long responderId) { this.responderId = responderId; }

        public String getResponseText() { return responseText; }
        public void setResponseText(String responseText) { this.responseText = responseText; }
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
