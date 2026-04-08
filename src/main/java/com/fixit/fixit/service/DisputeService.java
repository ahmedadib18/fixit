package com.fixit.fixit.service;

import com.fixit.fixit.entity.Dispute;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.Transaction;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.DisputeStatus;
import com.fixit.fixit.enums.DisputeType;
import com.fixit.fixit.exception.InvalidOperationException;
import com.fixit.fixit.repository.DisputeRepository;
import com.fixit.fixit.repository.SessionRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fixit.fixit.repository.TransactionRepository;

@Service
public class DisputeService {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillingService billingService;

    @Autowired
    private TransactionRepository transactionRepository;

    // =============================================
    // FILE DISPUTE
    // =============================================
    public Dispute fileDispute(String sessionId,
                               Long complainantId,
                               Long respondentId,
                               DisputeType disputeType,
                               BigDecimal amount,
                               String description) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException(
                        "Session not found: " + sessionId));

        User complainant = userRepository.findById(complainantId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + complainantId));

        User respondent = userRepository.findById(respondentId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + respondentId));

        Dispute dispute = new Dispute();
        dispute.setSession(session);
        dispute.setComplainant(complainant);
        dispute.setRespondent(respondent);
        dispute.setDisputeType(disputeType);
        dispute.setAmount(amount);
        dispute.setDescription(description);
        dispute.setStatus(DisputeStatus.OPEN);

        return disputeRepository.save(dispute);
    }

    // =============================================
    // GET ALL DISPUTES
    // =============================================
    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAllByOrderByCreatedAtDesc();
    }

    // =============================================
    // GET DISPUTE BY ID
    // =============================================
    public Dispute getDisputeById(Long disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException(
                        "Dispute not found: " + disputeId));
    }

    // =============================================
    // RESOLVE DISPUTE
    // =============================================
    @Transactional
    public Dispute resolveDispute(Long disputeId,
                                  String resolution,
                                  BigDecimal refundAmount) {

        Dispute dispute = getDisputeById(disputeId);

        // Process refund if needed
        if (refundAmount != null &&
                refundAmount.compareTo(BigDecimal.ZERO) > 0) {

            // Find the transaction for this session
            transactionRepository
                    .findBySessionId(dispute.getSession().getId())
                    .stream()
                    .findFirst()
                    .ifPresent(t -> billingService
                            .processRefund(t.getId(), resolution));

            dispute.setRefundAmount(refundAmount);
        }

        dispute.setResolution(resolution);
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolvedAt(LocalDateTime.now());
        return disputeRepository.save(dispute);
    }

    // =============================================
    // DISMISS DISPUTE
    // =============================================
    public Dispute dismissDispute(Long disputeId) {
        Dispute dispute = getDisputeById(disputeId);

        if (dispute.getStatus() == DisputeStatus.RESOLVED) {
            throw new InvalidOperationException("Cannot dismiss an already resolved dispute");
        }

        dispute.setStatus(DisputeStatus.DISMISSED);
        dispute.setResolvedAt(LocalDateTime.now());
        return disputeRepository.save(dispute);
    }

    // =============================================
    // SET UNDER REVIEW
    // =============================================
    public Dispute setUnderReview(Long disputeId, Long adminId) {
        Dispute dispute = getDisputeById(disputeId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException(
                        "Admin not found: " + adminId));

        dispute.setStatus(DisputeStatus.UNDER_REVIEW);
        dispute.setAssignedAdmin(admin);
        return disputeRepository.save(dispute);
    }

    // =============================================
    // GET DISPUTES BY STATUS
    // =============================================
    public List<Dispute> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status);
    }

    // =============================================
    // GET DISPUTES BY SESSION
    // =============================================
    public List<Dispute> getDisputesBySession(String sessionId) {
        return disputeRepository.findBySessionId(sessionId);
    }
}