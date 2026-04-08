package com.fixit.fixit.controller;

import com.fixit.fixit.dto.CreateTicketRequest;
import com.fixit.fixit.entity.SupportTicket;
import com.fixit.fixit.entity.SupportTicketResponse;
import com.fixit.fixit.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        try {
            SupportTicket ticket = supportTicketService.createTicket(
                    request.getUserId(),
                    request.getSubject(),
                    request.getDescription(),
                    request.getSessionId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTickets(@PathVariable Long userId) {
        try {
            List<SupportTicket> tickets = supportTicketService.getTicketsByUser(userId);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketDetail(@PathVariable Long ticketId) {
        try {
            SupportTicket ticket = supportTicketService.getTicketDetail(ticketId);
            List<SupportTicketResponse> responses = supportTicketService.getResponses(ticketId);
            
            TicketDetailResponse response = new TicketDetailResponse(ticket, responses);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for responses
    private static class TicketDetailResponse {
        private SupportTicket ticket;
        private List<SupportTicketResponse> responses;

        public TicketDetailResponse(SupportTicket ticket, List<SupportTicketResponse> responses) {
            this.ticket = ticket;
            this.responses = responses;
        }

        public SupportTicket getTicket() { return ticket; }
        public void setTicket(SupportTicket ticket) { this.ticket = ticket; }

        public List<SupportTicketResponse> getResponses() { return responses; }
        public void setResponses(List<SupportTicketResponse> responses) { this.responses = responses; }
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
