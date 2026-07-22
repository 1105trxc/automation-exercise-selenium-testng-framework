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
import com.automationexercise.utils.JsonDataReader;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * LoginTest – Automated tests for TC-AE-002, TC-AE-003, TC-AE-004.
 * TEST INDEPENDENCE STRATEGY:
 * Each test registers its own unique user (timestamp-based email).
 * Tests can run in any order or in parallel without conflicts.
 */
@Listeners(TestListener.class)
@Epic("Authentication")
@Feature("Login and Logout")
public class LoginTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(LoginTest.class);

    // TC-AE-002: Login with valid credentials

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
        // Arrange
        UserData testUser = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-002 START | email prefix: {}", uniqueEmail.split("@")[0]);

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible at start of test");

        AccountCreatedPage accountCreatedPage =
                new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, testUser);

        homePage = accountCreatedPage.clickContinue();

        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(),
                "FAIL: User should be logged in after registration");
        Assert.assertEquals(homePage.getHeader().getLoggedInUsername(), uniqueName,
                "FAIL: Username after registration should match " + uniqueName);

        // Logout to prepare for actual login test
        LoginPage loginPage = homePage.getHeader().clickLogout();

        // Act
        log.info("TC-AE-002 ACT: Logging in with registered credentials");
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: 'Login to your account' heading should be visible");

        homePage = new UserFlow(driver()).loginSuccessfully(uniqueEmail, testUser.getPassword());

        // Assert
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

    // TC-AE-003: Login with incorrect email and password (Data-Driven)
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

        // Act
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        LoginPage loginPage = homePage.getHeader().clickLoginSignup();

        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: Login page should be visible");

        loginPage
                .enterLoginEmail(loginData.getEmail())
                .enterLoginPassword(loginData.getPassword())
                .clickLoginButtonExpectingFailure();

        // Assert
        Assert.assertTrue(loginPage.isLoginErrorVisible(),
                "FAIL: Error message should appear for invalid credentials");

        String actualError = loginPage.getLoginErrorMessage();
        Assert.assertEquals(actualError,
                loginData.getExpectedError(),
                "FAIL: Error message text does not match expected value from JSON");
        Assert.assertTrue(loginPage.getHeader().isLoginSignupLinkVisible(),
                "FAIL: Signup / Login link should remain visible after rejected credentials");

        log.info("TC-AE-003 PASS | Error message verified: '{}'", actualError);
    }

    // TC-AE-004: Logout User

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
        // Arrange
        UserData testUser = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-004 START | email prefix: {}", uniqueEmail.split("@")[0]);

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        homePage = new UserFlow(driver())
                .registerNewUser(uniqueName, uniqueEmail, testUser)
                .clickContinue();

        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(),
                "FAIL: User should be logged in before testing logout");

        // Act
        log.info("TC-AE-004 ACT: Clicking Logout");
        LoginPage loginPage = homePage.getHeader().clickLogout();

        // Assert
        Assert.assertTrue(loginPage.isLoginPageVisible(),
                "FAIL: User should be redirected to the login page after logout");

        Assert.assertTrue(homePage.getHeader().isLoginSignupLinkVisible(),
                "FAIL: 'Signup / Login' nav link should reappear after logout");

        log.info("TC-AE-004 PASS | User successfully logged out");
    }
}
