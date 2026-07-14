package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ProductsPage – Page Object cho trang /products.
 *
 * Test Cases:
 * - TC-AE-008: Verify All Products and product detail page
 * - TC-AE-009: Search Product
 * - TC-AE-012: Add Products in Cart (hover add to cart)
 * - TC-AE-018: View Category Products (Women, Men sidebar)
 * - TC-AE-019: View & Cart Brand Products (Brand sidebar)
 * - TC-AE-020: Search Products and Verify Cart After Login
 * - TC-AE-021: Add Review on Product
 *
 * PAGE STRUCTURE:
 * - Left sidebar: Category accordion (Women/Men/Kids) + Brand list
 * - Main content: Product cards in grid
 * - Each card: thumbnail + product name + price + "Add to cart" button
 *
 * HOVER MECHANIC (TC-012):
 * Product cards có overlay xuất hiện khi hover, chứa "Add to cart" button.
 * Flow: hover(card) → waitForOverlay → click(Add to cart) → modal appears.
 *
 * ADD TO CART MODAL:
 * Sau khi add, modal xuất hiện với 2 options:
 * - "Continue Shopping" → close modal, stay on ProductsPage
 * - "View Cart" → navigate to CartPage
 */
public class ProductsPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(ProductsPage.class);

    // -----------------------------------------------------------------
    // Locators – Product List
    // -----------------------------------------------------------------

    /** "ALL PRODUCTS" heading */
    private static final By ALL_PRODUCTS_HEADING = By.xpath("//h2[normalize-space()='All Products']");

    /** Tất cả link "View Product" */
    private static final By VIEW_PRODUCT_LINKS = By.xpath("//a[contains(@href,'/product_details/')]");

    /** "View Product" của sản phẩm đầu tiên */
    private static final By FIRST_PRODUCT_VIEW = By.xpath("(//a[contains(@href,'/product_details/')])[1]");

    // -----------------------------------------------------------------
    // Locators – Search
    // -----------------------------------------------------------------

    /** Search input */
    private static final By SEARCH_INPUT = By.id("search_product");

    /** Search submit button */
    private static final By SEARCH_BUTTON = By.id("submit_search");

    /** "SEARCHED PRODUCTS" heading */
    private static final By SEARCHED_PRODUCTS_HEADING = By.xpath("//h2[normalize-space()='Searched Products']");

    // -----------------------------------------------------------------
    // Locators – Add to Cart (hover cards)
    // -----------------------------------------------------------------

    /**
     * "Add to cart" buttons trên product overlay (visible sau hover).
     * Tất cả buttons dùng class "btn btn-default add-to-cart".
     */
    private static final By ADD_TO_CART_BUTTONS =
            By.cssSelector(".productinfo.text-center a.add-to-cart");

    /** Sản phẩm đầu tiên – wrapper div để hover */
    private static final By FIRST_PRODUCT_WRAPPER =
            By.xpath("(//div[@class='product-image-wrapper'])[1]");

    /** Sản phẩm thứ hai – wrapper div để hover */
    private static final By SECOND_PRODUCT_WRAPPER =
            By.xpath("(//div[@class='product-image-wrapper'])[2]");

    /** Add to cart button của sản phẩm đầu tiên */
    private static final By FIRST_ADD_TO_CART =
            By.xpath("(//div[@class='productinfo text-center']//a[contains(@class,'add-to-cart')])[1]");

    /** Add to cart button của sản phẩm thứ hai */
    private static final By SECOND_ADD_TO_CART =
            By.xpath("(//div[@class='productinfo text-center']//a[contains(@class,'add-to-cart')])[2]");

    // -----------------------------------------------------------------
    // Locators – Add to Cart Modal
    // -----------------------------------------------------------------

    /** "Continue Shopping" button trong modal */
    private static final By CONTINUE_SHOPPING_BTN = By.cssSelector("button.close-modal");

    /** "View Cart" link trong modal sau khi Add to Cart */
    private static final By VIEW_CART_MODAL_LINK = By.cssSelector(".modal-body a[href='/view_cart']");

    // -----------------------------------------------------------------
    // Locators – Category Sidebar (TC-018)
    // -----------------------------------------------------------------

    /** "CATEGORY" sidebar section heading */
    private static final By CATEGORY_SECTION = By.xpath("//h2[normalize-space()='Category']");

    /** "Women" category accordion link */
    private static final By CATEGORY_WOMEN = By.xpath("//a[normalize-space()='Women'][@href='#Women']");

    /**
     * First subcategory link under Women (e.g. Dress, Tops, Saree).
     * Chọn link đầu tiên trong panel #Women để không hardcode.
     */
    private static final By FIRST_WOMEN_SUBCATEGORY =
            By.xpath("(//div[@id='Women']//li/a)[1]");

    /** "Men" category accordion link */
    private static final By CATEGORY_MEN = By.xpath("//a[normalize-space()='Men'][@href='#Men']");

    /** First subcategory link under Men (e.g. Tshirts, Jeans, Casual) */
    private static final By FIRST_MEN_SUBCATEGORY =
            By.xpath("(//div[@id='Men']//li/a)[1]");

    /** Category page heading (e.g. "WOMEN - TOPS PRODUCTS") */
    private static final By CATEGORY_PAGE_HEADING = By.cssSelector(".title.text-center");

    // -----------------------------------------------------------------
    // Locators – Brand Sidebar (TC-019)
    // -----------------------------------------------------------------

    /** Brands section label */
    private static final By BRANDS_SECTION = By.cssSelector(".brands_products h2");

    /** Tất cả brand links trong sidebar */
    private static final By BRAND_LINKS = By.cssSelector(".brands-name ul li a");

    /** Brand page heading sau khi click brand */
    private static final By BRAND_PAGE_HEADING = By.cssSelector(".title.text-center");

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

    /** Xác nhận category sidebar visible */
    public boolean isCategoryVisible() {
        return isDisplayed(CATEGORY_SECTION, 5);
    }

    /** Xác nhận brand sidebar visible */
    public boolean isBrandSectionVisible() {
        return isDisplayed(BRANDS_SECTION, 5);
    }

    /** Trả về heading text của trang category/brand hiện tại */
    public String getCategoryPageHeading() {
        return getText(CATEGORY_PAGE_HEADING);
    }

    /** Đếm số sản phẩm trong kết quả search */
    public int getSearchedProductCount() {
        return driver.findElements(ADD_TO_CART_BUTTONS).size();
    }

    // -----------------------------------------------------------------
    // Navigation – Product Detail
    // -----------------------------------------------------------------

    /** Click "View Product" của sản phẩm đầu tiên → ProductDetailPage */
    public ProductDetailPage clickFirstProductViewProduct() {
        log.info("Clicking 'View Product' for first product");
        jsClick(FIRST_PRODUCT_VIEW);
        return new ProductDetailPage(driver);
    }

    // -----------------------------------------------------------------
    // Search Actions (TC-009, TC-020)
    // -----------------------------------------------------------------

    /**
     * Nhập keyword và click search.
     * Returns this (ProductsPage) vì trang reload với search results.
     */
    public ProductsPage searchFor(String keyword) {
        log.info("Searching for product: '{}'", keyword);
        type(SEARCH_INPUT, keyword);
        click(SEARCH_BUTTON);
        return this;
    }

    // -----------------------------------------------------------------
    // Add to Cart Actions (TC-012, TC-020)
    // -----------------------------------------------------------------

    /**
     * Hover sản phẩm đầu tiên và click "Add to cart".
     *
     * FLOW: hoverOver(wrapper) → click(Add to cart)
     * Dùng JS click để bypass lỗi ElementClickIntercepted do thẻ overlay che khuất
     * button chính khi hover.
     */
    public ProductsPage hoverAndAddFirstProductToCart() {
        log.info("Hovering first product and clicking Add to Cart");
        hoverOver(FIRST_PRODUCT_WRAPPER);
        WebElement btn = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(FIRST_ADD_TO_CART));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        return this;
    }

    /**
     * Hover sản phẩm thứ hai và click "Add to cart".
     */
    public ProductsPage hoverAndAddSecondProductToCart() {
        log.info("Hovering second product and clicking Add to Cart");
        hoverOver(SECOND_PRODUCT_WRAPPER);
        WebElement btn = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(SECOND_ADD_TO_CART));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        return this;
    }

    /**
     * Click "Continue Shopping" trong modal sau khi Add to Cart.
     * Đóng modal, ở lại ProductsPage.
     */
    public ProductsPage clickContinueShopping() {
        log.info("Clicking Continue Shopping");
        click(CONTINUE_SHOPPING_BTN);
        return this;
    }

    /**
     * Click "View Cart" trong modal sau khi Add to Cart.
     * Navigate đến CartPage.
     */
    public CartPage clickViewCartFromModal() {
        log.info("Clicking View Cart from modal");
        click(VIEW_CART_MODAL_LINK);
        return new CartPage(driver);
    }

    /**
     * Add tất cả sản phẩm trong kết quả search vào cart.
     * Dùng cho TC-020: sau khi search, add tất cả products.
     *
     * Flow: click Add to Cart → Continue Shopping → lặp lại
     * Với product cuối cùng: click "View Cart" thay vì Continue Shopping.
     */
    public CartPage addAllSearchedProductsToCart() {

        List<WebElement> addButtons = driver.findElements(ADD_TO_CART_BUTTONS);
        log.info("Adding {} searched products to cart", addButtons.size());

        for (int i = 0; i < addButtons.size(); i++) {
            // Re-fetch buttons sau mỗi iteration vì DOM có thể stale
            List<WebElement> buttons = driver.findElements(ADD_TO_CART_BUTTONS);
            buttons.get(i).click();

            if (i < addButtons.size() - 1) {
                // Không phải sản phẩm cuối → Continue Shopping
                click(CONTINUE_SHOPPING_BTN);
            }
        }

        // Sau sản phẩm cuối → View Cart
        return clickViewCartFromModal();
    }

    // -----------------------------------------------------------------
    // Category Actions (TC-018)
    // -----------------------------------------------------------------

    /**
     * Click "Women" category để mở accordion.
     */
    public ProductsPage clickWomenCategory() {
        log.info("Clicking Women category");
        jsClick(CATEGORY_WOMEN);
        return this;
    }

    /**
     * Click subcategory đầu tiên trong Women (e.g. Dress hoặc Tops).
     * Returns this vì page reload với category products.
     */
    public ProductsPage clickFirstWomenSubcategory() {
        log.info("Clicking first Women subcategory");
        jsClick(FIRST_WOMEN_SUBCATEGORY);
        return this;
    }

    /**
     * Click "Men" category để mở accordion.
     */
    public ProductsPage clickMenCategory() {
        log.info("Clicking Men category");
        jsClick(CATEGORY_MEN);
        return this;
    }

    /**
     * Click subcategory đầu tiên trong Men (e.g. Tshirts).
     */
    public ProductsPage clickFirstMenSubcategory() {
        log.info("Clicking first Men subcategory");
        jsClick(FIRST_MEN_SUBCATEGORY);
        return this;
    }

    // -----------------------------------------------------------------
    // Brand Actions (TC-019)
    // -----------------------------------------------------------------

    /**
     * Click brand đầu tiên trong sidebar.
     * Returns this vì page reload với brand products.
     */
    public ProductsPage clickFirstBrand() {
        List<WebElement> brands = driver.findElements(BRAND_LINKS);
        if (brands.isEmpty()) {
            throw new RuntimeException("No brand links found in sidebar");
        }
        String brandName = brands.get(0).getText();
        log.info("Clicking first brand: '{}'", brandName);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", brands.get(0));
        return this;
    }

    /**
     * Click brand thứ hai trong sidebar.
     * Returns this vì page reload với brand products.
     */
    public ProductsPage clickSecondBrand() {
        List<WebElement> brands = driver.findElements(BRAND_LINKS);
        if (brands.size() < 2) {
            throw new RuntimeException("Less than 2 brand links found in sidebar");
        }
        String brandName = brands.get(1).getText();
        log.info("Clicking second brand: '{}'", brandName);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", brands.get(1));
        return this;
    }
}