package com.automationexercise.pages;

import com.automationexercise.config.ConfigManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * BasePage – Abstract base class for all Page Objects.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private static final Logger log = LoggerFactory.getLogger(BasePage.class);

    protected BasePage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null.");
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigManager.getInt("explicitWait", 15)));
    }

    // =========================================================================
    // ELEMENT ACTIONS
    // =========================================================================

    /**
     * Waits for element to be clickable, then clicks it.
     *
     * Nếu click bị intercept bởi quảng cáo, lỗi sẽ được đẩy ra ngoài
     * để AEBasePage xử lý tập trung (RF-P0-03).
     */
    protected void click(By locator) {
        waitUntilClickable(locator).click();
        log.debug("Clicked: {}", locator);
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
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
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

    /** Waits for the browser to reach the expected origin, path, and query. */
    protected boolean waitForUrl(String expectedUrl) {
        URI expected = resolveAgainstCurrentUrl(expectedUrl);
        try {
            wait.until(currentDriver -> matchesNavigationTarget(
                    currentDriver.getCurrentUrl(), expected));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Checks the current navigation target without waiting. URL fragments are ignored. */
    protected boolean isAtUrl(String expectedUrl) {
        return matchesNavigationTarget(driver.getCurrentUrl(), resolveAgainstCurrentUrl(expectedUrl));
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

    private URI resolveAgainstCurrentUrl(String url) {
        URI candidate = URI.create(Objects.requireNonNull(url, "URL must not be null."));
        return candidate.isAbsolute()
                ? candidate.normalize()
                : URI.create(driver.getCurrentUrl()).resolve(candidate).normalize();
    }

    private static boolean matchesNavigationTarget(String actualUrl, URI expected) {
        try {
            URI actual = URI.create(actualUrl).normalize();
            return equalsIgnoreCase(actual.getScheme(), expected.getScheme())
                    && equalsIgnoreCase(actual.getHost(), expected.getHost())
                    && effectivePort(actual) == effectivePort(expected)
                    && Objects.equals(actual.getRawPath(), expected.getRawPath())
                    && Objects.equals(actual.getRawQuery(), expected.getRawQuery());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        return left == null ? right == null : right != null && left.equalsIgnoreCase(right);
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
