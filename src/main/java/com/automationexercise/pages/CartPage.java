package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
 *
 * MODAL AFTER ADD TO CART:
 * Khi click "Add to cart" từ ProductsPage, website hiện modal với 2 options:
 * - "Continue Shopping" → đóng modal, ở lại ProductsPage
 * - "View Cart" → navigate đến CartPage
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

    /** "Continue Shopping" button trong modal sau Add to Cart */
    private static final By CONTINUE_SHOPPING_BTN = By.cssSelector("button.close-modal");

    /** "View Cart" link trong modal sau Add to Cart */
    private static final By VIEW_CART_MODAL_LINK = By.cssSelector(".modal-body a[href='/view_cart']");

    // -----------------------------------------------------------------
    // Locators – Footer Subscription (TC-011)
    // -----------------------------------------------------------------

    /** "SUBSCRIPTION" heading ở footer */
    private static final By SUBSCRIPTION_HEADING = By.xpath("//h2[normalize-space()='Subscription']");

    /** Email input trong subscription form */
    private static final By SUBSCRIBE_EMAIL_INPUT = By.id("susbscribe_email");

    /** Arrow button để submit subscription */
    private static final By SUBSCRIBE_BUTTON = By.id("subscribe");

    /** Success message sau khi subscribe */
    private static final By SUBSCRIBE_SUCCESS_MSG = By.cssSelector("div.alert-success");

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
        return isDisplayed(CART_HEADING, 10);
    }

    /**
     * Kiểm tra cart có trống không (không có row nào).
     */
    public boolean isCartEmpty() {
        return driver.findElements(CART_ROWS).isEmpty();
    }

    /**
     * Trả về số lượng sản phẩm (row) trong cart.
     */
    public int getProductCount() {
        return driver.findElements(CART_ROWS).size();
    }

    /**
     * Kiểm tra một sản phẩm có trong cart dựa trên tên.
     * @param productName Tên sản phẩm cần tìm (partial match)
     */
    public boolean isProductInCart(String productName) {
        return driver.findElements(CART_ROWS).stream()
                .anyMatch(row -> {
                    List<WebElement> names = row.findElements(ROW_PRODUCT_NAME);
                    return !names.isEmpty() && names.get(0).getText().contains(productName);
                });
    }

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
        return this;
    }

    /**
     * Click "Proceed To Checkout".
     *
     * NẾU đã login: navigate thẳng đến CheckoutPage.
     * NẾU chưa login: hiện modal với link "Register / Login".
     *
     * Trả về CheckoutPage cho cả 2 trường hợp.
     * Nếu cần modal → login flow, gọi clickRegisterLoginInModal() trước.
     */
    public CheckoutPage clickProceedToCheckout() {
        log.info("Clicking Proceed To Checkout");
        jsClick(PROCEED_TO_CHECKOUT_BTN);
        return new CheckoutPage(driver);
    }

    /**
     * Click "Register / Login" link trong modal checkout.
     * Xuất hiện khi click Proceed To Checkout mà chưa login.
     * Returns LoginPage vì navigate đến /login.
     */
    public LoginPage clickRegisterLoginInModal() {
        log.info("Clicking Register / Login in checkout modal");
        jsClick(REGISTER_LOGIN_LINK);
        return new LoginPage(driver);
    }

    // -----------------------------------------------------------------
    // Modal Actions (after "Add to Cart" from ProductsPage)
    // -----------------------------------------------------------------

    /**
     * Click "Continue Shopping" trong modal sau Add to Cart.
     * Đóng modal, ở lại ProductsPage.
     */
    public ProductsPage clickContinueShopping() {
        log.info("Clicking Continue Shopping in modal");
        click(CONTINUE_SHOPPING_BTN);
        return new ProductsPage(driver);
    }

    /**
     * Click "View Cart" trong modal sau Add to Cart.
     * Navigate đến CartPage.
     */
    public CartPage clickViewCartFromModal() {
        log.info("Clicking View Cart from modal");
        jsClick(VIEW_CART_MODAL_LINK);
        return this;
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

    // -----------------------------------------------------------------
    // Subscription (Footer) – TC-011
    // -----------------------------------------------------------------

    /** Xác nhận "SUBSCRIPTION" heading ở footer visible */
    public boolean isSubscriptionVisible() {
        return isDisplayed(SUBSCRIPTION_HEADING, 5);
    }

    /**
     * Nhập email vào subscription form ở footer.
     * Cần scroll xuống footer trước khi gọi method này.
     */
    public CartPage enterSubscriptionEmail(String email) {
        log.info("Entering subscription email on Cart page");
        type(SUBSCRIBE_EMAIL_INPUT, email);
        return this;
    }

    /** Click arrow button để submit subscription */
    public CartPage clickSubscribeButton() {
        log.info("Clicking subscribe button on Cart page");
        click(SUBSCRIBE_BUTTON);
        return this;
    }

    /** Xác nhận success message "You have been successfully subscribed!" */
    public boolean isSubscribeSuccessVisible() {
        return isDisplayed(SUBSCRIBE_SUCCESS_MSG, 5);
    }
}
