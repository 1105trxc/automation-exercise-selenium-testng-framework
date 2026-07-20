package com.automationexercise.base;

import com.automationexercise.components.AdHandler;
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
 * BaseTest – Abstract parent class cho tất cả test class.
 *
 * TRÁCH NHIỆM:
 * - @BeforeMethod: Khởi động WebDriver, navigate đến baseUrl
 * - @AfterMethod: Đóng WebDriver, dọn dẹp resource
 *
 * QUY TẮC:
 * - Không chứa business logic
 * - Không chứa locator
 * - Không chứa test data
 * - Chỉ setup và teardown
 *
 * TẠI SAO LÀ ABSTRACT:
 * BaseTest là base class, không đại diện cho test cụ thể nào.
 * Đặt là abstract ngăn việc khởi tạo BaseTest() trực tiếp.
 *
 * TẠI SAO DÙNG driver() METHOD THAY VÌ FIELD:
 * DriverFactory lưu driver trong ThreadLocal.
 * Nếu lưu thêm vào field instance: Thread 1 và Thread 2 cùng extend BaseTest
 * có thể ghi đè lẫn nhau khi chạy parallel="methods".
 * driver() method luôn lấy đúng driver của thread hiện tại từ ThreadLocal.
 *
 * BROWSER SELECTION PRIORITY (từ cao đến thấp):
 * 1. CLI System property: -Dbrowser=firefox   (override tất cả)
 * 2. TestNG XML parameter: <parameter name="browser" value="chrome"/>
 * 3. Config file: local.properties browser=chrome
 * 4. Default: chrome
 *
 * VÍ DỤ SỬ DỤNG:
 *   public class LoginTest extends BaseTest {
 *       @Test
 *       public void myTest() {
 *           HomePage homePage = new HomePage(driver());
 *       }
 *   }
 */
public abstract class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    /**
     * Trả về WebDriver của thread hiện tại từ ThreadLocal trong DriverFactory.
     *
     * Dùng method thay vì field để đảm bảo parallel-safe:
     * mỗi lần gọi driver() luôn lấy đúng instance của thread đang chạy.
     */
    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }

    /**
     * Chạy trước mỗi @Test method.
     *
     * @param browserParam Browser từ TestNG XML (optional, có thể null).
     *                     CLI -Dbrowser sẽ override giá trị này.
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("") String browserParam) {
        String browserName = resolveBrowser(browserParam);
        BrowserType browserType = BrowserType.fromString(browserName);

        log.info("══════════════════════════════════════════════");
        log.info("TEST SETUP: Initializing browser → {}", browserType);
        log.info("══════════════════════════════════════════════");

        DriverFactory.initDriver(browserType);

        String baseUrl = ConfigManager.get("baseUrl", "https://automationexercise.com");
        driver().get(baseUrl);
        log.info("Navigated to: {}", baseUrl);

        // The config-gated workaround is scoped to known Google ad elements.
        AdHandler.dismissIfPresent(driver());
    }

    /**
     * Chạy sau mỗi @Test method, kể cả khi test fail (alwaysRun = true).
     * CRITICAL: Phải luôn chạy để tránh browser process leak.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(org.testng.ITestResult result) {
        if (result.getStatus() == org.testng.ITestResult.FAILURE) {
            log.error("Test FAILED: {}", result.getName());
        } else if (result.getStatus() == org.testng.ITestResult.SUCCESS) {
            log.info("Test PASSED: {}", result.getName());
        } else {
            log.info("Test SKIPPED: {}", result.getName());
        }

        try {
            com.automationexercise.utils.AccountCleanupService.CleanupResult cleanup =
                    com.automationexercise.utils.AccountCleanupService.cleanupCurrentTestAccounts();

            if (!cleanup.isFullySuccessful()) {
                log.warn("Unresolved account cleanup for test '{}': {}",
                        result.getName(), cleanup.failedEmails());
            }
        } finally {
            log.info("TEST TEARDOWN: Closing browser");
            DriverFactory.quitDriver();
            log.info("══════════════════════════════════════════════");
        }
    }

    /**
     * Xác định browser theo thứ tự ưu tiên:
     * CLI System property → TestNG XML param → config file → default chrome.
     *
     * CLI System property (-Dbrowser) luôn được ưu tiên cao nhất để
     * pipeline CI có thể override cấu hình static trong XML.
     */
    private String resolveBrowser(String browserParam) {
        // Priority 1: CLI override (-Dbrowser=firefox)
        String systemBrowser = System.getProperty("browser");
        if (systemBrowser != null && !systemBrowser.isBlank()) {
            log.debug("Browser resolved from system property: {}", systemBrowser);
            return systemBrowser;
        }

        // Priority 2: TestNG XML parameter
        if (browserParam != null && !browserParam.isBlank()) {
            log.debug("Browser resolved from TestNG XML parameter: {}", browserParam);
            return browserParam;
        }

        // Priority 3: Config file + Default
        return ConfigManager.get("browser", "chrome");
    }
}
