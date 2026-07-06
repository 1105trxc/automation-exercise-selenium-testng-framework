package com.automationexercise.tests.registration;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.LoginPage;
import com.automationexercise.pages.SignupPage;
import com.automationexercise.utils.JsonDataReader;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * RegistrationTest – Automated tests for TC-AE-001 and TC-AE-005.
 *
 * PHASE 2: Demonstrates data-driven testing with UserData loaded from JSON.
 *
 * TC-AE-001: Register User
 *   - Full happy path registration flow
 *   - Verifies ACCOUNT CREATED! and ACCOUNT DELETED! messages
 *
 * TC-AE-005: Register User with existing email
 *   - Registers a user, logs out, then tries to register again with same email
 *   - Verifies "Email Address already exist!" error message
 *
 * DATA STRATEGY:
 * Profile data (title, name, password, address...) → loaded from validUsers[0] in users.json
 * Email → generated dynamically via RandomDataUtils.generateUniqueEmail()
 * This hybrid approach ensures test independence while having clean, maintainable test data.
 */
@Listeners(TestListener.class)
@Epic("Registration")
@Feature("User Registration")
public class RegistrationTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(RegistrationTest.class);

    // =====================================================================
    // TC-AE-001: Register User (Happy Path)
    // =====================================================================

    @Test(
        description = "TC-AE-001 - Register User",
        groups = {"smoke", "regression", "registration", "positive"}
    )
    @Story("Register new user")
    @Severity(SeverityLevel.BLOCKER)
    @Description(
        "Completes full user registration flow. " +
        "Uses UserData profile from users.json, unique email generated at runtime. " +
        "Verifies ACCOUNT CREATED! and ACCOUNT DELETED! messages."
    )
    public void registerNewUserSuccessfully() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        // Load profile template from JSON (name, password, address, etc.)
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);

        // Generate unique email to avoid conflicts with previous runs
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-001 START | email prefix: {}", uniqueEmail.split("@")[0]);

        // ── ACT ──────────────────────────────────────────────────────────
        // Step 3: Verify home page is visible
        HomePage homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible at start of test");

        // Step 4: Click on 'Signup / Login' button
        LoginPage loginPage = homePage.clickLoginSignup();

        // Step 5: Verify 'New User Signup!' is visible
        Assert.assertTrue(loginPage.isSignupSectionVisible(),
                "FAIL: 'New User Signup!' section should be visible on login page");

        // Step 6: Enter name and email address
        // Step 7: Click 'Signup' button
        SignupPage signupPage = loginPage
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        // Step 8: Verify that 'ENTER ACCOUNT INFORMATION' is visible
        Assert.assertTrue(signupPage.isAccountInfoVisible(),
                "FAIL: 'Enter Account Information' heading should be visible");

        // Steps 9-14: Fill in account details using UserData from JSON
        log.info("TC-AE-001 ACT: Filling registration form from JSON: {}", user);
        signupPage.fillRegistrationForm(user);

        // Step 15 / 16: Click 'Create Account' button
        signupPage.clickCreateAccount();

        // Step 16: Verify that 'ACCOUNT CREATED!' is visible
        Assert.assertTrue(signupPage.isAccountCreatedMessageVisible(),
                "FAIL: 'ACCOUNT CREATED!' message should be visible after form submission");
        log.info("TC-AE-001 PASS (registration step) | Account created successfully");

        // Step 17: Click 'Continue' button
        homePage = signupPage.clickContinue();

        // Step 18: Verify that 'Logged in as username' is visible
        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in after account creation");

        String loggedInUsername = homePage.getLoggedInUsername();
        log.info("TC-AE-001 | Logged in as: '{}'", loggedInUsername);

        // Step 19: Click 'Delete Account' button
        homePage.clickDeleteAccount();

        // Step 20: Verify that 'ACCOUNT DELETED!' is visible
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: 'ACCOUNT DELETED!' message should be visible after deletion");
        log.info("TC-AE-001 PASS | Account registered, verified, and deleted successfully");
    }

    // =====================================================================
    // TC-AE-005: Register User with existing email
    // =====================================================================

    @Test(
        description = "TC-AE-005 - Register User with existing email",
        groups = {"regression", "registration", "negative"}
    )
    @Story("Register with duplicate email")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Registers a user, logs out, then tries to register again with the same email. " +
        "Verifies 'Email Address already exist!' error message is displayed."
    )
    public void registerShouldFailWithExistingEmail() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-005 START | email: {}...", uniqueEmail.split("@")[0]);

        // Step 1: Register user first (pre-condition)
        log.info("ARRANGE: Registering first user to occupy the email");
        HomePage homePage = new HomePage(driver);
        SignupPage signupPage = homePage
                .clickLoginSignup()
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        signupPage.fillRegistrationForm(user).clickCreateAccount();
        Assert.assertTrue(signupPage.isAccountCreatedMessageVisible(),
                "FAIL: Pre-condition failed – account should be created successfully");

        homePage = signupPage.clickContinue();
        Assert.assertTrue(homePage.isUserLoggedIn(), "FAIL: Should be logged in after registration");

        // Logout to simulate a new registration attempt
        LoginPage loginPage = homePage.clickLogout();

        // ── ACT ──────────────────────────────────────────────────────────
        // Step 5: Enter name and same email address
        // Step 6: Click 'Signup' button
        log.info("TC-AE-005 ACT: Trying to register again with the SAME email");
        loginPage
                .enterSignupName("AnotherUser")
                .enterSignupEmail(uniqueEmail)   // ← same email used again!
                .clickSignupButtonExpectingDuplicate();

        // ── ASSERT ───────────────────────────────────────────────────────
        // Step 7: Verify error 'Email Address already exist!' is visible
        Assert.assertTrue(loginPage.isSignupErrorVisible(),
                "FAIL: 'Email Address already exist!' error should be visible");

        String actualError = loginPage.getSignupErrorMessage();
        Assert.assertEquals(actualError,
                "Email Address already exist!",
                "FAIL: Duplicate email error message does not match expected");
        log.info("TC-AE-005 PASS | Duplicate email error verified: '{}'", actualError);

        // ── CLEANUP ──────────────────────────────────────────────────────
        // Login and delete the first account to clean up
        log.info("TC-AE-005 CLEANUP: Logging in to delete the registered account");
        homePage = loginPage
                .enterLoginEmail(uniqueEmail)
                .enterLoginPassword(user.getPassword())
                .clickLoginButton();

        Assert.assertTrue(homePage.isUserLoggedIn(), "FAIL: Should be able to login for cleanup");
        homePage.clickDeleteAccount();
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: Account should be deleted during cleanup");
        log.info("TC-AE-005 CLEANUP: Account deleted");
    }
}
