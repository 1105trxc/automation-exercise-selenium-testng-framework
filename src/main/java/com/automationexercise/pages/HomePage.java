package com.automationexercise.pages;

import com.automationexercise.components.AddToCartModal;
import com.automationexercise.components.AdHandler;
import com.automationexercise.components.FooterSubscriptionComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HomePage – Page Object for the home page (https://automationexercise.com/).
 *
 * Test Cases:
 * - TC-AE-001 to TC-AE-008: Basic nav, login, register
 * - TC-AE-010: Verify Subscription in home page (footer form)
 * - TC-AE-022: Add to cart from Recommended items
 * - TC-AE-025: Verify Scroll Up using Arrow button
 * - TC-AE-026: Verify Scroll Up without Arrow button (PAGE_UP key)
 *
 * NAV ELEMENTS (when NOT logged in):
 *   Home | Products | Cart | Signup/Login | Test Cases | API Testing | Contact Us
 *
 * NAV ELEMENTS (when logged in):
 *   Home | Products | Cart | Logged in as <b>username</b> | Logout | Delete Account | ...
 */
public class HomePage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(HomePage.class);

    // -----------------------------------------------------------------
    // Locators – Content
    // -----------------------------------------------------------------

    /** Main slider on home page – proves the page loaded */
    private static final By HOME_PAGE_INDICATOR  = By.id("slider-carousel");

    // -----------------------------------------------------------------
    // Locators – Recommended Items (TC-022)
    // -----------------------------------------------------------------

    /** Recommended items carousel section */
    private static final By RECOMMENDED_SECTION = By.id("recommended-item-carousel");

    /**
     * "Add to cart" button của sản phẩm đầu tiên trong Recommended items.
     * Carousel có thể chứa nhiều items; dùng item đầu tiên visible.
     */
    private static final String ACTIVE_RECOMMENDED_ITEM =
            "#recommended-item-carousel .carousel-inner > .item.active:not(.left):not(.right)";
    private static final By FIRST_RECOMMENDED_NAME = By.cssSelector(
            ACTIVE_RECOMMENDED_ITEM + " .product-image-wrapper .productinfo p");
    private static final By FIRST_RECOMMENDED_ADD_TO_CART = By.cssSelector(
            ACTIVE_RECOMMENDED_ITEM + " .product-image-wrapper a.add-to-cart");

    // -----------------------------------------------------------------
    // Locators – Scroll (TC-025, TC-026)
    // -----------------------------------------------------------------

    /**
     * Scroll-to-top arrow button (xuất hiện ở bottom-right sau khi scroll down).
     * CSS: #scrollUp (jQuery ScrollUp plugin)
     */
    private static final By SCROLL_UP_BUTTON = By.id("scrollUp");

    /**
     * Hero text ở đầu trang – verify sau khi scroll up thành công.
     * "Full-Fledged practice website for Automation Engineers"
     */
    private static final By HERO_TEXT = By.xpath(
            "//div[contains(@class,'item active')]//h2[contains(text(),'Full-Fledged')]");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Verifies the home page has loaded by checking the main slider. */
    public boolean isHomePageVisible() {
        return isDisplayed(HOME_PAGE_INDICATOR);
    }

    /** Waits until the main home-page carousel is actually displayed. */
    public HomePage waitUntilLoaded() {
        waitUntilVisible(HOME_PAGE_INDICATOR);
        return this;
    }

    // -----------------------------------------------------------------
    // Recommended Items (TC-022)
    // -----------------------------------------------------------------

    /**
     * Xác nhận "RECOMMENDED ITEMS" section visible.
     * Cần scrollToBottom() trước khi gọi method này.
     */
    public boolean isRecommendedItemsVisible() {
        return isDisplayed(RECOMMENDED_SECTION);
    }

    /** Returns the first product name on the stable active carousel slide. */
    public String getFirstActiveRecommendedProductName() {
        scrollIntoView(RECOMMENDED_SECTION);
        hoverOver(RECOMMENDED_SECTION);
        return getText(FIRST_RECOMMENDED_NAME);
    }

    /** Clicks the first product on the stable active slide and waits for its modal. */
    public AddToCartModal clickAddFirstRecommendedToCart() {
        log.info("Clicking Add to Cart on first recommended item");
        scrollIntoView(RECOMMENDED_SECTION);
        hoverOver(RECOMMENDED_SECTION);
        click(FIRST_RECOMMENDED_ADD_TO_CART);
        return new AddToCartModal(driver).waitForModal();
    }

    // -----------------------------------------------------------------
    // Scroll Actions (TC-025, TC-026)
    // -----------------------------------------------------------------

    /**
     * Click scroll-to-top arrow button (TC-025).
     * Button xuất hiện ở bottom-right sau khi scroll down.
     * Cần scrollToBottom() trước khi gọi method này.
     */
    public HomePage clickScrollUpButton() {
        log.info("Clicking scroll-up arrow button");
        String sourceUrl = driver.getCurrentUrl();
        click(SCROLL_UP_BUTTON);
        boolean vignetteDismissed = AdHandler.dismissLinkTriggeredVignette(driver);

        try {
            return waitForScrollToTop();
        } catch (TimeoutException timeout) {
            if (!vignetteDismissed || !isAtUrl(sourceUrl)) {
                throw timeout;
            }

            log.warn("Google vignette consumed scroll-up action. Continuing the arrow once.");
            click(SCROLL_UP_BUTTON);
            AdHandler.dismissLinkTriggeredVignette(driver);
            return waitForScrollToTop();
        }
    }

    /**
     * Scroll lên đầu trang bằng phím PAGE_UP (TC-026).
     * Không dùng scroll button – dùng keyboard thay thế.
     */
    public HomePage scrollUpWithPageUpKey() {
        log.info("Pressing HOME key to scroll up");
        driver.findElement(By.tagName("body")).sendKeys(Keys.HOME);
        return this;
    }

    /**
     * Chờ đến khi browser cuộn hoàn toàn lên đầu trang (window.scrollY == 0).
     * Dùng để thay thế fixed wait sau khi click scroll up.
     */
    public HomePage waitForScrollToTop() {
        log.info("Waiting for scroll animation to reach top");
        wait.until(d -> {
            Long scrollY = (Long) ((JavascriptExecutor) d)
                    .executeScript("return Math.round(window.scrollY);");
            return scrollY != null && scrollY <= 5;
        });
        return this;
    }

    /**
     * Xác nhận hero text "Full-Fledged practice website for Automation Engineers" visible.
     * Dùng để verify đã scroll về đầu trang thành công.
     */
    public boolean isHeroTextVisible() {
        try {
            WebElement heroText = waitUntilVisible(HERO_TEXT);
            wait.until(d -> Boolean.TRUE.equals(
                    ((JavascriptExecutor) d).executeScript(
                        "const el = arguments[0];" +
                        "const rect = el.getBoundingClientRect();" +
                        "const style = window.getComputedStyle(el);" +
                        "const viewportHeight = window.innerHeight || document.documentElement.clientHeight;" +
                        "const viewportWidth = window.innerWidth || document.documentElement.clientWidth;" +
                        "return style.display !== 'none' &&" +
                        "       style.visibility !== 'hidden' &&" +
                        "       parseFloat(style.opacity || '1') > 0 &&" +
                        "       rect.width > 0 && rect.height > 0 &&" +
                        "       rect.top >= 0 && rect.left >= 0 &&" +
                        "       rect.bottom <= viewportHeight && rect.right <= viewportWidth;",
                        heroText
                    )
                ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // -----------------------------------------------------------------
    // Scroll helpers (public wrappers for BasePage protected methods)
    // -----------------------------------------------------------------

    public HomePage goToBottom() {
        scrollToBottom();
        return this;
    }
}
