package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PaymentSuccessPage – Page Object cho trang xác nhận đặt hàng thành công (/payment_done).
 *
 * Test Cases:
 * - TC-AE-014, 015, 016: Verify "Your order has been placed successfully!"
 * - TC-AE-024: Click "Download Invoice" + Continue
 *
 * HTML STRUCTURE:
 * <div class="col-sm-9 col-sm-offset-1">
 *   <h2 data-qa="order-placed">
 *     <b>Congratulations! Your order has been confirmed!</b>
 *   </h2>
 *   <p>Congratulations! Your order has been placed successfully!
 *      You order id is...
 *   </p>
 *   <a href="..." class="btn">Download Invoice</a>
 *   <a href="/" data-qa="continue-button">Continue</a>
 * </div>
 *
 * NOTE: Website hiện có 2 text messages khác nhau:
 * - h2: "Congratulations! Your order has been confirmed!"
 * - p:  "Congratulations! Your order has been placed successfully!"
 * Test case gốc verify "placed successfully" → dùng p tag.
 * Tuy nhiên, h2 với data-qa="order-placed" là selector ổn định hơn.
 * Chúng ta verify h2 visible để confirm order placed, vì data-qa ổn định hơn.
 */
public class PaymentSuccessPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessPage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    /**
     * "Order Placed" confirmation heading.
     * data-qa="order-placed" là selector ổn định nhất, không phụ thuộc text.
     */
    private static final By ORDER_PLACED_HEADING = By.cssSelector("h2[data-qa='order-placed']");

    /**
     * "Download Invoice" button (chỉ có ở TC-024).
     */
    private static final By DOWNLOAD_INVOICE_BTN = By.xpath("//a[contains(text(),'Download Invoice')]");

    /**
     * "Continue" button sau khi đặt hàng xong.
     */
    private static final By CONTINUE_BTN = By.cssSelector("a[data-qa='continue-button']");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public PaymentSuccessPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /**
     * Xác nhận order đã được đặt thành công.
     *
     * Test case step: "Verify success message 'Your order has been placed successfully!'"
     * Chúng ta verify bằng heading h2[data-qa='order-placed'] vì ổn định hơn text match.
     */
    public boolean isOrderPlacedSuccessfully() {
        return isDisplayed(ORDER_PLACED_HEADING, 15);
    }

    /** Xác nhận "Download Invoice" button visible (TC-024) */
    public boolean isDownloadInvoiceVisible() {
        return isDisplayed(DOWNLOAD_INVOICE_BTN, 5);
    }

    // -----------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------

    /**
     * Click "Download Invoice" (TC-024).
     * NOTE: Chỉ verify nút click được; không verify file thực tế download
     * vì điều đó phụ thuộc vào browser config / OS – nằm ngoài scope UI automation.
     */
    public PaymentSuccessPage clickDownloadInvoice() {
        log.info("Clicking Download Invoice");
        click(DOWNLOAD_INVOICE_BTN);
        return this;
    }

    /**
     * Click "Continue" sau khi order thành công.
     * Navigate về trang chủ.
     */
    public HomePage clickContinue() {
        log.info("Clicking Continue on order success page");
        click(CONTINUE_BTN);
        return new HomePage(driver);
    }
}
