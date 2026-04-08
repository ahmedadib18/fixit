package com.fixit.fixit.service;

import com.fixit.fixit.entity.PaymentMethod;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.repository.PaymentMethodRepository;
import com.fixit.fixit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // GET PAYMENT METHODS
    // =============================================
    public List<PaymentMethod> getPaymentMethods(Long userId) {
        return paymentMethodRepository.findByUserId(userId);
    }

    // =============================================
    // ADD PAYMENT METHOD
    // =============================================
    public PaymentMethod addPaymentMethod(Long userId,
                                          String stripePaymentMethodId,
                                          String cardLastFour,
                                          String cardBrand) {

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        // Check if this is the first payment method
        List<PaymentMethod> existing = paymentMethodRepository
                .findByUserId(userId);
        boolean isDefault = existing.isEmpty();

        // Create payment method
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setUser(user);
        paymentMethod.setStripePaymentMethodId(stripePaymentMethodId);
        paymentMethod.setCardLastFour(cardLastFour);
        paymentMethod.setCardBrand(cardBrand);
        paymentMethod.setIsDefault(isDefault);

        return paymentMethodRepository.save(paymentMethod);
    }

    // =============================================
    // DELETE PAYMENT METHOD
    // =============================================
    @Transactional
    public void deletePaymentMethod(Long paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment method not found: " + paymentMethodId));

        // If deleting default, set another as default
        if (paymentMethod.getIsDefault()) {
            List<PaymentMethod> others = paymentMethodRepository
                    .findByUserId(paymentMethod.getUser().getId());
            others.stream()
                    .filter(pm -> !pm.getId().equals(paymentMethodId))
                    .findFirst()
                    .ifPresent(pm -> {
                        pm.setIsDefault(true);
                        paymentMethodRepository.save(pm);
                    });
        }

        paymentMethodRepository.deleteById(paymentMethodId);
    }

    // =============================================
    // SET DEFAULT PAYMENT METHOD
    // =============================================
    @Transactional
    public PaymentMethod setDefaultPaymentMethod(Long userId,
                                                 Long paymentMethodId) {

        // Remove default from all existing
        List<PaymentMethod> allMethods = paymentMethodRepository
                .findByUserId(userId);
        allMethods.forEach(pm -> {
            pm.setIsDefault(false);
            paymentMethodRepository.save(pm);
        });

        // Set new default
        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment method not found: " + paymentMethodId));
        paymentMethod.setIsDefault(true);
        return paymentMethodRepository.save(paymentMethod);
    }

    // =============================================
    // GET DEFAULT PAYMENT METHOD
    // =============================================
    public PaymentMethod getDefaultPaymentMethod(Long userId) {
        return paymentMethodRepository
                .findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException(
                        "No default payment method found for user: " + userId));
    }

    // =============================================
    // GET PAYMENT METHOD BY ID
    // =============================================
    public PaymentMethod getPaymentMethodById(Long paymentMethodId) {
        return paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment method not found: " + paymentMethodId));
    }
}

