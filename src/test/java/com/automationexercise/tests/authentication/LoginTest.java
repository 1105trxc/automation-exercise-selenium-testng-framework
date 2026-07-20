package com.automationexercise.tests.authentication;

import com.automationexercise.base.BaseTest;
import com.automationexercise.dataproviders.TestDataProvider;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.LoginData;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.AccountCreatedPage;
import com.automationexercise.pages.AccountDeletedPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.LoginPage;
import com.automationexercise.flows.UserFlow;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * LoginTest – Automated tests for TC-AE-002, TC-AE-003, TC-AE-004.
 *
 * PHASE 2 CHANGES:
 * - TC-AE-003 now uses @DataProvider: reads 2 invalid login scenarios from users.json
 *   → TC-AE-003 runs TWICE automatically (once per data row)!
 * - TC-AE-002 now fully asserts "ACCOUNT DELETED!" (step 10 of manual TC-AE-002)
 * - @Listeners added → auto screenshot on fail
 *
 * TEST INDEPENDENCE STRATEGY:
 * Each test registers its own unique user (timestamp-based email).
 * Tests can run in any order or in parallel without conflicts.
 */
@Listeners(TestListener.class)
@Epic("Authentication")
@Feature("Login and Logout")
public class LoginTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(LoginTest.class);

    // Registration profile constants (non-sensitive defaults for test users)
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
        "Verifies 'Logged in as username' is visible (Step 8). " +
        "Verifies 'ACCOUNT DELETED!' after deletion (Step 10)."
    )
    public void loginWithValidCredentials() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-002 START | email prefix: {}", uniqueEmail.split("@")[0]);

        // Step 3: Verify home page is visible
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible at start of test");

        // Steps 4-8: Register new user (pre-condition for login test)
        UserData testUser = new UserData();
        testUser.setTitle("Mr");
        testUser.setName(uniqueName);
        testUser.setPassword(TEST_PASSWORD);
        testUser.setDayOfBirth("10");
        testUser.setMonthOfBirth("3");
        testUser.setYearOfBirth("1995");
        testUser.setFirstName(TEST_FIRST_NAME);
        testUser.setLastName(TEST_LAST_NAME);
        testUser.setAddress1(TEST_ADDRESS);
        testUser.setCountry(TEST_COUNTRY);
        testUser.setState(TEST_STATE);
        testUser.setCity(TEST_CITY);
        testUser.setZipcode(TEST_ZIPCODE);
        testUser.setMobile(TEST_MOBILE);

        AccountCreatedPage accountCreatedPage =
                new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, testUser);

        homePage = accountCreatedPage.clickContinue();

        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(),
                "FAIL: User should be logged in after registration");
        Assert.assertEquals(homePage.getHeader().getLoggedInUsername(), uniqueName,
                "FAIL: Username after registration should match " + uniqueName);

        // Logout to prepare for actual login test
        LoginPage loginPage = homePage.getHeader().clickLogout();

        // ── ACT ──────────────────────────────────────────────────────────
        log.info("TC-AE-002 ACT: Logging in with registered credentials");
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: 'Login to your account' heading should be visible");

        homePage = new UserFlow(driver()).loginSuccessfully(uniqueEmail, TEST_PASSWORD);

        // ── ASSERT ───────────────────────────────────────────────────────
        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(),
                "FAIL: User should be logged in after submitting valid credentials");

        String loggedInUsername = homePage.getHeader().getLoggedInUsername();
        Assert.assertEquals(loggedInUsername, uniqueName,
                "FAIL: Logged in username should be exactly: " + uniqueName);
        log.info("TC-AE-002 PASS (login step) | Logged in as: '{}'", loggedInUsername);

        AccountDeletedPage accountDeletedPage = homePage.getHeader().clickDeleteAccount();

        Assert.assertTrue(accountDeletedPage.isAccountDeletedVisible(),
                "FAIL: 'ACCOUNT DELETED!' message should be visible after deletion");
        com.automationexercise.utils.AccountCleanupService.unregisterAccount(uniqueEmail);
        log.info("TC-AE-002 PASS | Account deleted and confirmed");
    }

    // =====================================================================
    // TC-AE-003: Login with incorrect email and password (Data-Driven)
    // =====================================================================

    /**
     * PHASE 2 UPGRADE: This test now uses @DataProvider.
     *
     * BEFORE (Phase 1): One hardcoded invalid credential per run
     * AFTER  (Phase 2): Reads from users.json → "invalidLoginUsers" array (2 rows)
     *                   TestNG runs this test TWICE automatically!
     *
     * Output shows:
     *   loginShouldFailWithInvalidCredentials[0] → PASS (email: nonexistent.forever@...)
     *   loginShouldFailWithInvalidCredentials[1] → PASS (email: another.invalid.user@...)
     */
    @Test(
        description = "TC-AE-003 - Login User with incorrect email and password",
        groups = {"smoke", "regression", "authentication", "negative"},
        dataProvider = "invalidLoginData",
        dataProviderClass = TestDataProvider.class
    )
    @Story("Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Attempts login with non-existent credentials loaded from users.json. " +
        "Verifies error message 'Your email or password is incorrect!' is displayed. " +
        "Runs once per data row in JSON (currently 2 scenarios)."
    )
    public void loginShouldFailWithInvalidCredentials(LoginData loginData) {
        log.info("TC-AE-003 START | Testing with email: {}...", loginData.getEmail().split("@")[0]);

        // ── ACT ──────────────────────────────────────────────────────────
        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Click Signup/Login
        LoginPage loginPage = homePage.getHeader().clickLoginSignup();

        // Step 5: Verify 'Login to your account' is visible
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: Login page should be visible");

        // Step 6: Enter incorrect email and password | Step 7: Click 'login' button
        loginPage
                .enterLoginEmail(loginData.getEmail())
                .enterLoginPassword(loginData.getPassword())
                .clickLoginButtonExpectingFailure();

        // ── ASSERT ───────────────────────────────────────────────────────
        // Step 8: Verify error message is displayed
        Assert.assertTrue(loginPage.isLoginErrorVisible(),
                "FAIL: Error message should appear for invalid credentials");

        String actualError = loginPage.getLoginErrorMessage();
        Assert.assertEquals(actualError,
                loginData.getExpectedError(),
                "FAIL: Error message text does not match expected value from JSON");

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
        log.info("TC-AE-004 START | email prefix: {}", uniqueEmail.split("@")[0]);

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        UserData testUser = new UserData();
        testUser.setTitle("Mr");
        testUser.setName(uniqueName);
        testUser.setPassword(TEST_PASSWORD);
        testUser.setDayOfBirth("10");
        testUser.setMonthOfBirth("3");
        testUser.setYearOfBirth("1995");
        testUser.setFirstName(TEST_FIRST_NAME);
        testUser.setLastName(TEST_LAST_NAME);
        testUser.setAddress1(TEST_ADDRESS);
        testUser.setCountry(TEST_COUNTRY);
        testUser.setState(TEST_STATE);
        testUser.setCity(TEST_CITY);
        testUser.setZipcode(TEST_ZIPCODE);
        testUser.setMobile(TEST_MOBILE);

        homePage = new UserFlow(driver())
                .registerNewUser(uniqueName, uniqueEmail, testUser)
                .clickContinue();

        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(),
                "FAIL: User should be logged in before testing logout");

        // ── ACT ──────────────────────────────────────────────────────────
        // Step 6: Click 'Logout' button
        log.info("TC-AE-004 ACT: Clicking Logout");
        LoginPage loginPage = homePage.getHeader().clickLogout();

        // ── ASSERT ───────────────────────────────────────────────────────
        // Step 7: Verify that user is navigated to login page
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: User should be redirected to the login page after logout");

        Assert.assertTrue(homePage.getHeader().isLoginSignupLinkVisible(),
                "FAIL: 'Signup / Login' nav link should reappear after logout");

        log.info("TC-AE-004 PASS | User successfully logged out");
    }
}
