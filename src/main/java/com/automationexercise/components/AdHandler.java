package com.automationexercise.components;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * AdHandler – Xử lý quảng cáo riêng của automationexercise.com.
 *
 * LÝ DO TÁCH KHỎI BasePage:
 * BasePage là generic infrastructure dùng lại ở mọi dự án.
 * Logic quảng cáo này chỉ phù hợp với automationexercise.com (demo site).
 * Đặt ở đây để BasePage không bị ô nhiễm bởi đặc thù của một website cụ thể.
 *
 * CHIẾN LƯỢC XỬ LÝ VIGNETTE (theo thứ tự ưu tiên):
 * 1. Phát hiện vignette qua URL fragment (#google_vignette)
 * 2. Thử click nút Close để đóng overlay thật
 * 3. Chờ overlay biến mất, rồi dùng history.replaceState() làm sạch URL
 * 4. Thử Escape key nếu Close button không tìm được
 * 5. Fallback cuối: navigate đến clean URL
 *
 * TẠI SAO history.replaceState() KHÔNG ĐỦ MỘT MÌNH:
 * replaceState() chỉ xóa URL fragment khỏi address bar.
 * Nó KHÔNG xóa iframe/overlay đang che giao diện trong DOM.
 * Phải đóng overlay trước (bước 2/4), SAU ĐÓ mới dùng replaceState (bước 3).
 */
public final class AdHandler {

    private static final Logger log = LoggerFactory.getLogger(AdHandler.class);

    private static final Duration CLOSE_BUTTON_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration OVERLAY_GONE_TIMEOUT = Duration.ofSeconds(3);

    // Locators thử theo thứ tự khi tìm nút Close của vignette
    private static final By[] CLOSE_BUTTON_LOCATORS = {
        By.xpath("//button[contains(@aria-label,'Close') or contains(@aria-label,'close')]"),
        By.xpath("//div[contains(@id,'dismiss')]//button"),
        By.xpath("//*[text()='Close'][@role='button' or self::button]"),
        By.id("dismiss-button"),
        By.cssSelector("span#dismiss-button"),
    };

    private AdHandler() {
        throw new UnsupportedOperationException("AdHandler is a utility class.");
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Kiểm tra và dismiss Google Vignette ad nếu đang hiện.
     *
     * Gọi ở những nơi mà vignette có khả năng xuất hiện:
     * - BaseTest.setUp() sau khi navigate đến baseUrl
     * - Page method nào navigate về trang chủ (ví dụ SignupPage.clickContinue)
     *
     * @param driver WebDriver hiện tại
     */
    public static void dismissIfPresent(WebDriver driver) {
        if (!isVignettePresent(driver)) {
            return;
        }

        log.warn("🔔 Google Vignette detected (URL: {}). Dismissing...", driver.getCurrentUrl());

        // Strategy 1: Click nút Close → chờ overlay mất → clean URL fragment
        if (tryClickCloseButton(driver)) {
            waitForOverlayGone(driver);
            cleanUrlFragment(driver);
            log.info("✅ Vignette dismissed via Close button.");
            return;
        }

        // Strategy 2: Escape key → chờ overlay mất → clean URL fragment
        if (tryEscapeKey(driver)) {
            waitForOverlayGone(driver);
            cleanUrlFragment(driver);
            log.info("✅ Vignette dismissed via Escape key.");
            return;
        }

        // Strategy 3 (last resort): Navigate đến clean URL, reload trang
        navigateToCleanUrl(driver);
    }

    /**
     * Ẩn inline banner ads trong DOM (không xóa, chỉ hide bằng display:none).
     *
     * Dùng display:none thay vì removeChild để tránh làm thay đổi DOM structure.
     * Selector được thu hẹp, chỉ target những element chắc chắn là quảng cáo.
     *
     * @param driver WebDriver hiện tại
     */
    public static void hideInlineAds(WebDriver driver) {
        try {
            String script =
                "document.querySelectorAll(" +
                "  'iframe[id*=\"aswift\"], iframe[id*=\"google_ads\"], .adsbygoogle'" +
                ").forEach(function(el) { el.style.display = 'none'; });";
            ((JavascriptExecutor) driver).executeScript(script);
            log.debug("Inline ads hidden.");
        } catch (Exception e) {
            log.debug("Could not hide inline ads: {}", e.getMessage());
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private static boolean isVignettePresent(WebDriver driver) {
        return driver.getCurrentUrl().contains("google_vignette");
    }

    private static boolean tryClickCloseButton(WebDriver driver) {
        for (By locator : CLOSE_BUTTON_LOCATORS) {
            try {
                WebElement btn = new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
                btn.click();
                return true;
            } catch (Exception ignored) {
                // Try next locator
            }
        }
        return false;
    }

    private static boolean tryEscapeKey(WebDriver driver) {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            // Chờ xem URL có thay đổi không (overlay biến mất sẽ reset URL)
            new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                .until(d -> !d.getCurrentUrl().contains("google_vignette"));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Dùng history.replaceState() để xóa #google_vignette fragment khỏi URL.
     * Điều này chỉ làm sạch URL bar, KHÔNG tự đóng overlay.
     * Phải gọi SAU KHI overlay đã được đóng thật (bước 1 hoặc 2).
     */
    private static void cleanUrlFragment(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "history.replaceState(null, '', window.location.pathname + window.location.search);"
            );
            log.debug("URL fragment cleaned via history.replaceState.");
        } catch (Exception e) {
            log.debug("Could not clean URL fragment: {}", e.getMessage());
        }
    }

    private static void waitForOverlayGone(WebDriver driver) {
        try {
            new WebDriverWait(driver, OVERLAY_GONE_TIMEOUT)
                .until(d -> !d.getCurrentUrl().contains("google_vignette"));
        } catch (Exception ignored) {
            // Continue even if wait times out
        }
    }

    private static void navigateToCleanUrl(WebDriver driver) {
        String currentUrl = driver.getCurrentUrl();
        String cleanUrl = currentUrl.contains("#")
            ? currentUrl.substring(0, currentUrl.indexOf("#"))
            : currentUrl;
        log.warn("⚠️ Last resort: navigating to clean URL: {}", cleanUrl);
        try {
            driver.navigate().to(cleanUrl);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(ExpectedConditions.urlContains("google_vignette")));
            log.info("✅ Vignette bypassed via clean URL navigation.");
        } catch (Exception e) {
            log.error("❌ Could not dismiss vignette. Test may be unstable: {}", e.getMessage());
        }
    }
}
