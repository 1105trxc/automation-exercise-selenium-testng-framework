package com.automationexercise.listeners;

import com.automationexercise.driver.DriverFactory;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

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
 * [FAIL] logoutUserSuccessfully
 *    Screenshot: reports/screenshots/FAILED_logoutUserSuccessfully_20260706194453.png
 *    Reason: AssertionError: User should be logged in before testing logout
 *
 * (Dễ đọc hơn stack trace 50 dòng!)
 */
public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);
    private static final String SCREENSHOT_DIR = "reports/screenshots/";
    private static final String LOG_SEPARATOR = "------------------------------------------";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void onTestStart(ITestResult result) {
        log.info("[START] {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("[PASS]  {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getName();
        Throwable failure = result.getThrowable();
        String reason = failure == null
                ? "Unknown error"
                : failure.getMessage() != null
                        ? failure.getMessage()
                        : failure.getClass().getSimpleName();

        log.error("[FAIL]  {}", testName);
        log.error("   Reason:  {}", reason);
        logFailureContext(result);

        String screenshotPath = captureScreenshot("FAILED_" + testName);
        if (screenshotPath != null) {
            log.error("   Screenshot: {}", screenshotPath);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("[SKIP]  {}", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info(LOG_SEPARATOR);
        log.info("SUITE STARTED: {}", context.getName());
        log.info(LOG_SEPARATOR);
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info(LOG_SEPARATOR);
        log.info("SUITE FINISHED: {}", context.getName());
        log.info("  Passed:  {}", context.getPassedTests().size());
        log.info("  Failed:  {}", context.getFailedTests().size());
        log.info("  Skipped: {}", context.getSkippedTests().size());
        log.info(LOG_SEPARATOR);
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

            if (!(driver instanceof TakesScreenshot screenshotDriver)) {
                log.warn("Current driver does not support screenshots.");
                return null;
            }

            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);

            Path dir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(FORMATTER);
            String sanitizedPrefix = filePrefix.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = sanitizedPrefix + "_" + timestamp + ".png";
            Path screenshotPath = dir.resolve(fileName);

            Files.write(screenshotPath, screenshotBytes);

            return screenshotPath.toAbsolutePath().toString();

        } catch (IllegalStateException | WebDriverException e) {
            log.debug("Cannot capture screenshot from the current WebDriver: {}", e.getMessage());
            return null;
        } catch (IOException e) {
            log.error("Failed to save screenshot: {}", e.getMessage());
            return null;
        }
    }

    private void logFailureContext(ITestResult result) {
        String description = result.getMethod().getDescription();
        if (description != null && !description.isBlank()) {
            log.error("   Test case: {}", description);
        }
        log.error("   Environment: {}", System.getProperty("env", "local"));

        try {
            WebDriver driver = DriverFactory.getDriver();
            if (driver instanceof HasCapabilities capableDriver) {
                log.error("   Browser: {} {}",
                        capableDriver.getCapabilities().getBrowserName(),
                        capableDriver.getCapabilities().getBrowserVersion());
            }
            log.error("   Current URL: {}", driver.getCurrentUrl());
        } catch (IllegalStateException | WebDriverException e) {
            log.debug("Browser context is unavailable for failure evidence: {}", e.getMessage());
        }
    }
}
