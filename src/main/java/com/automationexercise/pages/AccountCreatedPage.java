package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountCreatedPage – Page Object for the "ACCOUNT CREATED!" confirmation state.
 *
 * This page appears after SignupPage.clickCreateAccount() succeeds at /account_created.
 */
public class AccountCreatedPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(AccountCreatedPage.class);

    private static final By ACCOUNT_CREATED_HEADING = By.xpath("//h2[contains(@class,'title')]//b");
    private static final By CONTINUE_BUTTON         = By.cssSelector("a[data-qa='continue-button']");

    public AccountCreatedPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Confirms the "ACCOUNT CREATED!" heading is visible.
     * Use this assertion in the test immediately after receiving this page.
     */
    public boolean isAccountCreatedVisible() {
        return isDisplayed(ACCOUNT_CREATED_HEADING, 10);
    }

    /**
     * Clicks "Continue" to proceed to the HomePage after account creation.
     *
     * @return HomePage
     */
    public HomePage clickContinue() {
        log.info("Clicking Continue after account creation");
        click(CONTINUE_BUTTON);
        return new HomePage(driver);
    }
}
