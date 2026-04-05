package com.fixit.fixit.repository;

import com.fixit.fixit.entity.SessionChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessionChatMessageRepository extends JpaRepository<SessionChatMessage, Long> {

    List<SessionChatMessage> findBySessionIdOrderBySentAtAsc(String sessionId);
}