package com.fixit.fixit.repository;

import com.fixit.fixit.entity.Session;
import com.fixit.fixit.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Session> findByHelperIdOrderByCreatedAtDesc(Long helperId);

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByUserIdAndStatus(Long userId, SessionStatus status);

    List<Session> findByHelperIdAndStatus(Long helperId, SessionStatus status);
}