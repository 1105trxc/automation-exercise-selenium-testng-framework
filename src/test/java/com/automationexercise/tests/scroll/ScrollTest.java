package com.automationexercise.tests.scroll;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.pages.HomePage;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * ScrollTest – Automated tests for TC-AE-025 và TC-AE-026.
 *
 * TC-AE-025: Verify Scroll Up using 'Arrow' button and Scroll Down functionality
 *   Scroll down → verify "SUBSCRIPTION" visible → click scroll-up arrow → verify hero text visible.
 *
 * TC-AE-026: Verify Scroll Up without 'Arrow' button and Scroll Down functionality
 *   Scroll down → verify "SUBSCRIPTION" visible → press PAGE_UP key → verify hero text visible.
 *
 * SCROLL UP BUTTON (TC-025):
 * Website dùng jQuery ScrollUp plugin. Button id="scrollUp" xuất hiện sau khi scroll down.
 * Button ẩn mặc định, chỉ visible khi đã scroll 1 khoảng nhất định.
 *
 * HERO TEXT (verify after scroll up):
 * "Full-Fledged practice website for Automation Engineers"
 * Element: //div[@class='item active']//h2[contains(text(),'Full-Fledged')]
 *
 * TIMING CONSIDERATION:
 * Sau khi click scroll button hoặc PAGE_UP, cần đợi animation hoàn tất.
 * BasePage.waitForPageLoad() hoặc small wait để animation kết thúc.
 */
@Listeners(TestListener.class)
@Epic("UI")
@Feature("Scroll Functionality")
public class ScrollTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ScrollTest.class);

    // =====================================================================
    // TC-AE-025: Verify Scroll Up using Arrow button
    // =====================================================================

    @Test(
        description = "TC-AE-025 - Verify Scroll Up using 'Arrow' button",
        groups = {"regression", "scroll", "positive"}
    )
    @Story("Scroll Up with Arrow Button")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Scrolls down to bottom of home page, verifies 'SUBSCRIPTION' text is visible. " +
        "Clicks the scroll-up arrow button at bottom-right, " +
        "then verifies 'Full-Fledged practice website for Automation Engineers' " +
        "text is visible at top of page."
    )
    public void scrollUpUsingArrowButton() {
        log.info("TC-AE-025 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Scroll down page to bottom
        homePage.goToBottom();
        log.info("TC-AE-025 | Scrolled to bottom");

        // Step 5: Verify 'SUBSCRIPTION' is visible
        Assert.assertTrue(homePage.isSubscriptionVisible(),
                "FAIL: 'SUBSCRIPTION' text should be visible after scrolling to bottom");

        // Step 6: Click on arrow at bottom right side to move upward
        homePage.clickScrollUpButton();
        log.info("TC-AE-025 | Scroll up arrow clicked");

        // Wait for scroll animation to complete
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Step 7: Verify page is scrolled up and hero text visible
        Assert.assertTrue(homePage.isHeroTextVisible(),
                "FAIL: Hero text 'Full-Fledged practice website...' should be visible " +
                "after clicking scroll-up button");

        log.info("TC-AE-025 PASS | Page scrolled up successfully via arrow button");
    }

    // =====================================================================
    // TC-AE-026: Verify Scroll Up without Arrow button (PAGE_UP key)
    // =====================================================================

    @Test(
        description = "TC-AE-026 - Verify Scroll Up without 'Arrow' button",
        groups = {"regression", "scroll", "positive"}
    )
    @Story("Scroll Up with Keyboard")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Scrolls down to bottom of home page, verifies 'SUBSCRIPTION' text is visible. " +
        "Uses PAGE_UP keyboard key (not the arrow button) to scroll up, " +
        "then verifies 'Full-Fledged practice website for Automation Engineers' " +
        "text is visible at top of page."
    )
    public void scrollUpWithoutArrowButton() {
        log.info("TC-AE-026 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Scroll down page to bottom
        homePage.goToBottom();
        log.info("TC-AE-026 | Scrolled to bottom");

        // Step 5: Verify 'SUBSCRIPTION' is visible
        Assert.assertTrue(homePage.isSubscriptionVisible(),
                "FAIL: 'SUBSCRIPTION' text should be visible after scrolling to bottom");

        // Step 6: Scroll up page to top using PAGE_UP key
        homePage.scrollUpWithPageUpKey();
        log.info("TC-AE-026 | PAGE_UP key pressed");

        // Wait for scroll to complete
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Step 7: Verify page is scrolled up and hero text visible
        Assert.assertTrue(homePage.isHeroTextVisible(),
                "FAIL: Hero text 'Full-Fledged practice website...' should be visible " +
                "after pressing PAGE_UP key");

        log.info("TC-AE-026 PASS | Page scrolled up successfully via PAGE_UP key");
    }
}
