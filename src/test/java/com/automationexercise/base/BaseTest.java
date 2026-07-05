package com.automationexercise.base;

import com.automationexercise.config.ConfigManager;
import com.automationexercise.driver.BrowserType;
import com.automationexercise.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * BaseTest – Parent class for all test classes.
 *
 * RESPONSIBILITIES:
 * - @BeforeMethod: Initialize WebDriver, navigate to base URL
 * - @AfterMethod:  Quit WebDriver, clean up resources
 *
 * RULES (from Blueprint):
 * - No business logic here
 * - No locators here
 * - No test data here
 * - Just setup and teardown
 *
 * HOW TO USE:
 *   public class LoginTest extends BaseTest {
 *       @Test
 *       public void myTest() {
 *           // 'driver' is available from BaseTest
 *           HomePage homePage = new HomePage(driver);
 *       }
 *   }
 *
 * BROWSER SELECTION PRIORITY:
 *   1. TestNG suite XML parameter: <parameter name="browser" value="firefox"/>
 *   2. System property: -Dbrowser=firefox
 *   3. local.properties: browser=chrome
 *   4. Hardcoded default: chrome
 */
public class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    /** Protected so all test subclasses can access the driver. */
    protected WebDriver driver;

    /**
     * Runs before each @Test method.
     *
     * @param browserParam Browser name from TestNG suite XML (optional).
     *                     If not provided via XML, falls back to config.
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional String browserParam) {
        // Determine which browser to use
        String browserName = (browserParam != null && !browserParam.isBlank())
                ? browserParam
                : ConfigManager.get("browser", "chrome");

        BrowserType browserType = BrowserType.fromString(browserName);

        log.info("══════════════════════════════════════════════");
        log.info("TEST SETUP: Initializing browser → {}", browserType);
        log.info("══════════════════════════════════════════════");

        // Initialize driver (stored in ThreadLocal inside DriverFactory)
        DriverFactory.initDriver(browserType);
        driver = DriverFactory.getDriver();

        // Navigate to the application base URL
        String baseUrl = ConfigManager.get("baseUrl", "https://automationexercise.com");
        driver.get(baseUrl);
        log.info("Navigated to: {}", baseUrl);
    }

    /**
     * Runs after each @Test method, regardless of pass/fail (alwaysRun = true).
     * CRITICAL: Must always run to prevent browser process leaks.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.info("TEST TEARDOWN: Closing browser");
        DriverFactory.quitDriver();
        log.info("══════════════════════════════════════════════");
    }
}
