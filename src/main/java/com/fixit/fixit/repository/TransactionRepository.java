package com.fixit.fixit.repository;

import com.fixit.fixit.entity.Transaction;
import com.fixit.fixit.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Get all transactions for a session
    List<Transaction> findBySessionId(String sessionId);

    // Get all transactions for a user (matches class diagram)
    @Query("SELECT t FROM Transaction t " +
            "JOIN t.session s " +
            "WHERE s.user.id = :userId " +
            "ORDER BY t.processedAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    // Get all transactions for a user via payment method
    List<Transaction> findByPaymentMethodUserId(Long userId);

    // Get all transactions for a helper (via session)
    @Query("SELECT t FROM Transaction t " +
            "JOIN t.session s " +
            "WHERE s.helper.id = :helperId")
    List<Transaction> findByHelperId(@Param("helperId") Long helperId);

    // Get transactions by status
    List<Transaction> findByStatus(TransactionStatus status);

    // Find a specific transaction for a session with a specific status
    Optional<Transaction> findBySessionIdAndStatus(String sessionId, TransactionStatus status);
}