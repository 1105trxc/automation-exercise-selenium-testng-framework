package com.automationexercise.listeners;

import com.automationexercise.driver.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TestListener – Tự động hóa các hành động khi test start/pass/fail/skip.
 *
 * IMPLEMENTS ITestListener của TestNG.
 *
 * KEY FEATURE: Chụp màn hình tự động khi test FAIL.
 * Ảnh được lưu vào: reports/screenshots/FAILED_TestName_timestamp.png
 *
 * HOW TO ACTIVATE:
 * Trong TestNG suite XML:
 *   <listeners>
 *     <listener class-name="com.automationexercise.listeners.TestListener"/>
 *   </listeners>
 *
 * Hoặc dùng @Listeners annotation trong test class.
 *
 * LOG FORMAT sau khi thêm Listener:
 * ❌ FAILED: TC-AE-004 - Logout User
 *    → Screenshot: reports/screenshots/FAILED_logoutUserSuccessfully_20260706194453.png
 *    → Reason: AssertionError: User should be logged in before testing logout
 *
 * (Dễ đọc hơn stack trace 50 dòng!)
 */
public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);
    private static final String SCREENSHOT_DIR = "reports/screenshots/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ STARTING: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ PASSED:  {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getName();
        String reason = result.getThrowable() != null
            ? result.getThrowable().getMessage()
            : "Unknown error";

        log.error("❌ FAILED:  {}", testName);
        log.error("   Reason:  {}", reason);

        // Take screenshot automatically
        String screenshotPath = captureScreenshot("FAILED_" + testName);
        if (screenshotPath != null) {
            log.error("   Screenshot: {}", screenshotPath);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⚠️ SKIPPED: {}", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("══════════════════════════════════════════");
        log.info("SUITE STARTED: {}", context.getName());
        log.info("══════════════════════════════════════════");
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("══════════════════════════════════════════");
        log.info("SUITE FINISHED: {}", context.getName());
        log.info("  ✅ Passed:  {}", context.getPassedTests().size());
        log.info("  ❌ Failed:  {}", context.getFailedTests().size());
        log.info("  ⚠️ Skipped: {}", context.getSkippedTests().size());
        log.info("══════════════════════════════════════════");
    }

    // -----------------------------------------------------------------------
    // Screenshot capture
    // -----------------------------------------------------------------------

    /**
     * Captures a screenshot of the current browser window.
     *
     * @param filePrefix Prefix for the screenshot filename (e.g., "FAILED_loginTest")
     * @return Absolute path to the saved screenshot, or null if capture failed
     */
    private String captureScreenshot(String filePrefix) {
        try {
            WebDriver driver = DriverFactory.getDriver();

            // Some drivers may not support screenshots
            if (!(driver instanceof TakesScreenshot)) {
                log.warn("Current driver does not support screenshots.");
                return null;
            }

            // Take the screenshot as byte array
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Create directory if it doesn't exist
            Path dir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dir);

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String sanitizedPrefix = filePrefix.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = sanitizedPrefix + "_" + timestamp + ".png";
            Path screenshotPath = dir.resolve(fileName);

            // Save the file
            Files.write(screenshotPath, screenshotBytes);

            return screenshotPath.toAbsolutePath().toString();

        } catch (IllegalStateException e) {
            // Driver not initialized (e.g., setup failed before driver was created)
            log.debug("Cannot capture screenshot: WebDriver not initialized.");
            return null;
        } catch (IOException e) {
            log.error("Failed to save screenshot: {}", e.getMessage());
            return null;
        }
    }
}
