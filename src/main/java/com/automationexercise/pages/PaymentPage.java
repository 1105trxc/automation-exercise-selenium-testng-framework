package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PaymentPage – Page Object cho trang /payment.
 *
 * Test Cases:
 * - TC-AE-014, TC-AE-015, TC-AE-016: Place Order flows
 * - TC-AE-024: Download Invoice after purchase
 *
 * FORM FIELDS:
 * Tất cả input dùng data-qa attribute (nhất quán với các form khác trên site).
 *
 * CARD NUMBER LƯU Ý:
 * Website không thực sự xử lý payment. Card number test chuẩn:
 * 4111111111111111 (Visa test) – luôn accepted.
 *
 * HTML STRUCTURE:
 * <div id="payment-form">
 *   <input name="name_on_card"  data-qa="name-on-card"   />
 *   <input name="card_number"   data-qa="card-number"    />
 *   <input name="cvc"           data-qa="cvc"            />
 *   <input name="expiry_month"  data-qa="expiry-month"   />
 *   <input name="expiry_year"   data-qa="expiry-year"    />
 *   <button data-qa="pay-button">Pay and Confirm Order</button>
 * </div>
 */
public class PaymentPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(PaymentPage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    private static final By NAME_ON_CARD    = By.cssSelector("input[data-qa='name-on-card']");
    private static final By CARD_NUMBER     = By.cssSelector("input[data-qa='card-number']");
    private static final By CVC             = By.cssSelector("input[data-qa='cvc']");
    private static final By EXPIRY_MONTH    = By.cssSelector("input[data-qa='expiry-month']");
    private static final By EXPIRY_YEAR     = By.cssSelector("input[data-qa='expiry-year']");
    private static final By PAY_BUTTON      = By.cssSelector("button[data-qa='pay-button']");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public PaymentPage(WebDriver driver) {
        super(driver);
    }

    /** Waits until the payment form is actually displayed. */
    public PaymentPage waitUntilLoaded() {
        waitUntilVisible(NAME_ON_CARD);
        return this;
    }

    // -----------------------------------------------------------------
    // Form Actions – Fluent API
    // -----------------------------------------------------------------

    public PaymentPage enterCardName(String name) {
        log.info("Entering card name");
        type(NAME_ON_CARD, name);
        return this;
    }

    public PaymentPage enterCardNumber(String number) {
        log.info("Entering card number");
        type(CARD_NUMBER, number);
        return this;
    }

    public PaymentPage enterCVC(String cvcValue) {
        log.info("Entering CVC");
        type(CVC, cvcValue);
        return this;
    }

    public PaymentPage enterExpiryMonth(String month) {
        log.info("Entering expiry month");
        type(EXPIRY_MONTH, month);
        return this;
    }

    public PaymentPage enterExpiryYear(String year) {
        log.info("Entering expiry year");
        type(EXPIRY_YEAR, year);
        return this;
    }

    /**
     * Click "Pay and Confirm Order".
     * Returns PaymentSuccessPage vì sau khi pay thành công, navigate đến confirmation page.
     */
    public PaymentSuccessPage clickPayAndConfirm() {
        log.info("Clicking Pay and Confirm Order");
        click(PAY_BUTTON);
        return new PaymentSuccessPage(driver).waitUntilLoaded();
    }

    // -----------------------------------------------------------------
    // Convenience Method
    // -----------------------------------------------------------------

    /**
     * Fill toàn bộ payment form và submit.
     * Dùng cho test cases không cần assert từng field riêng.
     */
    public PaymentSuccessPage fillAndConfirm(String cardName, String cardNumber,
                                              String cvcValue, String month, String year) {
        return enterCardName(cardName)
                .enterCardNumber(cardNumber)
                .enterCVC(cvcValue)
                .enterExpiryMonth(month)
                .enterExpiryYear(year)
                .clickPayAndConfirm();
    }
}
