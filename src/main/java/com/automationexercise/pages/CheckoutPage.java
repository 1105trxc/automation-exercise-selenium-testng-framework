package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CheckoutPage – Page Object cho trang /checkout.
 *
 * Test Cases:
 * - TC-AE-014: Verify address + place order (Register while Checkout)
 * - TC-AE-015: Verify address + place order (Register before Checkout)
 * - TC-AE-016: Verify address + place order (Login before Checkout)
 * - TC-AE-023: Verify delivery and billing address match registration data
 * - TC-AE-024: Download Invoice (full order flow)
 *
 * TRANG NÀY GỒM 2 SECTION:
 *
 * 1. "Your Delivery Address" (id="address_delivery"):
 *    Địa chỉ giao hàng – lấy từ thông tin đăng ký tài khoản.
 *
 * 2. "Review Your Order" (tbody với danh sách sản phẩm)
 *
 * 3. Comment textarea + "Place Order" button
 *
 * HTML STRUCTURE của address block:
 * <ul id="address_delivery">
 *   <li class="address_firstname address_lastname"> John  Doe </li>
 *   <li class="address_address1 address_address2">  123 QA Street </li>
 *   <li class="address_city address_state_name address_postcode"> Los Angeles California 90001 </li>
 *   <li class="address_country_name"> United States </li>
 *   <li class="address_phone"> 5551234567 </li>
 * </ul>
 */
public class CheckoutPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(CheckoutPage.class);

    // -----------------------------------------------------------------
    // Locators – Address sections
    // -----------------------------------------------------------------

    /** Delivery address block */
    private static final By DELIVERY_ADDRESS_BLOCK   = By.id("address_delivery");

    /** Billing address block */
    private static final By BILLING_ADDRESS_BLOCK    = By.id("address_invoice");

    /** Full name trong delivery address */
    private static final By DELIVERY_NAME = By.cssSelector(
            "#address_delivery .address_firstname.address_lastname");

    /** Street address trong delivery (Address 1) - luôn là thẻ li thứ 4 */
    private static final By DELIVERY_ADDRESS = By.cssSelector(
            "#address_delivery li:nth-child(4)");

    /** City, State, Zip trong delivery */
    private static final By DELIVERY_CITY_STATE_ZIP = By.cssSelector(
            "#address_delivery .address_city.address_state_name.address_postcode");

    /** Country trong delivery */
    private static final By DELIVERY_COUNTRY = By.cssSelector(
            "#address_delivery .address_country_name");

    /** Full name trong billing address */
    private static final By BILLING_NAME = By.cssSelector(
            "#address_invoice .address_firstname.address_lastname");

    /** Street address trong billing (Address 1) - luôn là thẻ li thứ 4 */
    private static final By BILLING_ADDRESS = By.cssSelector(
            "#address_invoice li:nth-child(4)");

    // -----------------------------------------------------------------
    // Locators – Order actions
    // -----------------------------------------------------------------

    /** Comment textarea */
    private static final By ORDER_COMMENT_TEXTAREA = By.name("message");

    /** "Place Order" button */
    private static final By PLACE_ORDER_BTN = By.cssSelector(".btn.btn-default.check_out");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /**
     * Xác nhận delivery address section visible.
     * Dùng để verify trang checkout đã load.
     */
    public boolean isDeliveryAddressVisible() {
        return isDisplayed(DELIVERY_ADDRESS_BLOCK, 10);
    }

    /**
     * Xác nhận billing address section visible.
     */
    public boolean isBillingAddressVisible() {
        return isDisplayed(BILLING_ADDRESS_BLOCK, 5);
    }

    // -----------------------------------------------------------------
    // Address Getters (for TC-023 address verification)
    // -----------------------------------------------------------------

    /**
     * Trả về tên đầy đủ trong delivery address.
     * Format: "Mr John Doe" hoặc "Mrs Jane Smith"
     */
    public String getDeliveryFullName() {
        return getText(DELIVERY_NAME);
    }

    /**
     * Trả về địa chỉ đường phố trong delivery address.
     */
    public String getDeliveryStreetAddress() {
        return getText(DELIVERY_ADDRESS);
    }

    /**
     * Trả về "City State Postcode" từ delivery address.
     */
    public String getDeliveryCityStateZip() {
        return getText(DELIVERY_CITY_STATE_ZIP);
    }

    /**
     * Trả về quốc gia trong delivery address.
     */
    public String getDeliveryCountry() {
        return getText(DELIVERY_COUNTRY);
    }

    /**
     * Trả về tên đầy đủ trong billing address.
     */
    public String getBillingFullName() {
        return getText(BILLING_NAME);
    }

    /**
     * Trả về địa chỉ đường phố trong billing address.
     */
    public String getBillingStreetAddress() {
        return getText(BILLING_ADDRESS);
    }

    // -----------------------------------------------------------------
    // Order Actions
    // -----------------------------------------------------------------

    /**
     * Nhập comment vào textarea (step "Enter description in comment text area").
     */
    public CheckoutPage enterOrderComment(String comment) {
        log.info("Entering order comment");
        type(ORDER_COMMENT_TEXTAREA, comment);
        return this;
    }

    /**
     * Click "Place Order" → navigate đến PaymentPage.
     */
    public PaymentPage clickPlaceOrder() {
        log.info("Clicking Place Order");
        click(PLACE_ORDER_BTN);
        return new PaymentPage(driver);
    }
}
