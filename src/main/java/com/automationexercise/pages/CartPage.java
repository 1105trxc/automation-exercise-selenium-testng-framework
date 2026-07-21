package com.automationexercise.pages;

import com.automationexercise.components.AdHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CartPage – Page Object cho trang /view_cart.
 *
 * Test Cases:
 * - TC-AE-011: Verify Subscription in Cart page
 * - TC-AE-012: Add Products in Cart (verify prices/qty/total)
 * - TC-AE-013: Verify Product quantity in Cart
 * - TC-AE-014/015/016: Place Order flows (proceed to checkout)
 * - TC-AE-017: Remove Products From Cart
 * - TC-AE-020: Search Products and Verify Cart After Login
 * - TC-AE-022: Add to cart from Recommended items
 * - TC-AE-023: Verify address details in checkout page
 * - TC-AE-024: Download Invoice after purchase order
 *
 * CART TABLE STRUCTURE:
 * <table class="table table-condensed">
 *   <tbody>
 *     <tr id="product-{id}">
 *       <td class="cart_product">    ← product image
 *       <td class="cart_description"> ← name + category
 *       <td class="cart_price">      ← unit price
 *       <td class="cart_quantity">   ← quantity input
 *       <td class="cart_total">      ← total = price × qty
 *       <td class="cart_delete">     ← X remove button
 *     </tr>
 *   </tbody>
 * </table>
 */
