package com.automationexercise.pages;

import com.automationexercise.components.AdHandler;
import com.automationexercise.constants.FrameworkConstants;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

/**
 * BasePage – Abstract base class for all Page Objects.
 *
 * TRÁCH NHIỆM:
 * - Cung cấp các thao tác element chung: click, type, getText, wait, scroll...
 * - Không chứa locator (thuộc về từng page class cụ thể)
 * - Không chứa assertion (thuộc về test class)
 * - Không chứa test data
 *
 * DESIGN DECISIONS:
 *
 * 1. click() KHÔNG tự động xử lý quảng cáo
 *    Lý do: Tự động fallback che giấu lỗi thật (locator sai, element disabled, v.v.)
 *    Page class gọi handleVignette() hoặc AdHandler trước khi click nếu cần.
 *
 * 2. clickWithJsFallback() chỉ dùng khi đã biết thực sự cần
 *    Lý do: JS click bypass validation của browser, dễ bỏ qua lỗi UI thật.
 *
 * 3. isDisplayedNow() vs isDisplayed()
 *    - isDisplayedNow(): kiểm tra tức thì, không wait. Dùng cho negative assertion.
 *    - isDisplayed(locator): wait với default timeout. Dùng cho positive assertion.
 *    - isDisplayed(locator, seconds): explicit timeout. Dùng khi biết cần bao lâu.
 *
 * 4. type() không log nội dung nhập
 *    Lý do: Tránh lộ password và dữ liệu nhạy cảm trong log file.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private static final Logger log = LoggerFactory.getLogger(BasePage.class);

    protected BasePage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null.");
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(FrameworkConstants.EXPLICIT_WAIT_SECONDS));
    }

    // =========================================================================
    // ELEMENT ACTIONS
    // =========================================================================

    /**
     * Waits for element to be clickable, then clicks it.
     *
     * KHÔNG tự động xử lý quảng cáo hay fallback JS.
     * Nếu click bị intercept bởi quảng cáo, gọi handleVignette() trước,
     * hoặc dùng clickWithJsFallback() ở nơi đã biết thực sự cần JS fallback.
     */
    protected void click(By locator) {
        waitUntilClickable(locator).click();
        log.debug("Clicked: {}", locator);
    }

    /**
     * Giống click() nhưng có fallback sang JavaScript nếu bị ElementClickInterceptedException.
     *
     * CHỈ dùng khi đã xác định click bị intercept bởi overlay không thể tránh khác.
     * Ghi lại lý do trong comment tại nơi gọi.
     */
    protected void clickWithJsFallback(By locator) {
        try {
            waitUntilClickable(locator).click();
            log.debug("Clicked: {}", locator);
        } catch (ElementClickInterceptedException e) {
            log.warn("⚠️ Click intercepted for {}. Using JS fallback (verify this is expected).", locator);
            jsClick(locator);
        }
    }

    /**
     * Waits for element to be visible, clears it, then types the text.
     *
     * KHÔNG log nội dung nhập vào để tránh lộ password trong log file.
     * Chỉ log số ký tự để debug khi cần thiết.
     */
    protected void type(By locator, String text) {
        Objects.requireNonNull(text, "Text to type must not be null.");
        WebElement element = waitUntilVisible(locator);
        element.clear();
        element.sendKeys(text);
        log.debug("Typed into {} ({} chars).", locator, text.length());
    }

    /**
     * Returns the visible text content of an element (trimmed).
     */
    protected String getText(By locator) {
        return waitUntilVisible(locator).getText().trim();
    }

    /**
     * Returns the value of an element's attribute.
     */
    protected String getAttribute(By locator, String attribute) {
        return waitUntilVisible(locator).getAttribute(attribute);
    }

    // =========================================================================
    // ELEMENT VISIBILITY CHECKS
    // =========================================================================

    /**
     * Kiểm tra TỨC THÌ (không wait) xem element có đang hiển thị không.
     *
     * KHI NÀO DÙNG:
     * - Negative assertion (kiểm tra element KHÔNG tồn tại)
     * - Conditional check khi element đã có thể có mặt
     *
     * VÍ DỤ:
     *   Assert.assertFalse(page.isDisplayedNow(LOGIN_BUTTON), "Should be logged out");
     *
     * KHÔNG dùng cho positive assertion vì không wait → false negative nếu page chưa load.
     */
    protected boolean isDisplayedNow(By locator) {
        return driver.findElements(locator)
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    /**
     * Waits up to DEFAULT timeout, returns true nếu element visible trong thời gian đó.
     *
     * KHI NÀO DÙNG:
     * - Positive assertion (kiểm tra element PHẢI tồn tại)
     * - Gọi khi không biết chắc tốc độ page load
     *
     * VÍ DỤ:
     *   Assert.assertTrue(homePage.isUserLoggedIn(), "Should be logged in");
     */
    protected boolean isDisplayed(By locator) {
        return isDisplayed(locator, FrameworkConstants.EXPLICIT_WAIT_SECONDS);
    }

    /**
     * Waits up to custom timeout (seconds), returns true nếu element visible.
     *
     * KHI NÀO DÙNG:
     * - Khi biết element cần ít/nhiều thời gian hơn default (ví dụ: slow network = 20s)
     *
     * VÍ DỤ:
     *   boolean hasLogoutLink = isDisplayed(LOGOUT_LINK, 5);
     */
    protected boolean isDisplayed(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // =========================================================================
    // AD HANDLING – proxy đến AdHandler (logic nằm ở AdHandler)
    // =========================================================================

    /**
     * Gọi AdHandler để dismiss Google Vignette ad nếu đang hiện.
     *
     * Page class gọi method này ở những nơi mà vignette có thể xuất hiện,
     * ví dụ: sau khi navigate về trang chủ.
     * BasePage không tự động gọi trong mọi click() hay isDisplayed().
     */
    protected void handleVignette() {
        AdHandler.dismissIfPresent(driver);
    }

    // =========================================================================
    // WAIT ACTIONS
    // =========================================================================

    /** Waits until element is visible. Returns the element when visible. */
    protected WebElement waitUntilVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Waits until element is clickable. Returns the element when clickable. */
    protected WebElement waitUntilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Waits until element is no longer visible (e.g., loading spinner disappears). */
    protected void waitUntilInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Waits until the URL contains a specific substring. */
    protected void waitForUrlContains(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    /** Waits until the URL exactly matches the given value. */
    protected void waitForUrlToBe(String expectedUrl) {
        wait.until(ExpectedConditions.urlToBe(expectedUrl));
    }

    /** Waits until specific text appears in an element. */
    protected void waitForTextInElement(By locator, String expectedText) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    // =========================================================================
    // SCROLL ACTIONS
    // =========================================================================

    /**
     * Scrolls element into center of viewport using JavaScript.
     * Dùng waitUntilVisible() để đảm bảo element đã tồn tại trong DOM trước khi scroll.
     */
    protected void scrollIntoView(By locator) {
        WebElement element = waitUntilVisible(locator);
        js().executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        log.debug("Scrolled into view: {}", locator);
    }

    /** Scrolls to the bottom of the page. */
    protected void scrollToBottom() {
        js().executeScript("window.scrollTo(0, document.body.scrollHeight);");
        log.debug("Scrolled to bottom of page.");
    }

    /** Scrolls to the top of the page. */
    protected void scrollToTop() {
        js().executeScript("window.scrollTo(0, 0);");
        log.debug("Scrolled to top of page.");
    }

    // =========================================================================
    // HOVER ACTIONS
    // =========================================================================

    /** Moves mouse cursor over an element (triggers hover state). */
    protected void hoverOver(By locator) {
        WebElement element = waitUntilVisible(locator);
        new Actions(driver).moveToElement(element).perform();
        log.debug("Hovered over: {}", locator);
    }

    // =========================================================================
    // DROPDOWN ACTIONS
    // =========================================================================

    /** Selects a dropdown option by its visible text. */
    protected void selectByVisibleText(By locator, String visibleText) {
        new Select(waitUntilVisible(locator)).selectByVisibleText(visibleText);
    }

    /** Selects a dropdown option by its value attribute. */
    protected void selectByValue(By locator, String value) {
        new Select(waitUntilVisible(locator)).selectByValue(value);
    }

    // =========================================================================
    // JAVASCRIPT ACTIONS
    // =========================================================================

    /**
     * Clicks an element using JavaScript.
     *
     * CHỈ dùng khi:
     * - Element bị che bởi overlay không thể dismiss
     * - Normal click() đã thất bại và đã có lý do rõ ràng
     *
     * Dùng waitUntilClickable() để đảm bảo element đã sẵn sàng trước khi JS click.
     */
    protected void jsClick(By locator) {
        WebElement element = waitUntilClickable(locator);
        js().executeScript("arguments[0].click();", element);
        log.debug("JS clicked: {}", locator);
    }

    // =========================================================================
    // NAVIGATION HELPERS
    // =========================================================================

    /** Returns the current browser URL. */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Returns the browser tab title. */
    protected String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Handles browser alert dialogs (OK button).
     * Used in: Contact Us form submit (TC-AE-006).
     */
    protected void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /** Private helper để cast driver sang JavascriptExecutor. */
    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
