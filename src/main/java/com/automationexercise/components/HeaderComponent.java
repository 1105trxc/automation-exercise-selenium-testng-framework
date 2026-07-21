package com.automationexercise.components;

import com.automationexercise.pages.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HeaderComponent – Encapsulates the navigation bar found on all pages.
 */
public class HeaderComponent extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(HeaderComponent.class);

    // -----------------------------------------------------------------
    // Locators – Nav bar
    // -----------------------------------------------------------------

    private static final By LOGGED_IN_USERNAME_B = By.cssSelector("li a b");
    private static final By LOGOUT_LINK          = By.cssSelector("a[href='/logout']");
    private static final By DELETE_ACCOUNT_LINK  = By.cssSelector("a[href='/delete_account']");
    private static final By LOGIN_SIGNUP_LINK    = By.cssSelector("a[href='/login']");
    private static final By CONTACT_US_LINK      = By.cssSelector("a[href='/contact_us']");
    private static final By PRODUCTS_LINK        = By.cssSelector("a[href='/products']");
    private static final By TEST_CASES_LINK      = By.cssSelector("a[href='/test_cases']");
    private static final By CART_LINK            = By.cssSelector("a[href='/view_cart']");

    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /**
     * Checks if a user is currently logged in.
     * Logic: Logout link is visible.
     */
    public boolean isUserLoggedIn() {
        return isDisplayed(LOGOUT_LINK);
    }

    /** Returns the logged-in username from the "Logged in as <b>name</b>" text. */
    public String getLoggedInUsername() {
        return getText(LOGGED_IN_USERNAME_B);
    }

    /**
     * Checks if the Login/Signup nav link is visible.
     * Used to confirm user is NOT logged in (e.g., after logout).
     */
    public boolean isLoginSignupLinkVisible() {
        return isDisplayed(LOGIN_SIGNUP_LINK);
    }

    // -----------------------------------------------------------------
    // Navigation Actions
    // -----------------------------------------------------------------

    /** Click "Signup / Login" in the nav bar → LoginPage */
    public LoginPage clickLoginSignup() {
        log.info("Clicking Signup / Login link in Header");
        click(LOGIN_SIGNUP_LINK);
        return new LoginPage(driver);
    }

    /** Click "Logout" → LoginPage */
    public LoginPage clickLogout() {
        log.info("Clicking Logout link in Header");
        scrollToTop();
        click(LOGOUT_LINK);
        return new LoginPage(driver);
    }

    /** Click "Delete Account" → AccountDeletedPage */
    public AccountDeletedPage clickDeleteAccount() {
        log.info("Clicking Delete Account link in Header");
        scrollToTop();
        click(DELETE_ACCOUNT_LINK);
        return new AccountDeletedPage(driver);
    }

    /** Click "Contact us" → ContactUsPage */
    public ContactUsPage clickContactUs() {
        log.info("Clicking Contact us link in Header");
        click(CONTACT_US_LINK);
        return new ContactUsPage(driver);
    }

    /** Click "Products" in nav → ProductsPage */
    public ProductsPage clickProducts() {
        log.info("Clicking Products link in Header");
        click(PRODUCTS_LINK);
        return new ProductsPage(driver);
    }

    /** Click "Test Cases" in nav → TestCasesPage */
    public TestCasesPage clickTestCases() {
        log.info("Clicking Test Cases link in Header");
        click(TEST_CASES_LINK);
        return new TestCasesPage(driver);
    }

    /** Click "Cart" in nav → CartPage */
    public CartPage clickCart() {
        log.info("Clicking Cart link in Header");
        click(CART_LINK);
        return new CartPage(driver);
    }
}
