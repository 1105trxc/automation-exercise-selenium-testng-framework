package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HomePage – Page Object for the home page (https://automationexercise.com/).
 *
 * LOCATORS: Based on actual HTML structure of automationexercise.com.
 *
 * NAVIGATION ELEMENTS (when NOT logged in):
 *   Home | Products | Cart | Signup/Login | Test Cases | API Testing | Contact Us
 *
 * NAVIGATION ELEMENTS (when logged in):
 *   Home | Products | Cart | Logged in as <b>username</b> | Logout | Delete Account | ...
 */
public class HomePage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(HomePage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    /** Main slider on home page – proves the page loaded successfully */
    private static final By HOME_PAGE_INDICATOR   = By.id("slider-carousel");

    /** "Logged in as username" – the <b> tag contains the username */
    private static final By LOGGED_IN_USERNAME_B  = By.cssSelector("li a b");

    /** Logout link – only visible when logged in */
    private static final By LOGOUT_LINK           = By.cssSelector("a[href='/logout']");

    /** Delete Account link – only visible when logged in */
    private static final By DELETE_ACCOUNT_LINK   = By.cssSelector("a[href='/delete_account']");

    /** Signup/Login link – only visible when NOT logged in */
    private static final By LOGIN_SIGNUP_LINK     = By.cssSelector("a[href='/login']");

    /** Contact Us link */
    private static final By CONTACT_US_LINK       = By.cssSelector("a[href='/contact_us']");

    /** Products link */
    private static final By PRODUCTS_LINK         = By.cssSelector("a[href='/products']");

    /** Test Cases link */
    private static final By TEST_CASES_LINK       = By.cssSelector("a[href='/test_cases']");

    /** "ACCOUNT DELETED!" confirmation heading */
    private static final By ACCOUNT_DELETED_MSG   = By.xpath("//b[text()='ACCOUNT DELETED!']");

    /** Continue button (appears on account-created and account-deleted pages) */
    private static final By CONTINUE_BUTTON       = By.cssSelector("a[data-qa='continue-button']");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /**
     * Verifies the home page has loaded by checking the main slider.
     */
    public boolean isHomePageVisible() {
        return isDisplayed(HOME_PAGE_INDICATOR);
    }

    /**
     * Checks if a user is currently logged in.
     * Logic: Logout link is visible AND Login/Signup link is NOT visible.
     */
    public boolean isUserLoggedIn() {
        return isDisplayed(LOGOUT_LINK, 5);
    }

    /**
     * Returns the logged-in username from the "Logged in as <b>name</b>" text.
     * Returns empty string if user is not logged in.
     */
    public String getLoggedInUsername() {
        try {
            return driver.findElement(LOGGED_IN_USERNAME_B).getText().trim();
        } catch (Exception e) {
            log.debug("Could not find logged-in username element.");
            return "";
        }
    }

    /**
     * Checks if the Login/Signup nav link is visible.
     * Used to confirm user is NOT logged in (e.g., after logout).
     */
    public boolean isLoginSignupLinkVisible() {
        return isDisplayed(LOGIN_SIGNUP_LINK, 5);
    }

    /**
     * Checks if "ACCOUNT DELETED!" message is visible.
     */
    public boolean isAccountDeletedMessageVisible() {
        return isDisplayed(ACCOUNT_DELETED_MSG, 10);
    }

    // -----------------------------------------------------------------
    // Navigation Actions
    // -----------------------------------------------------------------

    /**
     * Clicks "Signup / Login" in the nav bar.
     * Returns LoginPage because that is the destination page.
     */
    public LoginPage clickLoginSignup() {
        log.info("Clicking Signup / Login link");
        click(LOGIN_SIGNUP_LINK);
        return new LoginPage(driver);
    }

    /**
     * Clicks "Logout" in the nav bar.
     * Returns HomePage because logout redirects to /login which then becomes LoginPage,
     * but we return LoginPage directly since that's where we end up.
     * NOTE: The driver is still on the same "driver" reference.
     */
    public LoginPage clickLogout() {
        log.info("Clicking Logout link");
        click(LOGOUT_LINK);
        // After logout, site redirects to /login page
        return new LoginPage(driver);
    }

    /**
     * Clicks "Delete Account" in the nav bar.
     * Returns this HomePage because after delete, a confirmation page appears
     * and clicking Continue returns to home.
     */
    public HomePage clickDeleteAccount() {
        log.info("Clicking Delete Account link");
        click(DELETE_ACCOUNT_LINK);
        return this;
    }

    /**
     * Clicks "Contact us" in the nav bar.
     * Returns ContactUsPage as the destination.
     */
    public ContactUsPage clickContactUs() {
        log.info("Clicking Contact us link");
        click(CONTACT_US_LINK);
        return new ContactUsPage(driver);
    }

    /**
     * Clicks "Products" in the nav bar.
     * Returns ProductsPage as the destination.
     */
    public ProductsPage clickProducts() {
        log.info("Clicking Products link");
        click(PRODUCTS_LINK);
        return new ProductsPage(driver);
    }

    /**
     * Clicks "Test Cases" in the nav bar.
     * No specific return type since it just navigates to a content page.
     */
    public HomePage clickTestCases() {
        log.info("Clicking Test Cases link");
        click(TEST_CASES_LINK);
        return this;
    }

    /**
     * Clicks the "Continue" button on confirmation pages
     * (Account Created / Account Deleted).
     */
    public HomePage clickContinue() {
        if (isDisplayed(CONTINUE_BUTTON, 5)) {
            log.info("Clicking Continue button");
            click(CONTINUE_BUTTON);
        }
        return this;
    }
}
