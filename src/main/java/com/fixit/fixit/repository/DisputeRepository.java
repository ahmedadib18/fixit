package com.fixit.fixit.repository;

import com.fixit.fixit.entity.Dispute;
import com.fixit.fixit.enums.DisputeStatus;
import com.fixit.fixit.enums.DisputeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findBySessionId(String sessionId);

    List<Dispute> findByComplainantId(Long complainantId);

    List<Dispute> findByRespondentId(Long respondentId);

    List<Dispute> findByDisputeType(DisputeType disputeType);

    List<Dispute> findByAssignedAdminId(Long adminId);

    List<Dispute> findAllByOrderByCreatedAtDesc();
}