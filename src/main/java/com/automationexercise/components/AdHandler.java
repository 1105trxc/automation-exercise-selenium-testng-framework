package com.automationexercise.components;

import com.automationexercise.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * AdHandler – Handles third-party Google ad overlays on automationexercise.com.
 *
 * SCOPE:
 *   This class ONLY handles proven third-party ad elements from Google:
 *     - iframe[id^='aswift_']
 *     - iframe[id^='google_ads_']
 *     - ins.adsbygoogle
 *     - #google-anno-sa (Google AdSense ad-intent chip)
 *
 *   It does NOT hide:
 *     - Application modals
 *     - Confirmation dialogs
 *     - Loading blockers
 *     - Cookie consent banners
 *     - First-party overlays or error popups
 *
 * AD TYPES ON THIS SITE:
 *   Type 1 – Google Vignette (URL fragment #google_vignette):
 *     Appears before or during page load. Dismissed via Escape key.
 *     Only called from BaseTest.setUp() after initial navigation.
 *
 *   Type 2 – Full-viewport iframe (aswift_N):
 *     Appears after page load. No URL fragment.
 *     Caught as ElementClickInterceptedException in AEBasePage.click().
 *
 * Google may also inject an ad-intent chip into product content. The workaround
 * collapses it through its native close control before using the targeted fallback.
 */
public final class AdHandler {

    private static final Logger log = LoggerFactory.getLogger(AdHandler.class);

    private static final Duration CLOSE_BUTTON_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration OVERLAY_GONE_TIMEOUT = Duration.ofSeconds(3);

    private static final By KNOWN_AD_ELEMENTS = By.cssSelector(
            "iframe[id^='aswift_'], iframe[id^='google_ads_'], ins.adsbygoogle, #google-anno-sa");

    private static final By AD_INTENT_CHIP = By.id("google-anno-sa");
    private static final By AD_INTENT_CLOSE = By.cssSelector(
            "#google-anno-sa svg[role='button'][aria-label='Close shopping anchor']");

    private static final By[] KNOWN_AD_CLOSE_LOCATORS = {
        By.id("dismiss-button"),
        By.cssSelector("button[aria-label='Close ad']"),
        By.cssSelector("[aria-label='Close advertisement']")
    };

    /**
     * JavaScript that hides only the known Google ad iframe elements.
     *
     * WHY ONLY THESE SELECTORS:
     *   Broader selectors that target arbitrary divs or iframes risk hiding
     *   application-owned elements such as modals, loading overlays, or dialogs.
     *   We must target only the proven third-party ad containers.
     */
    private static final String HIDE_KNOWN_AD_ELEMENTS_JS =
        "var adFrames = document.querySelectorAll(" +
        "  'iframe[id^=\"aswift_\"], iframe[id^=\"google_ads_\"], ins.adsbygoogle, #google-anno-sa'" +
        ");" +
        "for (var i = 0; i < adFrames.length; i++) {" +
        "  adFrames[i].style.setProperty('display', 'none', 'important');" +
        "  adFrames[i].style.setProperty('visibility', 'hidden', 'important');" +
        "  adFrames[i].style.setProperty('pointer-events', 'none', 'important');" +
        "  adFrames[i].style.setProperty('z-index', '-1', 'important');" +
        "}";

    private AdHandler() {
        throw new UnsupportedOperationException("AdHandler is a utility class.");
    }

    public static boolean isWorkaroundEnabled() {
        return ConfigManager.getBoolean("thirdPartyAdWorkaround", true);
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Detects and dismisses a Google Vignette ad if the URL contains #google_vignette.
     *
     * WHEN TO CALL:
     *   Only at safe navigation boundaries, e.g., after initial page load in BaseTest.setUp().
     *   Not as a generic self-healing mechanism after every business action.
     *
     * @param driver the current WebDriver instance
     */
    public static void dismissIfPresent(WebDriver driver) {
        if (!isWorkaroundEnabled()) {
            log.debug("Third-party ad workaround is disabled by configuration.");
            return;
        }

        if (!isVignettePresent(driver)) {
            return;
        }

        log.warn("Google Vignette detected (URL: {}). Dismissing...", driver.getCurrentUrl());

        if (tryEscapeKey(driver) && waitForVignetteGone(driver)) {
            cleanUrlFragment(driver);
            log.info("Vignette dismissed via Escape key.");
            return;
        }

        if (tryClickCloseButton(driver) && waitForVignetteGone(driver)) {
            cleanUrlFragment(driver);
            log.info("Vignette dismissed via Close button.");
            return;
        }

        log.warn("Could not dismiss Vignette via UI. Hiding exact known ad elements via JS.");
        hideKnownAdElements(driver);
        cleanUrlFragment(driver);
    }

    /**
     * Returns true if the ElementClickInterceptedException was caused by a known
     * third-party Google ad element (identified by its id prefix or class).
     *
     * DETECTION POLICY:
     *   Only elements whose ids start with 'aswift_' or 'google_ads_', elements
     *   with class 'adsbygoogle', or 'google-anno-' elements qualify as known ads.
     *   Any other intercepting element is a first-party blocker.
     *
     * @param exception the intercepted click exception
     * @return true if caused by a known Google ad frame
     */
    public static boolean isBlockedByKnownThirdPartyAd(ElementClickInterceptedException exception) {
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("aswift_")
                || message.contains("google_ads_")
                || message.contains("adsbygoogle")
                || message.contains("google-anno-");
    }

    /**
     * Attempts to dismiss the third-party ad that blocked a click.
     *
     * RESOLUTION ORDER:
     *   1. Try clicking the native Close button of the ad.
     *   2. If no close button found, hide only the known ad frames via targeted JS.
     *   3. Never hides arbitrary DOM elements.
     *
     * @param driver the current WebDriver instance
     */
    public static void dismissBlockingThirdPartyAd(WebDriver driver) {
        if (!isWorkaroundEnabled()) {
            throw new IllegalStateException("Third-party ad workaround is disabled by configuration.");
        }

        log.warn("Attempting to dismiss blocking third-party ad...");

        if (isDisplayed(driver, AD_INTENT_CHIP) && tryClick(driver, AD_INTENT_CLOSE)) {
            log.info("Google ad-intent chip collapsed via its native close control.");
            return;
        }

        if (tryClickCloseButton(driver) && waitForKnownAdElementsGone(driver)) {
            log.info("Third-party ad dismissed via native Close button.");
            return;
        }

        log.info("No Close button found. Hiding known Google ad frames via targeted JS.");
        hideKnownAdElements(driver);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private static boolean isVignettePresent(WebDriver driver) {
        return driver.getCurrentUrl().contains("google_vignette");
    }

    private static boolean tryClickCloseButton(WebDriver driver) {
        for (By locator : KNOWN_AD_CLOSE_LOCATORS) {
            if (tryClick(driver, locator)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryClick(WebDriver driver, By locator) {
        try {
            WebElement button = new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            button.click();
            log.debug("Clicked Close button: {}", locator);
            return true;
        } catch (TimeoutException | StaleElementReferenceException e) {
            log.debug("Close button locator not usable: {}", locator, e);
            return false;
        }
    }

    private static boolean isDisplayed(WebDriver driver, By locator) {
        return driver.findElements(locator).stream().anyMatch(AdHandler::isDisplayed);
    }

    private static boolean tryEscapeKey(WebDriver driver) {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return true;
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            log.debug("Escape did not dismiss vignette ad.", e);
            return false;
        }
    }

    /**
     * Cleans the #google_vignette fragment from the URL bar using history.replaceState().
     * This only updates the URL — it does not affect the DOM or overlay.
     */
    private static void cleanUrlFragment(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "history.replaceState(null, '', window.location.pathname + window.location.search);"
            );
            log.debug("URL fragment cleaned via history.replaceState.");
        } catch (JavascriptException e) {
            log.debug("Could not clean URL fragment: {}", e.getMessage());
        }
    }

    private static boolean waitForVignetteGone(WebDriver driver) {
        try {
            new WebDriverWait(driver, OVERLAY_GONE_TIMEOUT)
                .until(d -> !d.getCurrentUrl().contains("google_vignette"));
            return true;
        } catch (TimeoutException e) {
            log.debug("Vignette URL fragment still present after wait.", e);
            return false;
        }
    }

    private static boolean waitForKnownAdElementsGone(WebDriver driver) {
        try {
            new WebDriverWait(driver, OVERLAY_GONE_TIMEOUT)
                    .until(d -> d.findElements(KNOWN_AD_ELEMENTS).stream()
                            .noneMatch(AdHandler::isDisplayed));
            return true;
        } catch (TimeoutException e) {
            log.debug("Known ad elements are still visible after close attempt.", e);
            return false;
        }
    }

    /**
     * Hides ONLY the known Google ad frame elements via targeted CSS.
     * Does NOT touch arbitrary divs, application modals, or layout styles.
     */
    private static void hideKnownAdElements(WebDriver driver) {
        if (driver.findElements(KNOWN_AD_ELEMENTS).isEmpty()) {
            throw new IllegalStateException(
                    "No known Google ad element was found; refusing to modify unrelated DOM.");
        }

        ((JavascriptExecutor) driver).executeScript(HIDE_KNOWN_AD_ELEMENTS_JS);
        if (!waitForKnownAdElementsGone(driver)) {
            throw new IllegalStateException("Known Google ad elements remained visible after workaround.");
        }
        log.info("Known Google ad frames hidden via targeted JS.");
    }

    private static boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }
}
