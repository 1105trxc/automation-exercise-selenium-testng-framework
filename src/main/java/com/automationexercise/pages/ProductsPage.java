package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProductsPage – Page Object cho trang /products.
 *
 * Test Cases:
 * - TC-AE-008: Verify All Products and product detail page
 * - TC-AE-009: Search Product (Phase 3+)
 *
 * Trang này hiển thị tất cả sản phẩm dưới dạng danh sách.
 * Mỗi sản phẩm có nút "View Product" để vào trang chi tiết.
 */
public class ProductsPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ProductsPage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    /** "ALL PRODUCTS" heading – xác nhận đang ở trang Products */
    private static final By ALL_PRODUCTS_HEADING = By.xpath("//h2[normalize-space()='All Products']");

    /** Tất cả các nút "View Product" trong danh sách */
    private static final By VIEW_PRODUCT_LINKS = By.xpath("//a[contains(@href,'/product_details/')]");

    /** "View Product" link của sản phẩm ĐẦU TIÊN trong danh sách */
    private static final By FIRST_PRODUCT_VIEW = By.xpath("(//a[contains(@href,'/product_details/')])[1]");

    /** Search bar */
    private static final By SEARCH_INPUT  = By.id("search_product");
    private static final By SEARCH_BUTTON = By.id("submit_search");

    /** "SEARCHED PRODUCTS" heading – xuất hiện sau khi search */
    private static final By SEARCHED_PRODUCTS_HEADING = By.xpath("//h2[normalize-space()='Searched Products']");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Xác nhận "ALL PRODUCTS" heading visible */
    public boolean isAllProductsVisible() {
        return isDisplayed(ALL_PRODUCTS_HEADING, 10);
    }

    /** Xác nhận ít nhất 1 sản phẩm có nút "View Product" */
    public boolean hasProducts() {
        return isDisplayed(VIEW_PRODUCT_LINKS, 5);
    }

    /** Xác nhận "SEARCHED PRODUCTS" heading visible sau khi search */
    public boolean isSearchedProductsVisible() {
        return isDisplayed(SEARCHED_PRODUCTS_HEADING, 10);
    }

    // -----------------------------------------------------------------
    // Navigation Actions
    // -----------------------------------------------------------------

    /**
     * Clicks "View Product" cho sản phẩm đầu tiên trong danh sách.
     * Returns ProductDetailPage vì navigation sang trang /product_details/{id}
     */
    public ProductDetailPage clickFirstProductViewProduct() {
        log.info("Clicking 'View Product' for first product");
        click(FIRST_PRODUCT_VIEW);
        return new ProductDetailPage(driver);
    }

    // -----------------------------------------------------------------
    // Search Actions
    // -----------------------------------------------------------------

    public ProductsPage searchFor(String keyword) {
        log.info("Searching for product: '{}'", keyword);
        type(SEARCH_INPUT, keyword);
        click(SEARCH_BUTTON);
        return this;
    }
}
