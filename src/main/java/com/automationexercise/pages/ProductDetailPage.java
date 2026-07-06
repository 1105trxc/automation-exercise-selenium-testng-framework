package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProductDetailPage – Page Object cho trang /product_details/{id}.
 *
 * Test Case: TC-AE-008 – Verify product detail page contains expected information.
 *
 * HTML STRUCTURE của trang product detail:
 * <div class="product-information">
 *   <h2>Blue Top</h2>
 *   <p>Category: Women > Tops</p>
 *   <span><span>Rs. 500</span></span>
 *   <p>Availability: <b>In Stock</b></p>
 *   <p>Condition: <b>New</b></p>
 *   <p>Brand: <b>Polo</b></p>
 * </div>
 */
public class ProductDetailPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ProductDetailPage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    /** Tên sản phẩm – tiêu đề h2 trong section product-information */
    private static final By PRODUCT_NAME     = By.cssSelector("div.product-information h2");

    /** Category text (e.g. "Category: Women > Tops") */
    private static final By PRODUCT_CATEGORY = By.xpath("//div[@class='product-information']//p[contains(text(),'Category')]");

    /** Giá sản phẩm (e.g. "Rs. 500") */
    private static final By PRODUCT_PRICE    = By.cssSelector("div.product-information span span");

    /** Availability text (e.g. "Availability: In Stock") */
    private static final By AVAILABILITY     = By.xpath("//div[@class='product-information']//p[contains(text(),'Availability')]");

    /** Condition text (e.g. "Condition: New") */
    private static final By CONDITION        = By.xpath("//div[@class='product-information']//p[contains(text(),'Condition')]");

    /** Brand text (e.g. "Brand: Polo") */
    private static final By BRAND            = By.xpath("//div[@class='product-information']//p[contains(text(),'Brand')]");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Xác nhận product detail page đã load (tên sản phẩm visible) */
    public boolean isProductDetailVisible() {
        return isDisplayed(PRODUCT_NAME, 10);
    }

    /** Xác nhận Category information hiển thị */
    public boolean isCategoryVisible() {
        return isDisplayed(PRODUCT_CATEGORY, 5);
    }

    /** Xác nhận Price hiển thị */
    public boolean isPriceVisible() {
        return isDisplayed(PRODUCT_PRICE, 5);
    }

    /** Xác nhận Availability hiển thị */
    public boolean isAvailabilityVisible() {
        return isDisplayed(AVAILABILITY, 5);
    }

    /** Xác nhận Condition hiển thị */
    public boolean isConditionVisible() {
        return isDisplayed(CONDITION, 5);
    }

    /** Xác nhận Brand hiển thị */
    public boolean isBrandVisible() {
        return isDisplayed(BRAND, 5);
    }

    // -----------------------------------------------------------------
    // Data Getters (for assertion messages)
    // -----------------------------------------------------------------

    public String getProductName()  { return getText(PRODUCT_NAME); }
    public String getCategory()     { return getText(PRODUCT_CATEGORY); }
    public String getPrice()        { return getText(PRODUCT_PRICE); }
    public String getAvailability() { return getText(AVAILABILITY); }
    public String getCondition()    { return getText(CONDITION); }
    public String getBrand()        { return getText(BRAND); }
}
