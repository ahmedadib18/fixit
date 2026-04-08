package com.fixit.fixit.controller;

import com.fixit.fixit.entity.Receipt;
import com.fixit.fixit.entity.Transaction;
import com.fixit.fixit.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId) {
        try {
            List<Transaction> transactions = billingService.getTransactionsByUser(userId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processBilling(@RequestBody BillingRequest request) {
        try {
            Transaction transaction = billingService.processBilling(
                    request.getSessionId(),
                    request.getPaymentMethodId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/receipts/{transactionId}")
    public ResponseEntity<?> getReceipt(@PathVariable Long transactionId) {
        try {
            Receipt receipt = billingService.getReceipt(transactionId);
            return ResponseEntity.ok(receipt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/receipts/{transactionId}/download")
    public ResponseEntity<?> downloadReceipt(@PathVariable Long transactionId) {
        try {
            byte[] pdfData = billingService.generateReceiptPdf(transactionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt-" + transactionId + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/earnings/{helperId}")
    public ResponseEntity<?> getHelperEarnings(@PathVariable Long helperId) {
        try {
            List<Transaction> earnings = billingService.getHelperEarnings(helperId);
            return ResponseEntity.ok(earnings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/earnings/{transactionId}/detail")
    public ResponseEntity<?> getEarningDetail(@PathVariable Long transactionId) {
        try {
            Transaction transaction = billingService.getEarningDetail(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/earnings/{helperId}/statement")
    public ResponseEntity<?> downloadEarningsStatement(@PathVariable Long helperId) {
        try {
            byte[] statementData = billingService.generateEarningsStatement(helperId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "earnings-statement-" + helperId + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(statementData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for requests and responses
    private static class BillingRequest {
        private String sessionId;
        private Long paymentMethodId;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public Long getPaymentMethodId() { return paymentMethodId; }
        public void setPaymentMethodId(Long paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    }

    private static class ErrorResponse {
        private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
    }
}
