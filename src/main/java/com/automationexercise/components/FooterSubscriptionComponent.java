package com.automationexercise.components;

import com.automationexercise.pages.AEBasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FooterSubscriptionComponent – Encapsulates the subscription form found in the footer of all pages.
 */
public class FooterSubscriptionComponent extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(FooterSubscriptionComponent.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------
    private static final By SUBSCRIPTION_HEADING = By.xpath("//h2[normalize-space()='Subscription']");
    private static final By SUBSCRIBE_EMAIL_INPUT = By.id("susbscribe_email");
    private static final By SUBSCRIBE_BUTTON = By.id("subscribe");
    private static final By SUBSCRIBE_SUCCESS_MSG = By.cssSelector("div.alert-success");

    public FooterSubscriptionComponent(WebDriver driver) {
        super(driver);
    }

    /** Xác nhận "SUBSCRIPTION" heading visible ở footer */
    public boolean isSubscriptionVisible() {
        return isDisplayed(SUBSCRIPTION_HEADING, 5);
    }

    /**
     * Nhập email vào subscription form ở footer.
     */
    public FooterSubscriptionComponent enterSubscriptionEmail(String email) {
        log.info("Entering subscription email in Footer");
        type(SUBSCRIBE_EMAIL_INPUT, email);
        return this;
    }

    /** Click arrow button để submit subscription */
    public FooterSubscriptionComponent clickSubscribeButton() {
        log.info("Clicking subscribe button in Footer");
        click(SUBSCRIBE_BUTTON);
        return this;
    }

    /** Verify "You have been successfully subscribed!" */
    public boolean isSubscribeSuccessVisible() {
        return isDisplayed(SUBSCRIBE_SUCCESS_MSG, 5);
    }
}
