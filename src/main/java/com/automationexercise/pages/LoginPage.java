package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoginPage – Page Object for /login page.
 *
 * IMPORTANT: The /login page contains TWO separate forms:
 *
 *   LEFT SIDE – Login form ("Login to your account"):
 *     - Email address input
 *     - Password input
 *     - Login button
 *
 *   RIGHT SIDE – New user signup form ("New User Signup!"):
 *     - Name input
 *     - Email address input
 *     - Signup button (→ navigates to /signup for full registration)
 *
 * This is why LoginPage has methods for BOTH login AND starting signup.
 */
public class LoginPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);

    // -----------------------------------------------------------------
    // Login Form Locators
    // -----------------------------------------------------------------

    private static final By LOGIN_HEADING   = By.xpath("//h2[normalize-space()='Login to your account']");
    private static final By LOGIN_EMAIL     = By.cssSelector("input[data-qa='login-email']");
    private static final By LOGIN_PASSWORD  = By.cssSelector("input[data-qa='login-password']");
    private static final By LOGIN_BUTTON    = By.cssSelector("button[data-qa='login-button']");

    /** Error message: "Your email or password is incorrect!" */
    private static final By LOGIN_ERROR     = By.xpath("//p[contains(@style,'color: red')]");

    // -----------------------------------------------------------------
    // Signup Form Locators (right side of /login page)
    // -----------------------------------------------------------------

    private static final By SIGNUP_HEADING  = By.xpath("//h2[normalize-space()='New User Signup!']");
    private static final By SIGNUP_NAME     = By.cssSelector("input[data-qa='signup-name']");
    private static final By SIGNUP_EMAIL    = By.cssSelector("input[data-qa='signup-email']");
    private static final By SIGNUP_BUTTON   = By.cssSelector("button[data-qa='signup-button']");

    /** Error message: "Email Address already exist!" */
    private static final By EMAIL_EXISTS_ERROR = By.xpath("//p[contains(text(),'Email Address already exist!')]");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Confirms we are on the login page. */
    public boolean isLoginPageVisible() {
        return isDisplayed(LOGIN_HEADING);
    }

    /** Confirms the "New User Signup!" section is visible. */
    public boolean isSignupSectionVisible() {
        return isDisplayed(SIGNUP_HEADING);
    }

    /** Checks if login error message is displayed. */
    public boolean isLoginErrorVisible() {
        return isDisplayed(LOGIN_ERROR);
    }

    /** Returns the login error message text. */
    public String getLoginErrorMessage() {
        return getText(LOGIN_ERROR);
    }

    /** Checks if "Email Address already exist!" error is shown. */
    public boolean isEmailExistsErrorVisible() {
        return isDisplayed(EMAIL_EXISTS_ERROR);
    }

    /** Returns the "email already exists" error text. */
    public String getEmailExistsErrorMessage() {
        return getText(EMAIL_EXISTS_ERROR);
    }

    // -----------------------------------------------------------------
    // Login Actions
    // -----------------------------------------------------------------

    /**
     * Fluent API: each method returns 'this' (LoginPage)
     * so you can chain: loginPage.enterEmail(e).enterPassword(p).clickLogin()
     */
    public LoginPage enterLoginEmail(String email) {
        log.info("Entering login email");
        type(LOGIN_EMAIL, email);
        return this;
    }

    public LoginPage enterLoginPassword(String password) {
        log.info("Entering login password");
        type(LOGIN_PASSWORD, password);
        return this;
    }

    /**
     * Clicks Login and returns HomePage (successful login navigates to home).
     * Use this when you EXPECT login to succeed.
     */
    public HomePage clickLoginButton() {
        log.info("Clicking Login button");
        click(LOGIN_BUTTON);
        return new HomePage(driver);
    }

    /**
     * Clicks Login and returns LoginPage (stay on same page after failure).
     * Use this when you EXPECT login to FAIL (negative test case).
     */
    public LoginPage clickLoginButtonExpectingFailure() {
        log.info("Clicking Login button (expecting failure)");
        click(LOGIN_BUTTON);
        return this;
    }

    // -----------------------------------------------------------------
    // Signup Actions (Step 1 – entering name + email)
    // -----------------------------------------------------------------

    public LoginPage enterSignupName(String name) {
        log.info("Entering signup name: {}", name);
        type(SIGNUP_NAME, name);
        return this;
    }

    public LoginPage enterSignupEmail(String email) {
        log.info("Entering signup email");
        type(SIGNUP_EMAIL, email);
        return this;
    }

    /**
     * Clicks "Signup" and navigates to the full registration form (/signup).
     * Returns SignupPage because that's where we land.
     */
    public SignupPage clickSignupButton() {
        log.info("Clicking Signup button → navigating to account details form");
        click(SIGNUP_BUTTON);
        return new SignupPage(driver);
    }

    /**
     * Clicks "Signup" when the email is already registered.
     * Stays on LoginPage because no navigation occurs.
     */
    public LoginPage clickSignupButtonExpectingError() {
        log.info("Clicking Signup button (expecting email-exists error)");
        click(SIGNUP_BUTTON);
        return this;
    }

}
