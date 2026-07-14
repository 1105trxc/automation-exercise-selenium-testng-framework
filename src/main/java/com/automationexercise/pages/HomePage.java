package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HomePage – Page Object for the home page (https://automationexercise.com/).
 *
 * Test Cases:
 * - TC-AE-001 to TC-AE-008: Basic nav, login, register
 * - TC-AE-010: Verify Subscription in home page (footer form)
 * - TC-AE-022: Add to cart from Recommended items
 * - TC-AE-025: Verify Scroll Up using Arrow button
 * - TC-AE-026: Verify Scroll Up without Arrow button (PAGE_UP key)
 *
 * NAV ELEMENTS (when NOT logged in):
 *   Home | Products | Cart | Signup/Login | Test Cases | API Testing | Contact Us
 *
 * NAV ELEMENTS (when logged in):
 *   Home | Products | Cart | Logged in as <b>username</b> | Logout | Delete Account | ...
 */
public class HomePage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(HomePage.class);

    // -----------------------------------------------------------------
    // Locators – Nav bar
    // -----------------------------------------------------------------

    /** Main slider on home page – proves the page loaded */
    private static final By HOME_PAGE_INDICATOR  = By.id("slider-carousel");

    /** "Logged in as username" – the <b> tag contains the username */
    private static final By LOGGED_IN_USERNAME_B = By.cssSelector("li a b");

    /** Logout link – only visible when logged in */
    private static final By LOGOUT_LINK          = By.cssSelector("a[href='/logout']");

    /** Delete Account link */
    private static final By DELETE_ACCOUNT_LINK  = By.cssSelector("a[href='/delete_account']");

    /** Signup/Login link – only visible when NOT logged in */
    private static final By LOGIN_SIGNUP_LINK    = By.cssSelector("a[href='/login']");

    /** Contact Us link */
    private static final By CONTACT_US_LINK      = By.cssSelector("a[href='/contact_us']");

    /** Products link */
    private static final By PRODUCTS_LINK        = By.cssSelector("a[href='/products']");

    /** Test Cases link */
    private static final By TEST_CASES_LINK      = By.cssSelector("a[href='/test_cases']");

    /** Cart link */
    private static final By CART_LINK            = By.cssSelector("a[href='/view_cart']");

    /** "ACCOUNT DELETED!" confirmation heading */
    private static final By ACCOUNT_DELETED_MSG  = By.xpath("//h2[contains(@class,'title')]//b");

    /** Continue button (account-created and account-deleted pages) */
    private static final By CONTINUE_BUTTON      = By.cssSelector("a[data-qa='continue-button']");

    // -----------------------------------------------------------------
    // Locators – Footer Subscription (TC-010)
    // -----------------------------------------------------------------

    /** "SUBSCRIPTION" heading ở footer */
    private static final By SUBSCRIPTION_HEADING = By.xpath("//h2[normalize-space()='Subscription']");

    /** Email input trong subscription form */
    private static final By SUBSCRIBE_EMAIL_INPUT = By.id("susbscribe_email");

    /** Arrow button để submit subscription */
    private static final By SUBSCRIBE_BUTTON = By.id("subscribe");

    /** Success message sau khi subscribe */
    private static final By SUBSCRIBE_SUCCESS_MSG = By.cssSelector("div.alert-success");

    // -----------------------------------------------------------------
    // Locators – Recommended Items (TC-022)
    // -----------------------------------------------------------------

    /** Recommended items carousel section */
    private static final By RECOMMENDED_SECTION = By.id("recommended-item-carousel");

    /**
     * "Add to cart" button của sản phẩm đầu tiên trong Recommended items.
     * Carousel có thể chứa nhiều items; dùng item đầu tiên visible.
     */
    private static final By FIRST_RECOMMENDED_ADD_TO_CART =
            By.xpath("(//div[@id='recommended-item-carousel']//a[contains(@class,'add-to-cart')])[1]");

    /** "View Cart" link trong modal sau khi add from recommended */
    private static final By VIEW_CART_MODAL_LINK =
            By.cssSelector(".modal-body a[href='/view_cart']");

    // -----------------------------------------------------------------
    // Locators – Scroll (TC-025, TC-026)
    // -----------------------------------------------------------------

    /**
     * Scroll-to-top arrow button (xuất hiện ở bottom-right sau khi scroll down).
     * CSS: #scrollUp (jQuery ScrollUp plugin)
     */
    private static final By SCROLL_UP_BUTTON = By.id("scrollUp");

    /**
     * Hero text ở đầu trang – verify sau khi scroll up thành công.
     * "Full-Fledged practice website for Automation Engineers"
     */
    private static final By HERO_TEXT = By.xpath(
            "//div[contains(@class,'item active')]//h2[contains(text(),'Full-Fledged')]");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Verifies the home page has loaded by checking the main slider. */
    public boolean isHomePageVisible() {
        return isDisplayed(HOME_PAGE_INDICATOR);
    }

    /**
     * Checks if a user is currently logged in.
     * Logic: Logout link is visible.
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

    /** Click "Signup / Login" in the nav bar → LoginPage */
    public LoginPage clickLoginSignup() {
        log.info("Clicking Signup / Login link");
        jsClick(LOGIN_SIGNUP_LINK);
        return new LoginPage(driver);
    }

    /** Click "Logout" → LoginPage */
    public LoginPage clickLogout() {
        log.info("Clicking Logout link");
        jsClick(LOGOUT_LINK);
        return new LoginPage(driver);
    }

    /** Click "Delete Account" → stays on HomePage (confirmation appears) */
    public HomePage clickDeleteAccount() {
        log.info("Clicking Delete Account link");
        jsClick(DELETE_ACCOUNT_LINK);
        handleVignette();
        return this;
    }

    /** Click "Contact us" → ContactUsPage */
    public ContactUsPage clickContactUs() {
        log.info("Clicking Contact us link");
        click(CONTACT_US_LINK);
        return new ContactUsPage(driver);
    }

    /** Click "Products" in nav → ProductsPage */
    public ProductsPage clickProducts() {
        log.info("Clicking Products link");
        jsClick(PRODUCTS_LINK);
        return new ProductsPage(driver);
    }

    /** Click "Test Cases" in nav */
    public HomePage clickTestCases() {
        log.info("Clicking Test Cases link");
        jsClick(TEST_CASES_LINK);
        return this;
    }

    /**
     * Click "Cart" in nav → CartPage.
     * Dùng cho TC-011, TC-012, TC-014, TC-015, TC-016, TC-020, TC-022, TC-023, TC-024.
     */
    public CartPage clickCart() {
        log.info("Clicking Cart link");
        jsClick(CART_LINK);
        return new CartPage(driver);
    }

    /** Click "Continue" on confirmation pages */
    public HomePage clickContinue() {
        if (isDisplayed(CONTINUE_BUTTON, 5)) {
            log.info("Clicking Continue button");
            click(CONTINUE_BUTTON);
        }
        return this;
    }

    // -----------------------------------------------------------------
    // Subscription – Footer (TC-010)
    // -----------------------------------------------------------------

    /** Xác nhận "SUBSCRIPTION" heading visible ở footer */
    public boolean isSubscriptionVisible() {
        return isDisplayed(SUBSCRIPTION_HEADING, 5);
    }

    /**
     * Nhập email vào subscription form ở footer.
     * Gọi scrollToFooter() trước khi gọi method này.
     */
    public HomePage enterSubscriptionEmail(String email) {
        log.info("Entering subscription email");
        type(SUBSCRIBE_EMAIL_INPUT, email);
        return this;
    }

    /** Click arrow button để submit subscription */
    public HomePage clickSubscribeButton() {
        log.info("Clicking subscribe button");
        click(SUBSCRIBE_BUTTON);
        return this;
    }

    /** Verify "You have been successfully subscribed!" */
    public boolean isSubscribeSuccessVisible() {
        return isDisplayed(SUBSCRIBE_SUCCESS_MSG, 5);
    }

    // -----------------------------------------------------------------
    // Recommended Items (TC-022)
    // -----------------------------------------------------------------

    /**
     * Xác nhận "RECOMMENDED ITEMS" section visible.
     * Cần scrollToBottom() trước khi gọi method này.
     */
    public boolean isRecommendedItemsVisible() {
        return isDisplayed(RECOMMENDED_SECTION, 5);
    }

    /**
     * Click "Add to Cart" của sản phẩm đầu tiên trong Recommended Items section.
     * Returns this (HomePage) vì modal xuất hiện overlay trang hiện tại.
     */
    public HomePage clickAddFirstRecommendedToCart() {
        log.info("Clicking Add to Cart on first recommended item");
        click(FIRST_RECOMMENDED_ADD_TO_CART);
        return this;
    }

    /**
     * Click "View Cart" từ modal sau khi add recommended item.
     * Returns CartPage.
     */
    public CartPage clickViewCartFromModal() {
        log.info("Clicking View Cart from modal");
        click(VIEW_CART_MODAL_LINK);
        return new CartPage(driver);
    }

    // -----------------------------------------------------------------
    // Scroll Actions (TC-025, TC-026)
    // -----------------------------------------------------------------

    /**
     * Click scroll-to-top arrow button (TC-025).
     * Button xuất hiện ở bottom-right sau khi scroll down.
     * Cần scrollToBottom() trước khi gọi method này.
     */
    public HomePage clickScrollUpButton() {
        log.info("Clicking scroll-up arrow button");
        jsClick(SCROLL_UP_BUTTON);
        return this;
    }

    /**
     * Scroll lên đầu trang bằng phím PAGE_UP (TC-026).
     * Không dùng scroll button – dùng keyboard thay thế.
     */
    public HomePage scrollUpWithPageUpKey() {
        log.info("Pressing HOME key to scroll up");
        driver.findElement(By.tagName("body")).sendKeys(Keys.HOME);
        return this;
    }

    /**
     * Xác nhận hero text "Full-Fledged practice website for Automation Engineers" visible.
     * Dùng để verify đã scroll về đầu trang thành công.
     */
    public boolean isHeroTextVisible() {
        return isDisplayed(HERO_TEXT, 5);
    }

    // -----------------------------------------------------------------
    // Scroll helpers (public wrappers for BasePage protected methods)
    // -----------------------------------------------------------------

    /** Scroll xuống bottom của trang (dùng trước subscription/recommended/scroll tests) */
    public HomePage goToBottom() {
        scrollToBottom();
        return this;
    }
}