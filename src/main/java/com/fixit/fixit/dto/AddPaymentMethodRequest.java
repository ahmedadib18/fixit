package com.fixit.fixit.dto;

import jakarta.validation.constraints.NotBlank;

public class AddPaymentMethodRequest {

    @NotBlank(message = "Stripe payment method ID is required")
    private String stripePaymentMethodId;

    @NotBlank(message = "Card last four is required")
    private String cardLastFour;

    @NotBlank(message = "Card brand is required")
    private String cardBrand;

    // =====================
    // Getters and Setters
    // =====================

    public String getStripePaymentMethodId() { return stripePaymentMethodId; }
    public void setStripePaymentMethodId(String stripePaymentMethodId) {
        this.stripePaymentMethodId = stripePaymentMethodId;
    }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
}