public class CartPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(CartPage.class);

    // -----------------------------------------------------------------
    // Locators – Cart table
    // -----------------------------------------------------------------

    /** Cart section heading */
    private static final By CART_HEADING = By.xpath("//li[@class='active' and contains(text(),'Shopping Cart')]");

    /** Tất cả các row sản phẩm trong cart */
    private static final By CART_ROWS = By.cssSelector("tbody tr");

    /** Tên sản phẩm (trong row – dùng với row element context) */
    private static final By ROW_PRODUCT_NAME = By.cssSelector("td.cart_description h4 a");

    /** Unit price (trong row) */
    private static final By ROW_UNIT_PRICE = By.cssSelector("td.cart_price p");

    /** Quantity (trong row) */
    private static final By ROW_QUANTITY = By.cssSelector("td.cart_quantity button");

    /** Total price (trong row) */
    private static final By ROW_TOTAL = By.cssSelector("td.cart_total p");

    /** Remove button X (trong row) */
    private static final By ROW_DELETE = By.cssSelector("td.cart_delete a.cart_quantity_delete");

    // -----------------------------------------------------------------
    // Locators – Actions
    // -----------------------------------------------------------------

    /** "Proceed To Checkout" button ở cuối trang cart */
    private static final By PROCEED_TO_CHECKOUT_BTN = By.cssSelector(".btn.btn-default.check_out");

    /** "Register / Login" link – xuất hiện trong modal khi chưa login */
    private static final By REGISTER_LOGIN_LINK = By.cssSelector("#checkoutModal .modal-body a");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public CartPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /**
     * Xác nhận đang ở trang Shopping Cart.
     * Verify bằng breadcrumb "Shopping Cart" active item.
     */
    public boolean isCartPageVisible() {
        return isDisplayed(CART_HEADING);
    }

    /** Waits until the shopping cart breadcrumb is actually displayed. */
    public CartPage waitUntilLoaded() {
        waitUntilVisible(CART_HEADING);
        return this;
    }

    /**
     * Kiểm tra cart có trống không (không có row nào).
     */
    public boolean isCartEmpty() {
        return driver.findElements(CART_ROWS).isEmpty();
    }

    /**
     * Chờ đến khi giỏ hàng trống (thường dùng sau khi remove product).
     */
    public CartPage waitForCartEmpty() {
        log.info("Waiting for cart to become empty");
        wait.until(ExpectedConditions.numberOfElementsToBe(CART_ROWS, 0));
        return this;
    }

    /**
     * Trả về số lượng sản phẩm (row) trong cart.
     */
    public int getProductCount() {
        return driver.findElements(CART_ROWS).size();
    }

    /**
     * Returns a snapshot of all cart items for identity comparison.
     *
     * Use this instead of getProductCount() when you need to verify
     * that the exact same products (not just the same number) are in the cart.
     *
     * @return list of CartItemSnapshot, one per cart row
     */
    public List<CartItemSnapshot> getCartItems() {
        return driver.findElements(CART_ROWS).stream()
                .map(row -> new CartItemSnapshot(
                        row.findElement(ROW_PRODUCT_NAME).getText().trim(),
                        row.findElement(ROW_UNIT_PRICE).getText().trim(),
                        row.findElement(ROW_QUANTITY).getText().trim(),
                        row.findElement(ROW_TOTAL).getText().trim()
                ))
                .toList();
    }

    /**
     * Immutable snapshot of a single cart row for before/after comparison.
     *
     * @param name      product name
     * @param unitPrice unit price text (e.g. "Rs. 500")
     * @param quantity  quantity text
     * @param total     total price text
     */
    public record CartItemSnapshot(String name, String unitPrice, String quantity, String total) {}

    // -----------------------------------------------------------------
    // Row-based Getters (1-indexed, e.g. getProductNameAtRow(1))
    // -----------------------------------------------------------------

    private WebElement getRow(int rowIndex) {
        List<WebElement> rows = driver.findElements(CART_ROWS);
        if (rowIndex < 1 || rowIndex > rows.size()) {
            throw new IllegalArgumentException("Cart row " + rowIndex + " not found. Total rows: " + rows.size());
        }
        return rows.get(rowIndex - 1);
    }

    /** Tên sản phẩm tại row thứ n (1-indexed) */
    public String getProductNameAtRow(int rowIndex) {
        return getRow(rowIndex).findElement(ROW_PRODUCT_NAME).getText().trim();
    }

    /** Đơn giá tại row thứ n */
    public String getProductPriceAtRow(int rowIndex) {
        return getRow(rowIndex).findElement(ROW_UNIT_PRICE).getText().trim();
    }

    /** Số lượng tại row thứ n */
    public String getProductQuantityAtRow(int rowIndex) {
        return getRow(rowIndex).findElement(ROW_QUANTITY).getText().trim();
    }

    /** Tổng tiền tại row thứ n */
    public String getProductTotalAtRow(int rowIndex) {
        return getRow(rowIndex).findElement(ROW_TOTAL).getText().trim();
    }

    // -----------------------------------------------------------------
    // Cart Actions
    // -----------------------------------------------------------------

    /**
     * Click nút X để xóa sản phẩm tại row thứ n.
     * Sau khi click, row sẽ biến mất khỏi DOM.
     */
    public CartPage removeProductAtRow(int rowIndex) {
        log.info("Removing product at cart row {}", rowIndex);
        WebElement row = getRow(rowIndex);
        row.findElement(ROW_DELETE).click();
        AdHandler.dismissLinkTriggeredVignette(driver);
        return this;
    }

    /**
     * Click "Proceed To Checkout" khi ĐÃ ĐĂNG NHẬP.
     * Trả về CheckoutPage.
     */
    public CheckoutPage clickProceedToCheckoutLoggedIn() {
        log.info("Clicking Proceed To Checkout (Logged In)");
        clickSideEffectFreeNavigationLink(PROCEED_TO_CHECKOUT_BTN, "Proceed To Checkout");
        return new CheckoutPage(driver).waitUntilLoaded();
    }

    /**
     * Click "Proceed To Checkout" khi LÀ GUEST.
     * Mở modal "Checkout / Register". Trả về chính CartPage.
     */
    public CartPage clickProceedToCheckoutGuest() {
        log.info("Clicking Proceed To Checkout (Guest)");
        click(PROCEED_TO_CHECKOUT_BTN);
        AdHandler.dismissLinkTriggeredVignette(driver);
        // Wait for modal to appear
        waitUntilVisible(REGISTER_LOGIN_LINK);
        return this;
    }

    /**
     * Click "Register / Login" link trong modal checkout.
     * Xuất hiện khi click Proceed To Checkout mà chưa login.
     * Returns LoginPage vì navigate đến /login.
     */
    public LoginPage clickRegisterLoginInModal() {
        log.info("Clicking Register / Login in checkout modal");
        clickSideEffectFreeNavigationLink(REGISTER_LOGIN_LINK, "Register / Login from checkout");
        return new LoginPage(driver).waitUntilLoaded();
    }

    // -----------------------------------------------------------------
    // Scroll helper (public wrapper – BasePage.scrollToBottom() is protected)
    // -----------------------------------------------------------------

    /**
     * Scroll xuống bottom của trang.
     * Gọi trước khi verify subscription hoặc footer elements.
     */
    public CartPage goToBottom() {
        scrollToBottom();
        return this;
    }
}
