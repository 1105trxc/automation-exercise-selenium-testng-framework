package com.automationexercise.driver;

import com.automationexercise.config.ConfigManager;
import com.automationexercise.utils.DownloadManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;

/**
 * DriverFactory – Manages WebDriver lifecycle using ThreadLocal.
 *
 * RESPONSIBILITIES:
 * - Create (initialize) WebDriver instances
 * - Provide the current thread's driver (getDriver)
 * - Destroy (quit) the driver and clean up ThreadLocal
 *
 * WHY THREADLOCAL?
 * ThreadLocal gives each thread its own WebDriver instance.
 * This is essential for parallel test execution: two tests running
 * simultaneously won't share the same browser window.
 *
 * RULE: Tests NEVER create WebDriver directly. Always use DriverFactory.
 */
public final class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    /**
     * ThreadLocal stores one WebDriver per thread.
     * Thread 1 → its own Chrome browser
     * Thread 2 → its own Firefox browser
     * No interference between threads.
     */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Utility class – prevent instantiation
    private DriverFactory() {
        throw new UnsupportedOperationException("DriverFactory is a utility class.");
    }

    /**
     * Initializes a new WebDriver for the current thread.
     * Called in @BeforeMethod of BaseTest.
     *
     * @param browserType Which browser to launch
     */
    public static void initDriver(BrowserType browserType) {
        boolean headless = ConfigManager.getBoolean("headless", false);
        int pageLoadTimeout = ConfigManager.getInt("pageLoadTimeout", 30);

        WebDriver driver = switch (browserType) {
            case CHROME  -> createChromeDriver(headless);
            case FIREFOX -> createFirefoxDriver(headless);
            case EDGE    -> createEdgeDriver(headless);
        };

        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        driverThreadLocal.set(driver);
        log.info("Browser initialized: {} | Headless: {}", browserType, headless);
    }

    /**
     * Returns the WebDriver for the current thread.
     * Throws a clear error if driver is not initialized.
     *
     * @return Current thread's WebDriver
     * @throws IllegalStateException if initDriver() was not called
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                "WebDriver is not initialized for this thread. " +
                "Ensure initDriver() is called in @BeforeMethod."
            );
        }
        return driver;
    }

    /**
     * Quits the WebDriver and removes it from ThreadLocal.
     * Called in @AfterMethod(alwaysRun = true) of BaseTest.
     * MUST be called to prevent browser process leaks.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();

        if (driver == null) {
            log.warn("quitDriver() called but no driver was found for this thread.");
            return;
        }

        // IMPORTANT: ThreadLocal.remove() MUST run even if driver.quit() throws.
        // Without finally, a failed quit() leaves a stale driver reference in ThreadLocal,
        // which causes NoSuchSessionException or memory leaks in the next test.
        try {
            driver.quit();
            log.info("Driver closed successfully.");
        } catch (org.openqa.selenium.WebDriverException e) {
            log.warn("Exception while quitting driver; ThreadLocal will still be cleared.", e);
        } finally {
            driverThreadLocal.remove();
            log.debug("Driver removed from ThreadLocal.");
        }
    }

    // -----------------------------------------------------------------
    // Private factory methods for each browser
    // -----------------------------------------------------------------

    private static WebDriver createChromeDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");     // New headless mode (Selenium 4)
        }
        options.addArguments("--no-sandbox");           // Required in Linux CI environments
        options.addArguments("--disable-dev-shm-usage"); // Prevent memory issues in Docker
        options.addArguments("--disable-gpu");          // Stability in headless
        options.addArguments("--remote-allow-origins=*");

        // ----------------------------------------------------------------
        // Disable Chrome native popups that interfere with automation:
        // - "Save address?" popup (autofill) → appears after filling registration form
        // - "Save password?" bubble → appears after login
        // These are NATIVE Chrome UI elements, not web elements.
        // They cannot be dismissed by Selenium alone without these settings.
        // ----------------------------------------------------------------
        options.setExperimentalOption("prefs", chromiumPreferences());

        options.addArguments("--disable-popup-blocking");   // Don't block popups from page
        options.addArguments("--disable-notifications");    // Block browser notifications

        // NOTE: Selenium Manager (built into Selenium 4) auto-downloads ChromeDriver.
        // No need for WebDriverManager or manual driver setup.
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        String downloadDirectory = DownloadManager.getDownloadDirectory().toString();
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.dir", downloadDirectory);
        options.addPreference("browser.download.useDownloadDir", true);
        options.addPreference("browser.download.alwaysOpenPanel", false);
        options.addPreference(
                "browser.helperApps.neverAsk.saveToDisk",
                "text/plain,application/octet-stream");
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.setExperimentalOption("prefs", chromiumPreferences());
        return new EdgeDriver(options);
    }

    private static HashMap<String, Object> chromiumPreferences() {
        HashMap<String, Object> preferences = new HashMap<>();
        preferences.put("autofill.profile_enabled", false);
        preferences.put("autofill.credit_card_enabled", false);
        preferences.put("credentials_enable_service", false);
        preferences.put("profile.password_manager_enabled", false);
        preferences.put("download.default_directory", DownloadManager.getDownloadDirectory().toString());
        preferences.put("download.prompt_for_download", false);
        preferences.put("download.directory_upgrade", true);
        preferences.put("safebrowsing.enabled", true);
        return preferences;
    }
}
