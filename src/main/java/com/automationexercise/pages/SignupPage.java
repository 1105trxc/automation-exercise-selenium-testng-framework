package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignupPage – Page Object for the account registration form (/signup).
 *
 * This page appears AFTER clicking "Signup" on LoginPage with name + email.
 * It has two logical sections:
 *
 *   SECTION 1 – Account Information:
 *     Title (Mr/Mrs), Name (pre-filled), Email (pre-filled), Password,
 *     Date of Birth, Newsletter checkbox, Offers checkbox
 *
 *   SECTION 2 – Address Information:
 *     First name, Last name, Company, Address1, Address2,
 *     Country, State, City, Zipcode, Mobile number
 *
 * After filling all fields → Click "Create Account" → Account Created confirmation.
 *
 * NOTE: This is a minimal Phase 1 implementation.
 * Full implementation (with models/DataProvider) will be built in Phase 2.
 */
public class SignupPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(SignupPage.class);

    // -----------------------------------------------------------------
    // Section 1: Account Information Locators
    // -----------------------------------------------------------------

    private static final By ACCOUNT_INFO_HEADING = By.xpath("//b[normalize-space()='Enter Account Information']");
    private static final By TITLE_MR             = By.id("id_gender1");
    private static final By TITLE_MRS            = By.id("id_gender2");
    private static final By PASSWORD             = By.id("password");
    private static final By DOB_DAY              = By.id("days");
    private static final By DOB_MONTH            = By.id("months");
    private static final By DOB_YEAR             = By.id("years");
    private static final By NEWSLETTER           = By.id("newsletter");
    private static final By SPECIAL_OFFERS       = By.id("optin");

    // -----------------------------------------------------------------
    // Section 2: Address Information Locators
    // -----------------------------------------------------------------

    private static final By FIRST_NAME           = By.id("first_name");
    private static final By LAST_NAME            = By.id("last_name");
    private static final By COMPANY              = By.id("company");
    private static final By ADDRESS_1            = By.id("address1");
    private static final By ADDRESS_2            = By.id("address2");
    private static final By COUNTRY              = By.id("country");
    private static final By STATE                = By.id("state");
    private static final By CITY                 = By.id("city");
    private static final By ZIPCODE              = By.id("zipcode");
    private static final By MOBILE_NUMBER        = By.id("mobile_number");

    // -----------------------------------------------------------------
    // Buttons and Confirmation
    // -----------------------------------------------------------------

    private static final By CREATE_ACCOUNT_BUTTON = By.cssSelector("button[data-qa='create-account']");
    private static final By CONTINUE_BUTTON        = By.cssSelector("a[data-qa='continue-button']");
    private static final By ACCOUNT_CREATED_MSG    = By.xpath("//b[normalize-space()='ACCOUNT CREATED!']");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public SignupPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Confirms "Enter Account Information" heading is visible. */
    public boolean isAccountInfoVisible() {
        return isDisplayed(ACCOUNT_INFO_HEADING, 10);
    }

    /** Confirms "ACCOUNT CREATED!" is shown after successful registration. */
    public boolean isAccountCreatedMessageVisible() {
        return isDisplayed(ACCOUNT_CREATED_MSG, 10);
    }

    // -----------------------------------------------------------------
    // Individual Field Methods (Fluent API)
    // -----------------------------------------------------------------

    public SignupPage selectTitleMr() {
        click(TITLE_MR);
        return this;
    }

    public SignupPage selectTitleMrs() {
        click(TITLE_MRS);
        return this;
    }

    public SignupPage enterPassword(String password) {
        type(PASSWORD, password);
        return this;
    }

    public SignupPage selectDateOfBirth(String day, String month, String year) {
        selectByValue(DOB_DAY, day);
        selectByValue(DOB_MONTH, month);
        selectByValue(DOB_YEAR, year);
        return this;
    }

    public SignupPage checkNewsletter() {
        if (!driver.findElement(NEWSLETTER).isSelected()) {
            click(NEWSLETTER);
        }
        return this;
    }

    public SignupPage enterFirstName(String firstName) {
        type(FIRST_NAME, firstName);
        return this;
    }

    public SignupPage enterLastName(String lastName) {
        type(LAST_NAME, lastName);
        return this;
    }

    public SignupPage enterAddress(String address) {
        type(ADDRESS_1, address);
        return this;
    }

    public SignupPage selectCountry(String country) {
        selectByVisibleText(COUNTRY, country);
        return this;
    }

    public SignupPage enterState(String state) {
        type(STATE, state);
        return this;
    }

    public SignupPage enterCity(String city) {
        type(CITY, city);
        return this;
    }

    public SignupPage enterZipcode(String zipcode) {
        type(ZIPCODE, zipcode);
        return this;
    }

    public SignupPage enterMobileNumber(String mobile) {
        type(MOBILE_NUMBER, mobile);
        return this;
    }

    // -----------------------------------------------------------------
    // Composite Method (convenience for tests)
    // -----------------------------------------------------------------

    /**
     * Fills all required registration fields in one call.
     * Used in tests to keep the test body clean and readable.
     *
     * DESIGN DECISION: This method only fills required fields.
     * Optional fields (Address2, Company) are skipped to keep tests lean.
     * Phase 2 will use a UserData model to pass all fields flexibly.
     */
    public SignupPage fillRegistrationForm(String password,
                                           String firstName, String lastName,
                                           String address,
                                           String country, String state,
                                           String city, String zipcode,
                                           String mobile) {
        log.info("Filling registration form for: {} {}", firstName, lastName);
        selectTitleMr();
        enterPassword(password);
        selectDateOfBirth("10", "3", "1995");
        checkNewsletter();
        enterFirstName(firstName);
        enterLastName(lastName);
        enterAddress(address);
        selectCountry(country);
        enterState(state);
        enterCity(city);
        enterZipcode(zipcode);
        enterMobileNumber(mobile);
        return this;
    }

    // -----------------------------------------------------------------
    // Submit and Navigate
    // -----------------------------------------------------------------

    /** Clicks "Create Account" button. Stays on SignupPage until confirmation. */
    public SignupPage clickCreateAccount() {
        log.info("Clicking Create Account button");
        click(CREATE_ACCOUNT_BUTTON);
        return this;
    }

    /**
     * Clicks "Continue" after account creation confirmation.
     * Navigates to HomePage.
     */
    public HomePage clickContinue() {
        log.info("Clicking Continue after account creation");
        click(CONTINUE_BUTTON);
        return new HomePage(driver);
    }
}
