package com.automationexercise.tests.contact;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.ContactFormData;
import com.automationexercise.pages.ContactUsPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.utils.JsonDataReader;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;

/**
 * ContactUsTest – Automated test for TC-AE-006.
 *
 * Test Case 6: Contact Us Form
 * 1. Launch browser
 * 2. Navigate to url 'http://automationexercise.com'
 * 3. Verify that home page is visible successfully
 * 4. Click on 'Contact Us' button
 * 5. Verify 'GET IN TOUCH' is visible
 * 6. Enter name, email, subject and message
 * 7. Upload file
 * 8. Click 'Submit' button
 * 9. Click OK button (browser alert)
 * 10. Verify success message 'Success! Your details have been submitted successfully.'
 * 11. Click 'Home' button and verify that home page is visible successfully
 *
 * NOTABLE TECHNIQUES:
 * - File upload: uses sendKeys() with absolute file path (no custom file dialog handling needed)
 * - Browser alert: handled in ContactUsPage.clickSubmit() via BasePage.acceptAlert()
 * - Test data from JSON: ContactFormData loaded from contact_form.json
 */
@Listeners(TestListener.class)
@Epic("Contact")
@Feature("Contact Us Form")
public class ContactUsTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ContactUsTest.class);

    @Test(
        description = "TC-AE-006 - Contact Us Form",
        groups = {"smoke", "regression", "contact", "positive"}
    )
    @Story("Submit Contact Us form")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Fills and submits the Contact Us form with data from contact_form.json. " +
        "Uploads a test file, handles browser alert, " +
        "and verifies the success message."
    )
    public void submitContactUsFormSuccessfully() {
        // ── ARRANGE ──────────────────────────────────────────────────────
        // Load form data from JSON
        ContactFormData formData = JsonDataReader.readFirst(
                "contact_form.json", "contactFormData", ContactFormData.class);
        log.info("TC-AE-006 START | Subject: '{}'", formData.getSubject());

        // Get absolute path to the test upload file from classpath resources
        String uploadFilePath = getUploadFilePath("testfiles/test_upload.txt");
        log.info("TC-AE-006 | Upload file path: {}", uploadFilePath);

        // ── ACT ──────────────────────────────────────────────────────────
        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Click 'Contact Us' button
        ContactUsPage contactUsPage = homePage.clickContactUs();

        // Step 5: Verify 'GET IN TOUCH' is visible
        Assert.assertTrue(contactUsPage.isGetInTouchVisible(),
                "FAIL: 'Get In Touch' heading should be visible on Contact Us page");
        log.info("TC-AE-006 | 'Get In Touch' heading verified");

        // Steps 6-9: Fill form, upload file, submit (alert handled inside clickSubmit())
        contactUsPage
                .enterName(formData.getName())
                .enterEmail(formData.getEmail())
                .enterSubject(formData.getSubject())
                .enterMessage(formData.getMessage())
                .uploadFile(uploadFilePath)
                .clickSubmit();  // ← internally calls acceptAlert()

        // ── ASSERT ───────────────────────────────────────────────────────
        // Step 10: Verify success message
        Assert.assertTrue(contactUsPage.isSuccessMessageVisible(),
                "FAIL: Success message should be visible after form submission");

        String successText = contactUsPage.getSuccessMessageText();
        Assert.assertTrue(successText.contains("Success"),
                "FAIL: Success message should contain 'Success'. Actual: " + successText);
        log.info("TC-AE-006 PASS (form submission) | Success message: '{}'", successText);

        // Step 11: Click 'Home' button and verify home page
        homePage = contactUsPage.clickHome();
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible after clicking Home button");

        log.info("TC-AE-006 PASS | Contact Us form submitted and verified successfully");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Gets the absolute file path of a resource file on the test classpath.
     * Required for Selenium's file upload (sendKeys needs absolute path).
     *
     * @param resourcePath Relative path in src/test/resources (e.g., "testfiles/test.txt")
     * @return Absolute path as String
     */
    private String getUploadFilePath(String resourcePath) {
        try {
            URL resource = getClass().getClassLoader().getResource(resourcePath);
            if (resource == null) {
                throw new RuntimeException("Upload file not found on classpath: " + resourcePath);
            }
            return new File(resource.toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve upload file path: " + resourcePath, e);
        }
    }
}
