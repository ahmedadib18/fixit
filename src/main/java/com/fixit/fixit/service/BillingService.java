package com.fixit.fixit.service;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.Receipt;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.Transaction;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.entity.PaymentMethod;
import com.fixit.fixit.enums.TransactionStatus;
import com.fixit.fixit.exception.PaymentProcessingException;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.repository.HelperRepository;
import com.fixit.fixit.repository.PaymentMethodRepository;
import com.fixit.fixit.repository.ReceiptRepository;
import com.fixit.fixit.repository.TransactionRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
public class BillingService {

    // Platform commission fee — 10% as per wireframe 13
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.10");

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private SessionService sessionService;

    // =============================================
    // PROCESS BILLING
    // =============================================
    @Transactional
    public Transaction processBilling(String sessionId, Long paymentMethodId) {

        // Get session
        Session session = sessionService.findById(sessionId);

        // Calculate duration in minutes
        long durationMinutes = sessionService.getSessionDurationMinutes(sessionId);

        // Calculate gross amount based on helper rate and duration
        BigDecimal helperRate = session.getHelperRate();
        BigDecimal durationHours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal grossAmount = helperRate
                .multiply(durationHours)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate platform fee (10%)
        BigDecimal platformFee = grossAmount
                .multiply(PLATFORM_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Get payment method
        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setSession(session);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setAmount(grossAmount);
        transaction.setPlatformFee(platformFee);
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setProcessedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Generate receipt
        generateReceipt(savedTransaction, session);

        // Mark transaction as succeeded
        savedTransaction.setStatus(TransactionStatus.SUCCEEDED);
        return transactionRepository.save(savedTransaction);
    }

    // =============================================
    // GENERATE RECEIPT
    // =============================================
    private Receipt generateReceipt(Transaction transaction, Session session) {

        // Generate receipt number in format RCP-YYYY-XXXX
        String receiptNumber = "RCP-" + Year.now().getValue() +
                "-" + String.format("%04d", transaction.getId());

        // Build receipt data
        String receiptData = buildReceiptData(transaction, session);

        Receipt receipt = new Receipt();
        receipt.setTransaction(transaction);
        receipt.setUser(session.getUser());
        receipt.setHelper(session.getHelper());
        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceiptData(receiptData);

        return receiptRepository.save(receipt);
    }

    // =============================================
    // BUILD RECEIPT DATA
    // =============================================
    private String buildReceiptData(Transaction transaction, Session session) {
        long durationMinutes = sessionService
                .getSessionDurationMinutes(session.getId());

        return "Receipt\n" +
                "Session: " + session.getId() + "\n" +
                "Helper: " + session.getHelper().getUser().getFirstName() +
                " " + session.getHelper().getUser().getLastName() + "\n" +
                "Duration: " + durationMinutes + " minutes\n" +
                "Subtotal: $" + transaction.getAmount() + "\n" +
                "Platform Fee: $" + transaction.getPlatformFee() + "\n" +
                "Total: $" + transaction.getAmount()
                .add(transaction.getPlatformFee()) + "\n" +
                "Currency: " + transaction.getCurrency() + "\n" +
                "Date: " + transaction.getProcessedAt();
    }

    // =============================================
    // GET RECEIPT
    // =============================================
    public Receipt getReceipt(Long transactionId) {
        return receiptRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "transactionId", transactionId));
    }

    // =============================================
    // GET TRANSACTIONS BY USER
    // =============================================
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    // =============================================
    // GET HELPER EARNINGS
    // =============================================
    public List<Transaction> getHelperEarnings(Long helperId) {
        return transactionRepository.findByHelperId(helperId);
    }

    // =============================================
    // GET EARNING DETAIL
    // =============================================
    public Transaction getEarningDetail(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
    }

    // =============================================
    // PROCESS REFUND
    // =============================================
    @Transactional
    public Transaction processRefund(Long transactionId, String refundReason) {
        Transaction transaction = getEarningDetail(transactionId);

        if (transaction.getStatus() != TransactionStatus.SUCCEEDED) {
            throw new PaymentProcessingException("Transaction cannot be refunded. Status: " + transaction.getStatus());
        }

        transaction.setStatus(TransactionStatus.REFUNDED);
        transaction.setRefundedAt(LocalDateTime.now());
        transaction.setRefundReason(refundReason);
        return transactionRepository.save(transaction);
    }

    // =============================================
    // CALCULATE NET EARNING FOR HELPER
    // =============================================
    public BigDecimal calculateNetEarning(BigDecimal grossAmount) {
        BigDecimal platformFee = grossAmount
                .multiply(PLATFORM_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        return grossAmount.subtract(platformFee);
    }

    // =============================================
    // PROCESS SESSION PAYMENT (AUTO)
    // =============================================
    @Transactional
    public Transaction processSessionPayment(String sessionId) {
        // Get session
        Session session = sessionService.findById(sessionId);

        // Calculate duration in minutes
        long durationMinutes = sessionService.getSessionDurationMinutes(sessionId);
        
        if (durationMinutes <= 0) {
            System.out.println("Session duration is 0, skipping payment");
            return null;
        }

        // Calculate gross amount based on helper rate and duration
        BigDecimal helperRate = session.getHelperRate();
        if (helperRate == null || helperRate.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("No helper rate set, skipping payment");
            return null;
        }

        BigDecimal durationHours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal grossAmount = helperRate
                .multiply(durationHours)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate platform fee (10%)
        BigDecimal platformFee = grossAmount
                .multiply(PLATFORM_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Create transaction without payment method (will be processed later)
        Transaction transaction = new Transaction();
        transaction.setSession(session);
        transaction.setPaymentMethod(null); // No payment method for now
        transaction.setAmount(grossAmount);
        transaction.setPlatformFee(platformFee);
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.SUCCEEDED); // Auto-succeed for now
        transaction.setProcessedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Generate receipt
        try {
            generateReceipt(savedTransaction, session);
        } catch (Exception e) {
            System.err.println("Failed to generate receipt: " + e.getMessage());
        }

        return savedTransaction;
    }

    // =============================================
    // GENERATE RECEIPT PDF
    // =============================================
    public byte[] generateReceiptPdf(Long transactionId) {
        Receipt receipt = getReceipt(transactionId);
        
        // For now, return receipt data as bytes
        // In production, use a PDF library like iText or Apache PDFBox
        return receipt.getReceiptData().getBytes();
    }

    // =============================================
    // GENERATE EARNINGS STATEMENT
    // =============================================
    public byte[] generateEarningsStatement(Long helperId) {
        List<Transaction> earnings = getHelperEarnings(helperId);
        
        StringBuilder statement = new StringBuilder();
        statement.append("Earnings Statement\n");
        statement.append("Helper ID: ").append(helperId).append("\n\n");
        
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        
        for (Transaction transaction : earnings) {
            if (transaction.getStatus() == TransactionStatus.SUCCEEDED) {
                statement.append("Transaction ID: ").append(transaction.getId()).append("\n");
                statement.append("Date: ").append(transaction.getProcessedAt()).append("\n");
                statement.append("Amount: $").append(transaction.getAmount()).append("\n");
                statement.append("Platform Fee: $").append(transaction.getPlatformFee()).append("\n");
                statement.append("Net: $").append(calculateNetEarning(transaction.getAmount())).append("\n\n");
                
                totalGross = totalGross.add(transaction.getAmount());
                totalFees = totalFees.add(transaction.getPlatformFee());
            }
        }
        
        statement.append("Total Gross: $").append(totalGross).append("\n");
        statement.append("Total Fees: $").append(totalFees).append("\n");
        statement.append("Total Net: $").append(totalGross.subtract(totalFees)).append("\n");
        
        // In production, use a PDF library
        return statement.toString().getBytes();
    }
}
