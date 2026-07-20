package com.automationexercise.tests.subscription;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.pages.CartPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * SubscriptionTest – Automated tests for TC-AE-010 và TC-AE-011.
 *
 * TC-AE-010: Verify Subscription in home page
 *   Scroll to footer → verify "SUBSCRIPTION" text → enter email → verify success.
 *
 * TC-AE-011: Verify Subscription in Cart page
 *   Navigate to Cart → scroll to footer → verify "SUBSCRIPTION" → enter email → verify success.
 *
 * NOTE VỀ SUBSCRIPTION EMAIL:
 * Dùng random email để tránh "already subscribed" error nếu chạy nhiều lần.
 */
@Listeners(TestListener.class)
@Epic("Subscription")
@Feature("Footer Subscription")
public class SubscriptionTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionTest.class);

    // =====================================================================
    // TC-AE-010: Verify Subscription in home page
    // =====================================================================

    @Test(
        description = "TC-AE-010 - Verify Subscription in home page",
        groups = {"regression", "subscription", "positive"}
    )
    @Story("Home Page Subscription")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Scrolls to footer on home page, verifies SUBSCRIPTION text, " +
        "enters email and clicks subscribe, then verifies success message."
    )
    public void verifySubscriptionOnHomePage() {
        log.info("TC-AE-010 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Scroll down to footer
        homePage.goToBottom();

        // Step 5: Verify text 'SUBSCRIPTION'
        Assert.assertTrue(homePage.getFooterSubscription().isSubscriptionVisible(),
                "FAIL: 'SUBSCRIPTION' text should be visible in footer");

        // Step 6: Enter email address and click arrow button
        String subscribeEmail = RandomDataUtils.generateUniqueEmail();
        homePage.getFooterSubscription().enterSubscriptionEmail(subscribeEmail)
                .clickSubscribeButton();

        // Step 7: Verify success message
        Assert.assertTrue(homePage.getFooterSubscription().isSubscribeSuccessVisible(),
                "FAIL: 'You have been successfully subscribed!' should be visible");

        log.info("TC-AE-010 PASS | Email: {}", subscribeEmail);
    }

    // =====================================================================
    // TC-AE-011: Verify Subscription in Cart page
    // =====================================================================

    @Test(
        description = "TC-AE-011 - Verify Subscription in Cart page",
        groups = {"regression", "subscription", "positive"}
    )
    @Story("Cart Page Subscription")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Navigates to Cart page, scrolls to footer, verifies SUBSCRIPTION text, " +
        "enters email and clicks subscribe, then verifies success message."
    )
    public void verifySubscriptionOnCartPage() {
        log.info("TC-AE-011 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Click 'Cart' button
        CartPage cartPage = homePage.getHeader().clickCart();

        // Step 5: Scroll down to footer
        cartPage.goToBottom();

        // Step 6: Verify text 'SUBSCRIPTION'
        Assert.assertTrue(cartPage.getFooterSubscription().isSubscriptionVisible(),
                "FAIL: 'SUBSCRIPTION' text should be visible in Cart page footer");

        // Step 7: Enter email address and click arrow button
        String subscribeEmail = RandomDataUtils.generateUniqueEmail();
        cartPage.getFooterSubscription().enterSubscriptionEmail(subscribeEmail)
                .clickSubscribeButton();

        // Step 8: Verify success message
        Assert.assertTrue(cartPage.getFooterSubscription().isSubscribeSuccessVisible(),
                "FAIL: 'You have been successfully subscribed!' should be visible on Cart page");

        log.info("TC-AE-011 PASS | Email: {}", subscribeEmail);
    }
}
