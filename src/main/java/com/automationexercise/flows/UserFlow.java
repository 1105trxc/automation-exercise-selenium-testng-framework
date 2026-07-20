package com.automationexercise.flows;

import com.automationexercise.components.HeaderComponent;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.AccountCreatedPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.LoginPage;
import com.automationexercise.pages.SignupPage;
import com.automationexercise.utils.AccountCleanupService;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserFlow – Orchestrates common workflows related to user accounts.
 *
 * RESPONSIBILITY:
 *   - Positive registration flow: navigate → fill → submit → return AccountCreatedPage.
 *   - Positive login flow: navigate → enter credentials → submit → return HomePage.
 *
 * RULES:
 *   - No Assert / locator / JS / retry / broad catch inside a Flow.
 *   - registerNewUser() registers cleanup BEFORE submitting, so partial failures don't leak accounts.
 *   - Negative tests (wrong password, duplicate email) use Page Objects directly
 *     because they need to assert error state, which is not a Flow concern.
 */
public class UserFlow {

    private static final Logger log = LoggerFactory.getLogger(UserFlow.class);

    private final WebDriver driver;

    public UserFlow(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Completes the positive user registration journey and returns the confirmation page.
     *
     * FAIL-FAST POLICY:
     *   This method does not check the URL or silently continue on failure.
     *   If clickCreateAccount() lands on an error state, the returned AccountCreatedPage
     *   will fail the caller's assertion (isAccountCreatedVisible() returns false),
     *   making the failure explicit.
     *
     * CLEANUP REGISTRATION:
     *   AccountCleanupService.registerAccountForCleanup() is called BEFORE clicking
     *   Create Account. This ensures even a partially completed registration is cleaned up
     *   if the test subsequently fails.
     *
     * @param uniqueName   unique display name for signup
     * @param uniqueEmail  unique email address for signup
     * @param user         UserData with the rest of the registration details
     * @return AccountCreatedPage – for the caller to assert isAccountCreatedVisible()
     */
    public AccountCreatedPage registerNewUser(String uniqueName, String uniqueEmail, UserData user) {
        log.info("UserFlow: registering new user '{}'", uniqueName);

        LoginPage loginPage = new HeaderComponent(driver).clickLoginSignup();

        SignupPage signupPage = loginPage
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        signupPage.fillRegistrationForm(user);

        // Register BEFORE creating to ensure cleanup on partial failures
        AccountCleanupService.registerAccountForCleanup(uniqueEmail, user.getPassword());

        return signupPage.clickCreateAccount();
    }

    /**
     * Completes the positive login journey and returns the HomePage.
     *
     * Use this for scenarios where login is expected to succeed.
     * For negative login tests, interact with LoginPage directly so you can assert error state.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return HomePage – the page reached after a successful login
     */
    public HomePage loginSuccessfully(String email, String password) {
        log.info("UserFlow: logging in as '{}'", email);

        new HeaderComponent(driver)
                .clickLoginSignup()
                .enterLoginEmail(email)
                .enterLoginPassword(password)
                .clickLoginButton();

        return new HomePage(driver);
    }

    /**
     * Submits login credentials and returns the LoginPage for error assertion.
     * Use this for negative login test cases where you expect failure.
     *
     * @param email    the email to attempt
     * @param password the password to attempt
     * @return LoginPage – so the test can assert the error message
     */
    public LoginPage submitInvalidLogin(String email, String password) {
        log.info("UserFlow: submitting invalid login for '{}'", email);

        return new HeaderComponent(driver)
                .clickLoginSignup()
                .enterLoginEmail(email)
                .enterLoginPassword(password)
                .clickLoginButtonExpectingFailure();
    }
}
