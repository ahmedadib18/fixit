package com.fixit.fixit.repository;

import com.fixit.fixit.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByTransactionId(Long transactionId);

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    List<Receipt> findByUserId(Long userId);

    List<Receipt> findByHelperId(Long helperId);
}