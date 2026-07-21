package com.automationexercise.pages;

import com.automationexercise.components.AdHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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
            By.cssSelector(".product-overlay a.add-to-cart");

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
        return isDisplayed(ALL_PRODUCTS_HEADING);
    }

    /** Xác nhận ít nhất 1 sản phẩm có nút "View Product" */
    public boolean hasProducts() {
        return isDisplayed(VIEW_PRODUCT_LINKS);
    }

    /** Xác nhận "SEARCHED PRODUCTS" heading visible sau khi search */
    public boolean isSearchedProductsVisible() {
        return isDisplayed(SEARCHED_PRODUCTS_HEADING);
    }

    /** Xác nhận category sidebar visible */
    public boolean isCategoryVisible() {
        return isDisplayed(CATEGORY_SECTION);
    }

    /** Xác nhận brand sidebar visible */
    public boolean isBrandSectionVisible() {
        return isDisplayed(BRANDS_SECTION);
    }

    /** Trả về heading text của trang category/brand hiện tại */
    public String getCategoryPageHeading() {
        return getText(CATEGORY_PAGE_HEADING);
    }


    /** Trả về danh sách tên các sản phẩm đang hiển thị trên trang */
    public java.util.List<String> getVisibleProductNames() {
        return driver.findElements(org.openqa.selenium.By.cssSelector(".productinfo p")).stream()
                .map(org.openqa.selenium.WebElement::getText)
                .collect(java.util.stream.Collectors.toList());
    }

    // -----------------------------------------------------------------
    // Navigation – Product Detail
    // -----------------------------------------------------------------

    /** Click "View Product" của sản phẩm đầu tiên → ProductDetailPage */
    public ProductDetailPage clickFirstProductViewProduct() {
        log.info("Clicking 'View Product' for first product");

        String sourceUrl = driver.getCurrentUrl();
        String targetUrl = waitUntilClickable(FIRST_PRODUCT_VIEW).getAttribute("href");
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalStateException("First product View Product link has no href.");
        }

        click(FIRST_PRODUCT_VIEW);
        boolean vignetteDismissed = AdHandler.dismissIfPresent(driver);

        if (!waitForUrl(targetUrl)) {
            if (!vignetteDismissed || !isAtUrl(sourceUrl)) {
                throw detailNavigationTimeout(targetUrl);
            }

            // This bounded continuation is only for a side-effect-free GET link
            // whose first click was proven to have opened a Google vignette.
            log.warn("Vignette consumed View Product navigation. Continuing the link once.");
            click(FIRST_PRODUCT_VIEW);
            AdHandler.dismissIfPresent(driver);

            if (!waitForUrl(targetUrl)) {
                throw detailNavigationTimeout(targetUrl);
            }
        }

        return new ProductDetailPage(driver).waitUntilLoaded();
    }

    private TimeoutException detailNavigationTimeout(String targetUrl) {
        return new TimeoutException("View Product did not reach '" + targetUrl
                + "'. Current URL: " + driver.getCurrentUrl());
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

    /** Hover the first product and click its overlay "Add to cart" button. */
    public ProductsPage hoverAndAddFirstProductToCart() {
        log.info("Hovering first product and clicking Add to Cart");
        return hoverAndAddProductToCart(1);
    }

    /**
     * Hover sản phẩm thứ hai và click "Add to cart".
     */
    public ProductsPage hoverAndAddSecondProductToCart() {
        log.info("Hovering second product and clicking Add to Cart");
        return hoverAndAddProductToCart(2);
    }

    /**
     * Returns the number of product cards currently present in the search results.
     * Used by CartFlow to control the orchestration loop.
     */
    public int getSearchedProductCount() {
        return driver.findElements(ADD_TO_CART_BUTTONS).size();
    }

    /**
     * Scrolls to, hovers over, and clicks "Add to cart" for the product at the given
     * 1-based index in the search results grid, then returns the resulting modal.
     *
     * CONTRACT:
     *   - Caller (CartFlow) is responsible for calling this method one product at a time.
     *   - Uses native WebDriver interactions only: scroll + hover + click.
     *   - No JS click, no retry loop, no Thread.sleep.
     *
     * WHY HOVER?
     *   Product cards reveal the overlay Add-to-cart button only on hover.
     *   scrollIntoView() ensures the card is visible in the viewport before hover.
     *
     * @param oneBasedIndex 1-based index of the product to add
     * @return AddToCartModal ready to call .clickContinueShopping() or .clickViewCart()
     */
    public com.automationexercise.components.AddToCartModal addSearchedProductAt(int oneBasedIndex) {
        log.info("Adding searched product #{} to cart", oneBasedIndex);
        hoverAndAddProductToCart(oneBasedIndex);

        return new com.automationexercise.components.AddToCartModal(driver).waitForModal();
    }

    private ProductsPage hoverAndAddProductToCart(int oneBasedIndex) {
        By wrapper = By.xpath(productWrapperXPath(oneBasedIndex));
        By productInfo = productPartAt(oneBasedIndex, "productinfo");
        By overlay = productPartAt(oneBasedIndex, "product-overlay");
        By overlayButton = By.xpath(
                productWrapperXPath(oneBasedIndex)
                        + "//div[contains(@class,'product-overlay')]//a[contains(@class,'add-to-cart')]"
        );

        scrollIntoView(wrapper);
        hoverOver(wrapper);
        wait.until(ignored -> isOverlayFullyExpanded(productInfo, overlay));
        click(overlayButton);
        return this;
    }

    private boolean isOverlayFullyExpanded(By productInfo, By overlay) {
        WebElement productInfoElement = driver.findElement(productInfo);
        WebElement overlayElement = driver.findElement(overlay);
        return overlayElement.isDisplayed()
                && overlayElement.getRect().getHeight() >= productInfoElement.getRect().getHeight() - 1;
    }

    private By productPartAt(int oneBasedIndex, String className) {
        return By.xpath(productWrapperXPath(oneBasedIndex)
                + "//div[contains(concat(' ', normalize-space(@class), ' '), ' " + className + " ')]");
    }

    private String productWrapperXPath(int oneBasedIndex) {
        return "(//div[contains(@class,'product-image-wrapper')])[" + oneBasedIndex + "]";
    }

    // -----------------------------------------------------------------
    // Category Actions (TC-018)
    // -----------------------------------------------------------------

    /**
     * Click "Women" category để mở accordion.
     */
    public ProductsPage clickWomenCategory() {
        log.info("Clicking Women category");
        click(CATEGORY_WOMEN);
        return this;
    }

    /**
     * Click subcategory đầu tiên trong Women (e.g. Dress hoặc Tops).
     * Returns this vì page reload với category products.
     */
    public ProductsPage clickFirstWomenSubcategory() {
        log.info("Clicking first Women subcategory");
        click(FIRST_WOMEN_SUBCATEGORY);
        return this;
    }

    /**
     * Click "Men" category để mở accordion.
     */
    public ProductsPage clickMenCategory() {
        log.info("Clicking Men category");
        click(CATEGORY_MEN);
        return this;
    }

    /**
     * Click subcategory đầu tiên trong Men (e.g. Tshirts).
     */
    public ProductsPage clickFirstMenSubcategory() {
        log.info("Clicking first Men subcategory");
        click(FIRST_MEN_SUBCATEGORY);
        return this;
    }

    // -----------------------------------------------------------------
    // Brand Actions (TC-019)
    // -----------------------------------------------------------------

    /**
     * Returns the brand name at the given 1-based index in the sidebar, normalized.
     * Raw text may appear as "(6) Polo" — this strips the count prefix.
     *
     * @param oneBasedIndex 1-based index of the brand link
     * @return normalized brand name, e.g. "Polo"
     */
    public String getBrandNameAt(int oneBasedIndex) {
        By locator = By.xpath("(//div[contains(@class,'brands-name')]//li/a)[" + oneBasedIndex + "]");
        String raw = getText(locator);
        return raw.replaceFirst("^\\s*\\(\\d+\\)\\s*", "").trim();
    }

    /**
     * Clicks the brand at the given 1-based index in the sidebar.
     * Returns this (ProductsPage) because the page reloads with brand products.
     *
     * @param oneBasedIndex 1-based index of the brand link
     * @return this ProductsPage with brand products loaded
     */
    public ProductsPage clickBrandAt(int oneBasedIndex) {
        By locator = By.xpath("(//div[contains(@class,'brands-name')]//li/a)[" + oneBasedIndex + "]");
        String brandName = getBrandNameAt(oneBasedIndex);
        log.info("Clicking brand #{}: '{}'", oneBasedIndex, brandName);
        click(locator);
        return this;
    }
}
