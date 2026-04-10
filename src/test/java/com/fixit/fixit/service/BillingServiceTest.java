package com.fixit.fixit.service;

import com.fixit.fixit.entity.*;
import com.fixit.fixit.enums.TransactionStatus;
import com.fixit.fixit.exception.PaymentProcessingException;
import com.fixit.fixit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        User helperUser = new User();
        helperUser.setId(2L);
        helperUser.setFirstName("Helper");
        helperUser.setLastName("User");

        Helper helper = new Helper();
        helper.setId(1L);
        helper.setUser(helperUser);

        testSession = new Session();
        testSession.setId("SES-12345678");
        testSession.setUser(user);
        testSession.setHelper(helper);
        testSession.setHelperRate(new BigDecimal("50.00"));
        testSession.setStartedAt(LocalDateTime.now().minusHours(1));
        testSession.setEndedAt(LocalDateTime.now());

        testPaymentMethod = new PaymentMethod();
        testPaymentMethod.setId(1L);
        testPaymentMethod.setUser(user);
        testPaymentMethod.setStripePaymentMethodId("pm_123");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setSession(testSession);
        testTransaction.setPaymentMethod(testPaymentMethod);
        testTransaction.setAmount(new BigDecimal("50.00"));
        testTransaction.setPlatformFee(new BigDecimal("5.00"));
        testTransaction.setStatus(TransactionStatus.SUCCEEDED);
    }

    @Test
    void processBilling_Success() {
        when(sessionService.findById("SES-12345678")).thenReturn(testSession);
        when(sessionService.getSessionDurationMinutes("SES-12345678")).thenReturn(60L);
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(testPaymentMethod));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(receiptRepository.save(any(Receipt.class))).thenReturn(new Receipt());

        Transaction result = billingService.processBilling("SES-12345678", 1L);

        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @Test
    void calculateNetEarning_CorrectCalculation() {
        BigDecimal grossAmount = new BigDecimal("100.00");

        BigDecimal netEarning = billingService.calculateNetEarning(grossAmount);

        assertEquals(new BigDecimal("90.00"), netEarning);
    }

    @Test
    void processRefund_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = billingService.processRefund(1L, "Customer request");

        assertNotNull(result);
        assertEquals(TransactionStatus.REFUNDED, result.getStatus());
        assertNotNull(result.getRefundedAt());
        assertEquals("Customer request", result.getRefundReason());
    }

    @Test
    void processRefund_InvalidStatus_ThrowsException() {
        testTransaction.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(PaymentProcessingException.class, () ->
                billingService.processRefund(1L, "Customer request")
        );
    }

    @Test
    void generateReceiptPdf_Success() {
        Receipt receipt = new Receipt();
        receipt.setReceiptData("Test receipt data");
        when(receiptRepository.findByTransactionId(1L)).thenReturn(Optional.of(receipt));

        byte[] result = billingService.generateReceiptPdf(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
