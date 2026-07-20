package com.automationexercise.flows;

import com.automationexercise.models.PaymentData;
import com.automationexercise.pages.CheckoutPage;
import com.automationexercise.pages.PaymentSuccessPage;

/**
 * CheckoutFlow - Encapsulates workflows related to completing an order checkout.
 * 
 * Note: Flows perform the steps but DO NOT contain business assertions.
 */
public class CheckoutFlow {

    /**
     * Completes the payment process using the provided PaymentData.
     * 
     * @param checkoutPage The CheckoutPage already navigated to.
     * @param payment The PaymentData model containing card details.
     * @return PaymentSuccessPage after payment is submitted.
     */
    public PaymentSuccessPage completePayment(CheckoutPage checkoutPage, PaymentData payment) {
        return checkoutPage
                .enterOrderComment(payment.getOrderComment())
                .clickPlaceOrder()
                .fillAndConfirm(
                        payment.getNameOnCard(),
                        payment.getCardNumber(),
                        payment.getCvc(),
                        payment.getExpiryMonth(),
                        payment.getExpiryYear()
                );
    }
}
