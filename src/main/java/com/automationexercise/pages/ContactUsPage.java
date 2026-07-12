package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContactUsPage – Page Object cho trang /contact_us.
 *
 * Test Case: TC-AE-006 – Contact Us Form
 *
 * Trang này có một form liên hệ với các trường:
 * - Name, Email, Subject, Message
 * - File upload
 * - Submit button
 *
 * Đặc biệt: Submit form sẽ trigger một JavaScript ALERT (dialog xác nhận).
 * BasePage.acceptAlert() sẽ xử lý việc này.
 */
public class ContactUsPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ContactUsPage.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------

    /** "GET IN TOUCH" heading – xác nhận đang ở đúng trang */
    private static final By GET_IN_TOUCH_HEADING = By.xpath("//h2[normalize-space()='Get In Touch']");

    // Form fields
    private static final By NAME_INPUT      = By.cssSelector("input[data-qa='name']");
    private static final By EMAIL_INPUT     = By.cssSelector("input[data-qa='email']");
    private static final By SUBJECT_INPUT   = By.cssSelector("input[data-qa='subject']");
    private static final By MESSAGE_TEXTAREA= By.cssSelector("textarea[data-qa='message']");
    private static final By UPLOAD_FILE     = By.cssSelector("input[name='upload_file']");
    private static final By SUBMIT_BUTTON   = By.cssSelector("input[data-qa='submit-button']");

    /** Success message after form submission */
    private static final By SUCCESS_MESSAGE = By.cssSelector("div.alert-success");

    /** "Home" button on success state → navigate back to home page */
    private static final By HOME_BUTTON     = By.cssSelector("a.btn-success");

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    public ContactUsPage(WebDriver driver) {
        super(driver);
    }

    // -----------------------------------------------------------------
    // State Verification
    // -----------------------------------------------------------------

    /** Xác nhận "Get In Touch" heading visible → đang ở đúng trang */
    public boolean isGetInTouchVisible() {
        return isDisplayed(GET_IN_TOUCH_HEADING, 10);
    }

    /** Xác nhận message thành công visible sau khi submit */
    public boolean isSuccessMessageVisible() {
        return isDisplayed(SUCCESS_MESSAGE, 10);
    }

    /** Lấy text của success message */
    public String getSuccessMessageText() {
        return getText(SUCCESS_MESSAGE);
    }

    // -----------------------------------------------------------------
    // Form Actions (Fluent API)
    // -----------------------------------------------------------------

    public ContactUsPage enterName(String name) {
        log.info("Entering contact name");
        type(NAME_INPUT, name);
        return this;
    }

    public ContactUsPage enterEmail(String email) {
        log.info("Entering contact email");
        type(EMAIL_INPUT, email);
        return this;
    }

    public ContactUsPage enterSubject(String subject) {
        log.info("Entering subject: {}", subject);
        type(SUBJECT_INPUT, subject);
        return this;
    }

    public ContactUsPage enterMessage(String message) {
        log.info("Entering message");
        type(MESSAGE_TEXTAREA, message);
        return this;
    }

    /**
     * Uploads a file bằng cách gửi absolute path vào input file element.
     * KHÔNG cần click nút Browse – sendKeys() trực tiếp vào input type=file.
     *
     * @param absoluteFilePath Đường dẫn tuyệt đối đến file cần upload
     */
    public ContactUsPage uploadFile(String absoluteFilePath) {
        log.info("Uploading file: {}", absoluteFilePath);
        driver.findElement(UPLOAD_FILE).sendKeys(absoluteFilePath);
        return this;
    }

    /**
     * Clicks Submit → triggers JavaScript alert → auto-accepts alert.
     * Stays on ContactUsPage because success message appears on same page.
     */
    public ContactUsPage clickSubmit() {
        log.info("Clicking Submit button");
        click(SUBMIT_BUTTON);
        // Website shows a browser alert after submit – must accept it
        log.info("Accepting browser confirmation dialog");
        acceptAlert();
        return this;
    }

    /**
     * Clicks "Home" button on success page → navigates back to home.
     *
     * NOTE: Gọi handleVignette() vì navigate về trang chủ có thể trigger vignette ad.
     */
    public HomePage clickHome() {
        log.info("Clicking Home button after contact form submission");
        click(HOME_BUTTON);
        handleVignette(); // Dismiss vignette nếu xuất hiện khi về home page
        return new HomePage(driver);
    }
}
