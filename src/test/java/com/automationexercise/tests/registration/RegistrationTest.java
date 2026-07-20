package com.automationexercise.tests.registration;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.AccountCreatedPage;
import com.automationexercise.pages.AccountDeletedPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.LoginPage;
import com.automationexercise.pages.SignupPage;
import com.automationexercise.flows.UserFlow;
import com.automationexercise.utils.AccountCleanupService;
import com.automationexercise.utils.JsonDataReader;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * RegistrationTest – TC-AE-001 and TC-AE-005.
 *
 * TC-AE-001: Register User (happy path)
 * TC-AE-005: Register User with existing email (negative)
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
        "Verifies ACCOUNT CREATED! and ACCOUNT DELETED! messages."
    )
    public void registerNewUserSuccessfully() {
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-001 START | email prefix: {}", uniqueEmail.split("@")[0]);

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible at start of test");

        LoginPage loginPage = homePage.getHeader().clickLoginSignup();
        Assert.assertTrue(loginPage.isSignupSectionVisible(),
                "FAIL: 'New User Signup!' section should be visible on login page");

        SignupPage signupPage = loginPage
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        Assert.assertTrue(signupPage.isAccountInfoVisible(),
                "FAIL: 'Enter Account Information' heading should be visible");

        signupPage.fillRegistrationForm(user);
        // Register for cleanup BEFORE submitting — handles partial failure
        AccountCleanupService.registerAccountForCleanup(uniqueEmail, user.getPassword());

        AccountCreatedPage accountCreatedPage = signupPage.clickCreateAccount();
        Assert.assertTrue(accountCreatedPage.isAccountCreatedVisible(),
                "FAIL: 'ACCOUNT CREATED!' message should be visible after form submission");
        log.info("TC-AE-001 PASS (registration step) | Account created successfully");

        homePage = accountCreatedPage.clickContinue();
        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(),
                "FAIL: User should be logged in after registration");
        Assert.assertEquals(homePage.getHeader().getLoggedInUsername(), uniqueName,
                "FAIL: Logged in username should be exactly: " + uniqueName);
        log.info("TC-AE-001 | Logged in as: '{}'", homePage.getHeader().getLoggedInUsername());

        AccountDeletedPage accountDeletedPage = homePage.getHeader().clickDeleteAccount();
        Assert.assertTrue(accountDeletedPage.isAccountDeletedVisible(),
                "FAIL: 'ACCOUNT DELETED!' message should be visible after deletion");
        AccountCleanupService.unregisterAccount(uniqueEmail);
        log.info("TC-AE-001 PASS | Account deleted and confirmed");
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
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-005 START | email: {}...", uniqueEmail.split("@")[0]);

        // Pre-condition: Register a user first
        log.info("ARRANGE: Registering first user to occupy the email");
        AccountCreatedPage accountCreatedPage = new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user);
        Assert.assertTrue(accountCreatedPage.isAccountCreatedVisible(),
                "FAIL: Pre-condition failed - account should be created successfully");

        HomePage homePage = accountCreatedPage.clickContinue();
        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(), "FAIL: Should be logged in after registration");
        Assert.assertEquals(homePage.getHeader().getLoggedInUsername(), uniqueName,
                "FAIL: Logged in username should be exactly: " + uniqueName);

        LoginPage loginPage = homePage.getHeader().clickLogout();

        // ACT: Try to register again with same email
        log.info("TC-AE-005 ACT: Trying to register again with the SAME email");
        loginPage
                .enterSignupName("AnotherUser")
                .enterSignupEmail(uniqueEmail)
                .clickSignupButtonExpectingError();

        Assert.assertTrue(loginPage.isEmailExistsErrorVisible(),
                "FAIL: 'Email Address already exist!' error should be visible");
        String actualError = loginPage.getEmailExistsErrorMessage();
        Assert.assertEquals(actualError, "Email Address already exist!",
                "FAIL: Duplicate email error message does not match expected");
        log.info("TC-AE-005 PASS | Duplicate email error verified: '{}'", actualError);

        // CLEANUP: Login and delete via UI, then unregister from API cleanup
        log.info("TC-AE-005 CLEANUP: Logging in to delete the registered account");
        homePage = new UserFlow(driver()).loginSuccessfully(uniqueEmail, user.getPassword());
        Assert.assertTrue(homePage.getHeader().isUserLoggedIn(), "FAIL: Should be able to login for cleanup");

        AccountDeletedPage deletedPage = homePage.getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(),
                "FAIL: Account should be deleted during cleanup");
        AccountCleanupService.unregisterAccount(uniqueEmail);
        log.info("TC-AE-005 CLEANUP: Account deleted");
    }
}
