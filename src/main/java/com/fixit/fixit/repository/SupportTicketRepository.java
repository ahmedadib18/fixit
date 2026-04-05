package com.fixit.fixit.repository;

import com.fixit.fixit.entity.SupportTicket;
import com.fixit.fixit.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserId(Long userId);

    List<SupportTicket> findByStatus(TicketStatus status);

    List<SupportTicket> findByAssignedAdminId(Long adminId);

    List<SupportTicket> findBySessionId(String sessionId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}