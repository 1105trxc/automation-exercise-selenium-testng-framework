package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    private static final By ADDRESS_NAME = By.cssSelector(".address_firstname.address_lastname");
    private static final By ADDRESS_LINES = By.cssSelector(".address_address1.address_address2");
    private static final By ADDRESS_CITY_STATE_ZIP =
            By.cssSelector(".address_city.address_state_name.address_postcode");
    private static final By ADDRESS_COUNTRY = By.cssSelector(".address_country_name");
    private static final By ADDRESS_PHONE = By.cssSelector(".address_phone");

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

    // -----------------------------------------------------------------
    // Address Getters (for TC-023 address verification)
    // -----------------------------------------------------------------

    public AddressSnapshot getDeliveryAddress() {
        return getAddressSnapshot(DELIVERY_ADDRESS_BLOCK);
    }

    public AddressSnapshot getBillingAddress() {
        return getAddressSnapshot(BILLING_ADDRESS_BLOCK);
    }

    private AddressSnapshot getAddressSnapshot(By blockLocator) {
        WebElement block = waitUntilVisible(blockLocator);
        java.util.List<WebElement> addressLines = block.findElements(ADDRESS_LINES);
        if (addressLines.size() < 3) {
            throw new IllegalStateException(
                    "Expected company, address1 and address2 in checkout address block, found "
                            + addressLines.size());
        }

        return new AddressSnapshot(
                normalizedText(block.findElement(ADDRESS_NAME)),
                normalizedText(addressLines.get(0)),
                normalizedText(addressLines.get(1)),
                normalizedText(addressLines.get(2)),
                normalizedText(block.findElement(ADDRESS_CITY_STATE_ZIP)),
                normalizedText(block.findElement(ADDRESS_COUNTRY)),
                normalizedText(block.findElement(ADDRESS_PHONE))
        );
    }

    private String normalizedText(WebElement element) {
        return element.getText().trim().replaceAll("\\s+", " ");
    }

    public record AddressSnapshot(
            String fullName,
            String company,
            String address1,
            String address2,
            String cityStateZip,
            String country,
            String mobile) {}

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
