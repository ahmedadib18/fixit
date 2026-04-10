package com.fixit.fixit;

import com.fixit.fixit.entity.Helper;
import com.fixit.fixit.entity.PaymentMethod;
import com.fixit.fixit.entity.Receipt;
import com.fixit.fixit.entity.Session;
import com.fixit.fixit.entity.Transaction;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.TransactionStatus;
import com.fixit.fixit.exception.PaymentProcessingException;
import com.fixit.fixit.repository.PaymentMethodRepository;
import com.fixit.fixit.repository.ReceiptRepository;
import com.fixit.fixit.repository.TransactionRepository;
import com.fixit.fixit.service.BillingService;
import com.fixit.fixit.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private BillingService billingService;

    private Session testSession;
    private PaymentMethod testPaymentMethod;
    private User testUser;
    private Helper testHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        User helperUser = new User();
        helperUser.setFirstName("Mike");
        helperUser.setLastName("Smith");

        testHelper = new Helper();
        testHelper.setId(1L);
        testHelper.setUser(helperUser);

        testSession = new Session();
        testSession.setId("SES-TESTID1");
        testSession.setUser(testUser);
        testSession.setHelper(testHelper);
        testSession.setHelperRate(new BigDecimal("60.00"));
        testSession.setStartedAt(LocalDateTime.now().minusMinutes(30));
        testSession.setEndedAt(LocalDateTime.now());

        testPaymentMethod = new PaymentMethod();
        testPaymentMethod.setId(1L);
        testPaymentMethod.setUser(testUser);
        testPaymentMethod.setCardLastFour("4242");
        testPaymentMethod.setCardBrand("Visa");
    }

    // =============================================
    // CALCULATE NET EARNING TESTS
    // =============================================

    @Test
    void testCalculateNetEarning_10PercentFee() {
        // Arrange
        BigDecimal grossAmount = new BigDecimal("100.00");

        // Act
        BigDecimal netEarning = billingService.calculateNetEarning(grossAmount);

        // Assert
        assertEquals(new BigDecimal("90.00"), netEarning);
    }

    @Test
    void testCalculateNetEarning_SmallAmount() {
        // Arrange
        BigDecimal grossAmount = new BigDecimal("24.00");

        // Act
        BigDecimal netEarning = billingService.calculateNetEarning(grossAmount);

        // Assert
        // 24.00 - 2.40 = 21.60
        assertEquals(new BigDecimal("21.60"), netEarning);
    }

    @Test
    void testCalculateNetEarning_ZeroAmount() {
        // Arrange
        BigDecimal grossAmount = new BigDecimal("0.00");

        // Act
        BigDecimal netEarning = billingService.calculateNetEarning(grossAmount);

        // Assert
        assertEquals(new BigDecimal("0.00"), netEarning);
    }

    // =============================================
    // PROCESS REFUND TESTS
    // =============================================

    @Test
    void testProcessRefund_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setStatus(TransactionStatus.SUCCEEDED);
        transaction.setAmount(new BigDecimal("30.00"));

        Transaction refundedTransaction = new Transaction();
        refundedTransaction.setId(1L);
        refundedTransaction.setStatus(TransactionStatus.REFUNDED);
        refundedTransaction.setRefundedAt(LocalDateTime.now());
        refundedTransaction.setRefundReason("Dispute resolution");

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(refundedTransaction);

        // Act
        Transaction result = billingService.processRefund(1L, "Dispute resolution");

        // Assert
        assertEquals(TransactionStatus.REFUNDED, result.getStatus());
        assertNotNull(result.getRefundedAt());
        assertEquals("Dispute resolution", result.getRefundReason());
    }

    @Test
    void testProcessRefund_NotSucceeded_ThrowsException() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(PaymentProcessingException.class, () ->
                billingService.processRefund(1L, "Some reason")
        );
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testProcessRefund_AlreadyRefunded_ThrowsException() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setStatus(TransactionStatus.REFUNDED);

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(PaymentProcessingException.class, () ->
                billingService.processRefund(1L, "Some reason")
        );
    }

    // =============================================
    // GET TRANSACTIONS TESTS
    // =============================================

    @Test
    void testGetTransactionsByUser_Success() {
        // Arrange
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(new BigDecimal("30.00"));

        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setAmount(new BigDecimal("45.00"));

        when(transactionRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(t1, t2));

        // Act
        List<Transaction> result = billingService.getTransactionsByUser(1L);

        // Assert
        assertEquals(2, result.size());
        verify(transactionRepository).findByUserId(1L);
    }

    @Test
    void testGetHelperEarnings_Success() {
        // Arrange
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setStatus(TransactionStatus.SUCCEEDED);
        t1.setAmount(new BigDecimal("60.00"));

        when(transactionRepository.findByHelperId(1L))
                .thenReturn(List.of(t1));

        // Act
        List<Transaction> result = billingService.getHelperEarnings(1L);

        // Assert
        assertEquals(1, result.size());
        verify(transactionRepository).findByHelperId(1L);
    }

    // =============================================
    // GENERATE RECEIPT PDF TESTS
    // =============================================

    @Test
    void testGenerateReceiptPdf_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("30.00"));
        transaction.setPlatformFee(new BigDecimal("3.00"));

        Receipt receipt = new Receipt();
        receipt.setReceiptNumber("RCP-2026-0001");
        receipt.setReceiptData("Receipt data content");
        receipt.setTransaction(transaction);

        when(receiptRepository.findByTransactionId(1L))
                .thenReturn(Optional.of(receipt));

        // Act
        byte[] result = billingService.generateReceiptPdf(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // =============================================
    // GENERATE EARNINGS STATEMENT TESTS
    // =============================================

    @Test
    void testGenerateEarningsStatement_Success() {
        // Arrange
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setStatus(TransactionStatus.SUCCEEDED);
        t1.setAmount(new BigDecimal("60.00"));
        t1.setPlatformFee(new BigDecimal("6.00"));
        t1.setProcessedAt(LocalDateTime.now());

        when(transactionRepository.findByHelperId(1L))
                .thenReturn(List.of(t1));

        // Act
        byte[] result = billingService.generateEarningsStatement(1L);

        // Assert
        assertNotNull(result);
        String statementText = new String(result);
        assertTrue(statementText.contains("Earnings Statement"));
        assertTrue(statementText.contains("60.00"));
    }
}