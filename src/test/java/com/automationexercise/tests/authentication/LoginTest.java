package com.automationexercise.tests.authentication;

import com.automationexercise.base.BaseTest;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.LoginPage;
import com.automationexercise.pages.SignupPage;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * LoginTest – Automated tests for TC-AE-002, TC-AE-003, TC-AE-004.
 *
 * TEST INDEPENDENCE STRATEGY:
 * TC-002 (Login valid) and TC-004 (Logout) require a registered user.
 * Instead of sharing a user between tests (which creates test dependency),
 * each test registers its OWN unique user → performs its action → deletes account.
 *
 * This ensures:
 * - Tests can run in any order
 * - Tests can run in parallel (each has its own user)
 * - No test depends on another test running first
 *
 * PATTERN USED: Arrange → Act → Assert
 */
@Epic("Authentication")
@Feature("Login and Logout")
public class LoginTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(LoginTest.class);

    // Shared registration data (non-sensitive defaults for test users)
    private static final String TEST_PASSWORD   = "Automation@2026";
    private static final String TEST_FIRST_NAME = "Test";
    private static final String TEST_LAST_NAME  = "Automation";
    private static final String TEST_ADDRESS    = "123 QA Street";
    private static final String TEST_COUNTRY    = "United States";
    private static final String TEST_STATE      = "California";
    private static final String TEST_CITY       = "Los Angeles";
    private static final String TEST_ZIPCODE    = "90001";
    private static final String TEST_MOBILE     = "5551234567";

    // =====================================================================
    // TC-AE-002: Login with valid credentials
    // =====================================================================

    @Test(
        description = "TC-AE-002 - Login User with correct email and password",
        groups = {"smoke", "regression", "authentication", "positive"}
    )
    @Story("Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Registers a new user, logs out, then logs in with the registered credentials. " +
        "Verifies 'Logged in as username' is visible. Deletes account after test."
    )
    public void loginWithValidCredentials() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-002 START | Test user email prefix: {}", uniqueEmail.split("@")[0]);

        // Step: Verify home page is visible
        HomePage homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible at start of test");

        // Step: Register a new user (ensures test independence)
        log.info("Step: Registering new test user to prepare login credentials");
        SignupPage signupPage = homePage
                .clickLoginSignup()
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        Assert.assertTrue(signupPage.isAccountInfoVisible(),
                "FAIL: 'Enter Account Information' should be visible after clicking Signup");

        homePage = signupPage
                .fillRegistrationForm(TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME,
                        TEST_ADDRESS, TEST_COUNTRY, TEST_STATE,
                        TEST_CITY, TEST_ZIPCODE, TEST_MOBILE)
                .clickCreateAccount()
                .clickContinue();

        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in after registration");

        // Step: Logout to prepare for the actual login test
        LoginPage loginPage = homePage.clickLogout();

        // ── ACT ──────────────────────────────────────────────────────────
        log.info("TC-AE-002 ACT: Logging in with registered credentials");
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: 'Login to your account' heading should be visible");

        homePage = loginPage
                .enterLoginEmail(uniqueEmail)
                .enterLoginPassword(TEST_PASSWORD)
                .clickLoginButton();

        // ── ASSERT ───────────────────────────────────────────────────────
        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in after submitting valid credentials");

        String loggedInUsername = homePage.getLoggedInUsername();
        Assert.assertFalse(loggedInUsername.isEmpty(),
                "FAIL: 'Logged in as <username>' should be visible in nav");

        log.info("TC-AE-002 PASS | Logged in as: '{}'", loggedInUsername);

        // ── CLEANUP ──────────────────────────────────────────────────────
        // Delete account so this email can't affect other test runs
        homePage.clickDeleteAccount();
        log.info("TC-AE-002 CLEANUP: Account deleted");
    }

    // =====================================================================
    // TC-AE-003: Login with incorrect email and password
    // =====================================================================

    @Test(
        description = "TC-AE-003 - Login User with incorrect email and password",
        groups = {"smoke", "regression", "authentication", "negative"}
    )
    @Story("Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Attempts login with non-existent credentials. " +
        "Verifies error message 'Your email or password is incorrect!' is displayed."
    )
    public void loginShouldFailWithInvalidCredentials() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        // Use a clearly fake email that can never be registered
        String invalidEmail    = "nonexistent_" + System.currentTimeMillis() + "@fakeDomain.xyz";
        String invalidPassword = "WrongPassword999";
        log.info("TC-AE-003 START | Testing with invalid email: {}...", invalidEmail.substring(0, 15));

        // ── ACT ──────────────────────────────────────────────────────────
        HomePage homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        LoginPage loginPage = homePage.clickLoginSignup();
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: Login page should be visible");

        loginPage
                .enterLoginEmail(invalidEmail)
                .enterLoginPassword(invalidPassword)
                .clickLoginButtonExpectingFailure();

        // ── ASSERT ───────────────────────────────────────────────────────
        Assert.assertTrue(loginPage.isLoginErrorVisible(),
                "FAIL: Error message should appear for invalid credentials");

        String actualError = loginPage.getLoginErrorMessage();
        Assert.assertEquals(actualError,
                "Your email or password is incorrect!",
                "FAIL: Error message text does not match expected value");

        log.info("TC-AE-003 PASS | Error message verified: '{}'", actualError);
    }

    // =====================================================================
    // TC-AE-004: Logout User
    // =====================================================================

    @Test(
        description = "TC-AE-004 - Logout User",
        groups = {"smoke", "regression", "authentication", "positive"}
    )
    @Story("Logout")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Registers and logs in a new user, then clicks Logout. " +
        "Verifies user is redirected to the login page."
    )
    public void logoutUserSuccessfully() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-004 START | Test user email prefix: {}", uniqueEmail.split("@")[0]);

        // Register and login a new user
        HomePage homePage = new HomePage(driver);
        SignupPage signupPage = homePage
                .clickLoginSignup()
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        Assert.assertTrue(signupPage.isAccountInfoVisible(),
                "FAIL: Account info form should be visible");

        homePage = signupPage
                .fillRegistrationForm(TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME,
                        TEST_ADDRESS, TEST_COUNTRY, TEST_STATE,
                        TEST_CITY, TEST_ZIPCODE, TEST_MOBILE)
                .clickCreateAccount()
                .clickContinue();

        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in before testing logout");

        // ── ACT ──────────────────────────────────────────────────────────
        log.info("TC-AE-004 ACT: Clicking Logout");
        LoginPage loginPage = homePage.clickLogout();

        // ── ASSERT ───────────────────────────────────────────────────────
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: User should be redirected to the login page after logout");

        Assert.assertTrue(homePage.isLoginSignupLinkVisible(),
                "FAIL: 'Signup / Login' nav link should reappear after logout");

        log.info("TC-AE-004 PASS | User successfully logged out and redirected to login page");
    }
}
