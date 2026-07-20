package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountDeletedPage – Page Object for the "ACCOUNT DELETED!" confirmation state.
 *
 * This page appears after HeaderComponent.clickDeleteAccount() succeeds at /delete_account.
 */
public class AccountDeletedPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletedPage.class);

    private static final By ACCOUNT_DELETED_HEADING = By.xpath("//h2[contains(@class,'title')]//b");
    private static final By CONTINUE_BUTTON         = By.cssSelector("a[data-qa='continue-button']");

    public AccountDeletedPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Confirms the "ACCOUNT DELETED!" heading is visible.
     * Assert this in the test after receiving this page.
     */
    public boolean isAccountDeletedVisible() {
        return isDisplayed(ACCOUNT_DELETED_HEADING, 10);
    }

    /**
     * Clicks "Continue" to return to the HomePage after account deletion.
     *
     * @return HomePage
     */
    public HomePage clickContinue() {
        log.info("Clicking Continue after account deletion");
        click(CONTINUE_BUTTON);
        return new HomePage(driver);
    }
}
