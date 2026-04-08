package com.fixit.fixit.service;

import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.SupportTicket;
import com.fixit.fixit.entity.SupportTicketResponse;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.TicketStatus;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.SupportTicketRepository;
import com.fixit.fixit.repository.TicketResponseRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private TicketResponseRepository ticketResponseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    // =============================================
    // CREATE TICKET
    // =============================================
    public SupportTicket createTicket(Long userId,
                                      String subject,
                                      String description,
                                      String sessionId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setStatus(TicketStatus.OPEN);

        // Link to session if provided
        if (sessionId != null && !sessionId.isEmpty()) {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException(
                            "Session not found: " + sessionId));
            ticket.setSession(session);
        }

        return supportTicketRepository.save(ticket);
    }

    // =============================================
    // GET ALL TICKETS
    // =============================================
    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAllByOrderByCreatedAtDesc();
    }

    // =============================================
    // GET TICKETS BY USER
    // =============================================
    public List<SupportTicket> getTicketsByUser(Long userId) {
        return supportTicketRepository.findByUserId(userId);
    }

    // =============================================
    // GET TICKET DETAIL
    // =============================================
    public SupportTicket getTicketDetail(Long ticketId) {
        return supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket not found: " + ticketId));
    }

    // =============================================
    // ADD RESPONSE
    // =============================================
    public SupportTicket addResponse(Long ticketId,
                                     Long responderId,
                                     String responseText) {

        SupportTicket ticket = getTicketDetail(ticketId);

        User responder = userRepository.findById(responderId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + responderId));

        SupportTicketResponse response = new SupportTicketResponse();
        response.setTicket(ticket);
        response.setResponder(responder);
        response.setResponseText(responseText);
        ticketResponseRepository.save(response);

        // Update ticket status to IN_PROGRESS
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            supportTicketRepository.save(ticket);
        }

        return ticket;
    }

    // =============================================
    // CLOSE TICKET
    // =============================================
    public SupportTicket closeTicket(Long ticketId) {
        SupportTicket ticket = getTicketDetail(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());
        return supportTicketRepository.save(ticket);
    }

    // =============================================
    // RESOLVE TICKET
    // =============================================
    public SupportTicket resolveTicket(Long ticketId) {
        SupportTicket ticket = getTicketDetail(ticketId);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        return supportTicketRepository.save(ticket);
    }

    // =============================================
    // ESCALATE TICKET
    // =============================================
    public SupportTicket escalateTicket(Long ticketId) {
        SupportTicket ticket = getTicketDetail(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return supportTicketRepository.save(ticket);
    }

    // =============================================
    // GET RESPONSES
    // =============================================
    public List<SupportTicketResponse> getResponses(Long ticketId) {
        return ticketResponseRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    // =============================================
    // ASSIGN ADMIN
    // =============================================
    public SupportTicket assignAdmin(Long ticketId, Long adminId) {
        SupportTicket ticket = getTicketDetail(ticketId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException(
                        "Admin not found: " + adminId));

        ticket.setAssignedAdmin(admin);
        return supportTicketRepository.save(ticket);
    }

    // =============================================
    // GET TICKETS BY STATUS
    // =============================================
    public List<SupportTicket> getTicketsByStatus(TicketStatus status) {
        return supportTicketRepository.findByStatus(status);
    }
}