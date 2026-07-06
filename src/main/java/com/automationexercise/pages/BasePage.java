package com.automationexercise.pages;

import com.automationexercise.constants.FrameworkConstants;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * BasePage – Abstract base class for all Page Objects.
 *
 * WHY THIS CLASS EXISTS:
 * Every page needs to click, type, wait, scroll, etc.
 * Instead of duplicating these actions in every page class,
 * BasePage provides them as protected methods that all pages inherit.
 *
 * RULES:
 * - BasePage must NOT contain locators (those belong in specific page classes)
 * - BasePage must NOT contain assertions (those belong in test classes)
 * - BasePage must NOT contain test data
 * - Every method must have a single, clear purpose
 *
 * HOW TO USE:
 *   public class LoginPage extends BasePage {
 *       public LoginPage(WebDriver driver) { super(driver); }
 *       // Use: click(loginButton), type(emailInput, text), etc.
 *   }
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private static final Logger log = LoggerFactory.getLogger(BasePage.class);

    /**
     * Constructor: every Page Object receives the driver and creates its own wait.
     * Using Explicit Wait (not implicit) as per framework policy.
     */
    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(FrameworkConstants.EXPLICIT_WAIT_SECONDS));
    }

    // =====================================================================
    // ELEMENT ACTIONS
    // =====================================================================

    /**
     * Waits for element to be clickable, then clicks it.
     * Automatically handles Google Ads overlay if click is intercepted.
     */
    protected void click(By locator) {
        try {
            // Proactively dismiss vignette before clicking to prevent intercept
            dismissVignetteAd();
            waitUntilClickable(locator).click();
            log.debug("Clicked: {}", locator);
        } catch (ElementClickInterceptedException e) {
            log.warn("⚠️ Click intercepted for {}. Removing ads and retrying...", locator);
            removeAds();
            try {
                waitUntilClickable(locator).click();
                log.info("✅ Click succeeded after removing ads.");
            } catch (Exception ex) {
                log.warn("⚠️ Standard click still failed. Forcing click using JavaScript.");
                jsClick(locator);
            }
        }
    }

    /**
     * Removes Google Ads (iframes and specific classes) from the DOM.
     * Also dismisses Google Vignette ads if present.
     * Useful for automationexercise.com which is heavy on ads.
     */
    protected void removeAds() {
        // 1. Dismiss vignette overlay first (it blocks everything)
        dismissVignetteAd();

        // 2. Remove standard inline ads from DOM
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "var ads = document.querySelectorAll(" +
                "'iframe[id*=google], iframe[src*=doubleclick], .adsbygoogle, " +
                "[id^=ad_], [class^=ad-], [id*=aswift], [id*=google-ads]');" +
                "if(ads) ads.forEach(function(ad) { if(ad.parentNode) ad.parentNode.removeChild(ad); });"
            );
            log.debug("Inline ads removed from DOM.");
        } catch (Exception e) {
            log.debug("Failed to remove inline ads: {}", e.getMessage());
        }
    }

    /**
     * Dismisses Google Vignette ads – the full-screen overlay that appears
     * when navigating to automationexercise.com.
     *
     * HOW TO IDENTIFY VIGNETTE:
     * - URL contains "#google_vignette" hash fragment
     * - A full-screen iframe/overlay appears over the page
     * - Has a "Close" button accessible via keyboard or click
     *
     * STRATEGY (3-level fallback):
     * 1. Try clicking the Close button (various CSS selectors)
     * 2. Press Escape key (sometimes dismisses overlay)
     * 3. Navigate to the clean URL (strip #google_vignette hash)
     */
    protected void dismissVignetteAd() {
        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.contains("google_vignette") && !currentUrl.contains("#google")) {
            return; // No vignette present – skip
        }

        log.warn("🔔 Google Vignette ad detected (URL: {}). Attempting to dismiss...", currentUrl);

        // Strategy 1: Try clicking the visible "Close" button
        // The vignette close button can appear in multiple forms
        By[] closeButtonLocators = {
            By.xpath("//div[contains(@id,'dismiss')]//button"),
            By.xpath("//button[contains(@aria-label,'Close')]"),
            By.xpath("//button[contains(@aria-label,'close')]"),
            By.xpath("//*[text()='Close'][@role='button' or self::button]"),
            By.xpath("//span[@id='dismiss-button']"),
            By.id("dismiss-button"),
            By.cssSelector("[data-dismiss], .ad-close, .close-btn, #ad_close"),
        };

        for (By closeLocator : closeButtonLocators) {
            try {
                WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.elementToBeClickable(closeLocator));
                closeBtn.click();
                log.info("✅ Vignette dismissed by clicking Close button.");
                return;
            } catch (Exception ignored) {
                // Try next locator
            }
        }

        // Strategy 2: Press Escape key
        try {
            log.debug("Trying Escape key to dismiss vignette...");
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            java.lang.Thread.sleep(500); // Short pause to let overlay respond
            if (!driver.getCurrentUrl().contains("google_vignette")) {
                log.info("✅ Vignette dismissed by Escape key.");
                return;
            }
        } catch (Exception ignored) {}

        // Strategy 3 (last resort): Strip the #google_vignette hash and navigate to clean URL
        try {
            String cleanUrl = currentUrl.contains("#")
                ? currentUrl.substring(0, currentUrl.indexOf("#"))
                : currentUrl;
            log.warn("⚠️ Navigating to clean URL to bypass vignette: {}", cleanUrl);
            driver.navigate().to(cleanUrl);

            // Wait for page to be stable after navigation
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(
                    ExpectedConditions.urlContains("google_vignette")
                ));
            log.info("✅ Vignette bypassed via clean URL navigation.");
        } catch (Exception e) {
            log.error("❌ Failed to dismiss vignette ad. Test may be unstable. Error: {}", e.getMessage());
        }
    }

    /**
     * Waits for element to be visible, clears it, then types the text.
     * NEVER use Thread.sleep() to wait before typing.
     */
    protected void type(By locator, String text) {
        WebElement element = waitUntilVisible(locator);
        element.clear();
        element.sendKeys(text);
        log.debug("Typed into {}: [{}]", locator, text);
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

    /**
     * Checks if an element is displayed using the default explicit wait.
     * Returns false instead of throwing if element doesn't exist.
     */
    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Checks if an element is displayed within a custom timeout (seconds).
     * Use this for conditional checks where you don't want the full 15s wait.
     *
     * NOTE: Proactively dismisses Vignette ads before waiting.
     * This prevents false negatives when an ad overlay blocks page content
     * right after navigation (e.g., after clickContinue → home page).
     */
    protected boolean isDisplayed(By locator, int timeoutSeconds) {
        // Dismiss vignette overlay first so it doesn't block the check
        dismissVignetteAd();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // =====================================================================
    // WAIT ACTIONS (Explicit Waits)
    // =====================================================================

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

    // =====================================================================
    // SCROLL ACTIONS
    // =====================================================================

    /** Scrolls element into center of viewport using JavaScript. */
    protected void scrollIntoView(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        log.debug("Scrolled into view: {}", locator);
    }

    /** Scrolls to the bottom of the page. */
    protected void scrollToBottom() {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight);");
        log.debug("Scrolled to bottom of page");
    }

    /** Scrolls to the top of the page. */
    protected void scrollToTop() {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, 0);");
        log.debug("Scrolled to top of page");
    }

    // =====================================================================
    // HOVER ACTIONS
    // =====================================================================

    /** Moves mouse cursor over an element (triggers hover state). */
    protected void hoverOver(By locator) {
        WebElement element = waitUntilVisible(locator);
        new Actions(driver).moveToElement(element).perform();
        log.debug("Hovered over: {}", locator);
    }

    // =====================================================================
    // DROPDOWN ACTIONS
    // =====================================================================

    /** Selects a dropdown option by its visible text. */
    protected void selectByVisibleText(By locator, String visibleText) {
        new Select(waitUntilVisible(locator)).selectByVisibleText(visibleText);
    }

    /** Selects a dropdown option by its value attribute. */
    protected void selectByValue(By locator, String value) {
        new Select(waitUntilVisible(locator)).selectByValue(value);
    }

    // =====================================================================
    // JAVASCRIPT ACTIONS (use sparingly – only when normal click fails)
    // =====================================================================

    /**
     * Clicks an element using JavaScript.
     * Use ONLY when: element is covered by another element (overlay),
     * or when normal click doesn't trigger the expected action.
     * Document WHY you used jsClick instead of regular click.
     */
    protected void jsClick(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        log.debug("JS clicked: {}", locator);
    }

    // =====================================================================
    // NAVIGATION HELPERS
    // =====================================================================

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
}
