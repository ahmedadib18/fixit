package com.fixit.fixit.repository;

import com.fixit.fixit.entity.SupportTicketResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketResponseRepository extends JpaRepository<SupportTicketResponse, Long> {

    List<SupportTicketResponse> findByTicketId(Long ticketId);

    List<SupportTicketResponse> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}