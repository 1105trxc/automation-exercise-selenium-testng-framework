package com.automationexercise.pages;

import com.automationexercise.components.AdHandler;
import com.automationexercise.components.AddToCartModal;
import com.automationexercise.components.FooterSubscriptionComponent;
import com.automationexercise.components.HeaderComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AEBasePage – Site-specific base page for automationexercise.com.
 *
 * WHY THIS CLASS EXISTS:
 *   BasePage is generic infrastructure reusable across projects.
 *   AEBasePage adds site-specific behavior: handling third-party Google ads
 *   that appear as full-viewport iframes on automationexercise.com.
 *
 * CLICK CONTRACT:
 *   One retry is permitted ONLY when Selenium throws ElementClickInterceptedException
 *   AND the intercepting element is a proven known third-party ad.
 *
 *   What the generic click() does NOT do:
 *   - No post-click URL inspection to decide whether to click again.
 *   - No automatic re-click after a successful click (risk of double-submit).
 *   - No hiding of first-party overlays (app modals, dialogs, error popups).
 *
 *   Side-effect-free anchors can explicitly opt into destination verification
 *   through clickSideEffectFreeNavigationLink(); submits and destructive links cannot.
 *
 * WHY NOT RE-CLICK AFTER A SUCCESSFUL CLICK:
 *   A generic click override cannot know if the locator represents a navigation link,
 *   a form submit, "Place Order", "Pay", or "Delete Account".
 *   Re-clicking blindly after the click already landed can cause duplicate submissions.
 */
public abstract class AEBasePage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(AEBasePage.class);

    protected AEBasePage(WebDriver driver) {
        super(driver);
    }

    /** Returns the FooterSubscriptionComponent present on all pages. */
    public FooterSubscriptionComponent getFooterSubscription() {
        return new FooterSubscriptionComponent(driver);
    }

    /** Returns the AddToCartModal (appears after adding a product). */
    public AddToCartModal getAddToCartModal() {
        return new AddToCartModal(driver);
    }

    /** Returns the HeaderComponent (navigation bar on all pages). */
    public HeaderComponent getHeader() {
        return new HeaderComponent(driver);
    }

    /**
     * Overrides click() to handle third-party ad interceptions specific to automationexercise.com.
     *
     * POLICY:
     *   1. Attempt native click.
     *   2. If ElementClickInterceptedException is thrown AND it is proven to be caused by
     *      a known third-party ad element (identified by AdHandler), dismiss the ad and
     *      click exactly once more.
     *   3. Any other ElementClickInterceptedException (first-party modal, dialog, overlay)
     *      is re-thrown immediately — the test must fail so we can diagnose the real issue.
     */
    @Override
    protected void click(By locator) {
        try {
            super.click(locator);
        } catch (ElementClickInterceptedException exception) {
            if (!AdHandler.isBlockedByKnownThirdPartyAd(exception)
                    || !AdHandler.isWorkaroundEnabled()) {
                // First-party element is blocking. Re-throw so the test fails honestly.
                throw exception;
            }

            log.warn("Click intercepted by known third-party ad. Dismissing ad, then retrying once.");
            AdHandler.dismissBlockingThirdPartyAd(driver);
            // One controlled retry after a confirmed ad interception
            super.click(locator);
        }
    }

    /**
     * Follows a side-effect-free anchor and verifies that its destination loaded.
     *
     * A Google vignette can accept a native click without allowing the underlying
     * GET navigation to continue. The link is continued once only when the
     * vignette was detected, the destination did not load, and the browser is
     * still at the original URL. Never use this for submits or destructive links.
     */
    protected void clickSideEffectFreeNavigationLink(By locator, String actionName) {
        String sourceUrl = driver.getCurrentUrl();
        String targetUrl = waitUntilClickable(locator).getAttribute("href");
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalStateException(actionName + " link has no href.");
        }

        click(locator);
        boolean vignetteDismissed = AdHandler.dismissLinkTriggeredVignette(driver);

        if (!waitForUrl(targetUrl)) {
            if (!vignetteDismissed || !isAtUrl(sourceUrl)) {
                throw navigationTimeout(actionName, targetUrl);
            }

            log.warn("Google vignette consumed {} navigation. Continuing the link once.", actionName);
            click(locator);
            AdHandler.dismissLinkTriggeredVignette(driver);

            if (!waitForUrl(targetUrl)) {
                throw navigationTimeout(actionName, targetUrl);
            }
        }
    }

    private TimeoutException navigationTimeout(String actionName, String targetUrl) {
        return new TimeoutException(actionName + " did not reach '" + targetUrl
                + "'. Current URL: " + driver.getCurrentUrl());
    }
}
