package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProductDetailPage – Page Object cho trang /product_details/{id}.
 *
 * Test Cases:
 * - TC-AE-008: Verify product detail page contains expected information
 * - TC-AE-013: Verify Product quantity in Cart (set quantity, add to cart)
 * - TC-AE-021: Add Review on Product
 *
 * HTML STRUCTURE của trang product detail:
 * <div class="product-information">
 *   <h2>Blue Top</h2>
 *   <p>Category: Women > Tops</p>
 *   <span><span>Rs. 500</span></span>
 *   <p>Availability: <b>In Stock</b></p>
 *   <p>Condition: <b>New</b></p>
 *   <p>Brand: <b>Polo</b></p>
 *   <input id="quantity" type="number" value="1" />
 *   <button type="button" class="cart btn btn-default">Add to cart</button>
 * </div>
 *
 * REVIEW SECTION (TC-021):
 * <div id="review-section">
 *   <h2>Write Your Review</h2>
 *   <input id="name" placeholder="Your Name" />
 *   <input id="email" placeholder="Email Address" />
 *   <textarea id="review" placeholder="Add Review Here!" />
 *   <button id="button-review" type="submit">Submit</button>
 * </div>
 */
public class ProductDetailPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(ProductDetailPage.class);

    // -----------------------------------------------------------------
    // Locators – Product Information (TC-008)
    // -----------------------------------------------------------------

    /** Tên sản phẩm */
    private static final By PRODUCT_NAME     = By.cssSelector("div.product-information h2");

    /** Category text (e.g. "Category: Women > Tops") */
    private static final By PRODUCT_CATEGORY = By.xpath(
            "//div[@class='product-information']//p[contains(text(),'Category')]");

    /** Giá sản phẩm */
    private static final By PRODUCT_PRICE    = By.cssSelector("div.product-information span span");

    /** Availability text */
    private static final By AVAILABILITY     = By.xpath(
            "//div[@class='product-information']//p[contains(.,'Availability')]");

    /** Condition text */
    private static final By CONDITION        = By.xpath(
            "//div[@class='product-information']//p[contains(.,'Condition')]");

    /** Brand text */
    private static final By BRAND            = By.xpath(
            "//div[@class='product-information']//p[contains(.,'Brand')]");

    // -----------------------------------------------------------------
    // Locators – Add to Cart with Quantity (TC-013)
    // -----------------------------------------------------------------

    /** Quantity input field */
    private static final By QUANTITY_INPUT   = By.id("quantity");

    /** "Add to cart" button */
    private static final By ADD_TO_CART_BTN  = By.cssSelector(".product-information .cart");

    // -----------------------------------------------------------------
    // Locators – Review Section (TC-021)
    // -----------------------------------------------------------------

    /** "Write Your Review" section heading */
    private static final By WRITE_REVIEW_HEADING = By.xpath("//a[normalize-space()='Write Your Review']");

    /** Reviewer name input */
    private static final By REVIEW_NAME     = By.id("name");

    /** Reviewer email input */
    private static final By REVIEW_EMAIL    = By.id("email");

    /** Review text textarea */
    private static final By REVIEW_TEXT     = By.id("review");

    /** Submit review button */
    private static final By SUBMIT_REVIEW   = By.id("button-review");

    /** Success message sau khi submit review */
    private static final By REVIEW_SUCCESS  = By.xpath("//div[contains(@class,'alert-success')]");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    /** Waits until the product-detail identity element is visible. */
    public ProductDetailPage waitUntilLoaded() {
        waitUntilVisible(PRODUCT_NAME);
        return this;
    }

    // -----------------------------------------------------------------
    // State Verification – Product Info (TC-008)
    // -----------------------------------------------------------------

    public boolean isProductDetailVisible()  { return isDisplayed(PRODUCT_NAME, 10); }
    public boolean isCategoryVisible()       { return isDisplayed(PRODUCT_CATEGORY, 5); }
    public boolean isPriceVisible()          { return isDisplayed(PRODUCT_PRICE, 5); }
    public boolean isAvailabilityVisible()   { return isDisplayed(AVAILABILITY, 5); }
    public boolean isConditionVisible()      { return isDisplayed(CONDITION, 5); }
    public boolean isBrandVisible()          { return isDisplayed(BRAND, 5); }

    // -----------------------------------------------------------------
    // Data Getters – Product Info
    // -----------------------------------------------------------------

    public String getProductName()  { return getText(PRODUCT_NAME); }
    public String getCategory()     { return getText(PRODUCT_CATEGORY); }
    public String getPrice()        { return getText(PRODUCT_PRICE); }
    public String getAvailability() { return getText(AVAILABILITY); }
    public String getCondition()    { return getText(CONDITION); }
    public String getBrand()        { return getText(BRAND); }

    // -----------------------------------------------------------------
    // Add to Cart with Quantity (TC-013)
    // -----------------------------------------------------------------

    /**
     * Đặt số lượng sản phẩm trước khi add to cart.
     *
     * Implementation: clear() rồi sendKeys() thay vì triple-click + type
     * vì input có type="number" và site không có validation JS phức tạp.
     *
     * @param quantity Số lượng cần đặt (phải là số dương)
     */
    public ProductDetailPage setQuantity(int quantity) {
        log.info("Setting quantity to {}", quantity);
        WebElement input = waitUntilVisible(QUANTITY_INPUT);
        input.clear();
        input.sendKeys(String.valueOf(quantity));
        return this;
    }

    /**
     * Click "Add to cart" button trên product detail page.
     * Returns this (ProductDetailPage) vì modal mở ra overlay trang hiện tại.
     */
    public ProductDetailPage clickAddToCart() {
        log.info("Clicking Add to Cart on product detail page");
        click(ADD_TO_CART_BTN);
        return this;
    }

    // -----------------------------------------------------------------
    // Review Actions (TC-021)
    // -----------------------------------------------------------------

    /** Xác nhận "Write Your Review" section visible */
    public boolean isWriteReviewVisible() {
        return isDisplayed(WRITE_REVIEW_HEADING, 5);
    }

    /** Nhập tên reviewer */
    public ProductDetailPage enterReviewName(String name) {
        log.info("Entering review name: {}", name);
        type(REVIEW_NAME, name);
        return this;
    }

    /** Nhập email reviewer */
    public ProductDetailPage enterReviewEmail(String email) {
        log.info("Entering review email");
        type(REVIEW_EMAIL, email);
        return this;
    }

    /** Nhập nội dung review */
    public ProductDetailPage enterReviewText(String reviewText) {
        log.info("Entering review text ({} chars)", reviewText.length());
        type(REVIEW_TEXT, reviewText);
        return this;
    }

    /** Click Submit review button */
    public ProductDetailPage clickSubmitReview() {
        log.info("Submitting review");
        click(SUBMIT_REVIEW);
        return this;
    }

    /** Xác nhận success message "Thank you for your review." */
    public boolean isReviewSuccessVisible() {
        return isDisplayed(REVIEW_SUCCESS, 5);
    }
}
