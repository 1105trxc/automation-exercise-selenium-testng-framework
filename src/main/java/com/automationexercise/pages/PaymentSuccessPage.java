package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PaymentSuccessPage – Page Object cho trang xác nhận đặt hàng thành công (/payment_done).
 *
 * Test Cases:
 * - TC-AE-014, 015, 016: Verify the order confirmation state
 * - TC-AE-024: Click "Download Invoice" + Continue
 *
 * HTML STRUCTURE:
 * <div class="col-sm-9 col-sm-offset-1">
 *   <h2 data-qa="order-placed"><b>Order Placed!</b></h2>
 *   <p>Congratulations! Your order has been confirmed!</p>
 *   <a href="..." class="btn">Download Invoice</a>
 *   <a href="/" data-qa="continue-button">Continue</a>
 * </div>
 *
 */
public class PaymentSuccessPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessPage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    private static final By ORDER_PLACED_HEADING = By.cssSelector("h2[data-qa='order-placed']");

    private static final By ORDER_SUCCESS_MESSAGE = By.xpath(
            "//p[normalize-space()='Congratulations! Your order has been confirmed!']");

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

    public String getOrderSuccessMessage() {
        return getText(ORDER_SUCCESS_MESSAGE);
    }

    public String getOrderPlacedHeading() {
        return getText(ORDER_PLACED_HEADING);
    }

    /** Xác nhận "Download Invoice" button visible (TC-024) */
    public boolean isDownloadInvoiceVisible() {
        return isDisplayed(DOWNLOAD_INVOICE_BTN);
    }

    // -----------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------

    /** Click "Download Invoice" (TC-024). */
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
