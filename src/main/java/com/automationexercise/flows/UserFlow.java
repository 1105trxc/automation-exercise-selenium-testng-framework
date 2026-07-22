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
     * @param uniqueName   unique display name for signup
     * @param uniqueEmail  unique email address for signup
     * @param user         UserData with registration details
     * @return the displayed account-created confirmation page
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

        return new HeaderComponent(driver)
                .clickLoginSignup()
                .enterLoginEmail(email)
                .enterLoginPassword(password)
                .clickLoginButton();
    }

}
