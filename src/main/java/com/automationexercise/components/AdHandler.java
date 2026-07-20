package com.automationexercise.components;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
 * The demo site injects third-party vignette ads outside the application UI.
 * The workaround is deliberately scoped to known Google ad frames only.
 */
public final class AdHandler {

    private static final Logger log = LoggerFactory.getLogger(AdHandler.class);

    private static final Duration CLOSE_BUTTON_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration OVERLAY_GONE_TIMEOUT = Duration.ofSeconds(3);

    // -----------------------------------------------------------------
    // Vignette close button locators (Type 1)
    // -----------------------------------------------------------------

    private static final By[] VIGNETTE_CLOSE_LOCATORS = {
        By.xpath("//button[contains(@aria-label,'Close') or contains(@aria-label,'close')]"),
        By.xpath("//div[contains(@id,'dismiss')]//button"),
        By.xpath("//*[text()='Close'][@role='button' or self::button]"),
        By.id("dismiss-button"),
        By.cssSelector("span#dismiss-button"),
    };

    // -----------------------------------------------------------------
    // Known third-party ad close button locators (Type 2)
    // -----------------------------------------------------------------

    private static final By[] THIRD_PARTY_AD_CLOSE_LOCATORS = {
        By.xpath("//div[contains(text(),'Close') and not(ancestor::iframe)]"),
        By.xpath("//p[contains(text(),'Close') and not(ancestor::iframe)]"),
        By.xpath("//*[normalize-space(text())='Close'][not(ancestor::iframe)]"),
        By.xpath("//a[normalize-space(text())='Close']"),
    };

    /**
     * JavaScript that hides only the known Google ad iframe elements.
     *
     * WHY ONLY THESE SELECTORS:
     *   Broader selectors that target arbitrary divs or iframes risk hiding
     *   application-owned elements such as modals, loading overlays, or dialogs.
     *   We must target only the proven third-party ad containers.
     */
    private static final String HIDE_KNOWN_AD_FRAMES_JS =
        "var adFrames = document.querySelectorAll(" +
        "  'iframe[id^=\"aswift_\"], iframe[id^=\"google_ads_\"], ins.adsbygoogle'" +
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
        if (!isVignettePresent(driver)) {
            return;
        }

        log.warn("Google Vignette detected (URL: {}). Dismissing...", driver.getCurrentUrl());

        // Strategy 1: Escape key
        if (tryEscapeKey(driver)) {
            waitForVignetteGone(driver);
            cleanUrlFragment(driver);
            log.info("Vignette dismissed via Escape key.");
            return;
        }

        // Strategy 2: Click a known close button
        if (tryClickCloseButton(driver, VIGNETTE_CLOSE_LOCATORS)) {
            waitForVignetteGone(driver);
            cleanUrlFragment(driver);
            log.info("Vignette dismissed via Close button.");
            return;
        }

        // Strategy 3: JS hide of known ad frames only (not the whole DOM)
        log.warn("Could not dismiss Vignette via UI. Hiding known ad frames via JS.");
        hideKnownAdFrames(driver);
        cleanUrlFragment(driver);
    }

    /**
     * Returns true if the ElementClickInterceptedException was caused by a known
     * third-party Google ad element (identified by its id prefix or class).
     *
     * DETECTION POLICY:
     *   Only elements whose ids start with 'aswift_' or 'google_ads_', or
     *   elements with class 'adsbygoogle', qualify as known third-party ads.
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
                || message.contains("adsbygoogle");
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
        log.warn("Attempting to dismiss blocking third-party ad...");

        if (tryClickCloseButton(driver, THIRD_PARTY_AD_CLOSE_LOCATORS)) {
            log.info("Third-party ad dismissed via native Close button.");
            return;
        }

        // Fallback: hide only the known Google ad frame elements
        log.info("No Close button found. Hiding known Google ad frames via targeted JS.");
        hideKnownAdFrames(driver);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private static boolean isVignettePresent(WebDriver driver) {
        try {
            return driver.getCurrentUrl().contains("google_vignette");
        } catch (org.openqa.selenium.WebDriverException e) {
            return false;
        }
    }

    private static boolean tryClickCloseButton(WebDriver driver, By[] locators) {
        for (By locator : locators) {
            try {
                WebElement btn = new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
                btn.click();
                log.debug("Clicked Close button: {}", locator);
                return true;
            } catch (org.openqa.selenium.WebDriverException e) {
                log.debug("Close button locator not usable: {}", locator, e);
            }
        }
        return false;
    }

    private static boolean tryEscapeKey(WebDriver driver) {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                .until(d -> !d.getCurrentUrl().contains("google_vignette"));
            return true;
        } catch (org.openqa.selenium.WebDriverException e) {
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
        } catch (org.openqa.selenium.WebDriverException e) {
            log.debug("Could not clean URL fragment: {}", e.getMessage());
        }
    }

    private static void waitForVignetteGone(WebDriver driver) {
        try {
            new WebDriverWait(driver, OVERLAY_GONE_TIMEOUT)
                .until(d -> !d.getCurrentUrl().contains("google_vignette"));
        } catch (org.openqa.selenium.TimeoutException e) {
            log.debug("Vignette URL fragment still present after wait.", e);
        }
    }

    /**
     * Hides ONLY the known Google ad frame elements via targeted CSS.
     * Does NOT touch arbitrary divs, application modals, or layout styles.
     */
    private static void hideKnownAdFrames(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(HIDE_KNOWN_AD_FRAMES_JS);
            log.info("Known Google ad frames hidden via targeted JS.");
        } catch (org.openqa.selenium.WebDriverException e) {
            log.warn("Could not hide known ad frames: {}", e.getMessage());
        }
    }
}
