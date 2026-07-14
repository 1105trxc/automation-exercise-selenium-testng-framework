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
import java.util.List;

/**
 * AdHandler – Xử lý quảng cáo riêng của automationexercise.com.
 *
 * LÝ DO TÁCH KHỎI BasePage:
 * BasePage là generic infrastructure dùng lại ở mọi dự án.
 * Logic quảng cáo này chỉ phù hợp với automationexercise.com (demo site).
 *
 * ═══════════════════════════════════════════════════════════════════
 * PHÂN TÍCH VẤN ĐỀ (dựa trên log lỗi và screenshots thực tế):
 * ═══════════════════════════════════════════════════════════════════
 *
 * Website automationexercise.com có 2 LOẠI quảng cáo khác nhau:
 *
 * LOẠI 1 – Google Vignette (URL-based):
 *   URL chứa "#google_vignette" → overlay hiện TRƯỚC trang load.
 *   → Xử lý bằng cách click Close → Escape → navigate clean URL.
 *
 * LOẠI 2 – Full-viewport iframe (KHÔNG có URL fragment):
 *   iframe aswift_N với style="width:100vw; height:100vh; position:absolute"
 *   che toàn bộ màn hình SAU KHI trang đã load.
 *   URL BÌNH THƯỜNG (không có #google_vignette).
 *   → Xử lý bằng cách:
 *     a. Click "Close" text/button ở gần iframe
 *     b. Ẩn iframe bằng JS (display:none) nếu không tìm được Close
 *
 * ERROR MESSAGE ĐIỂN HÌNH CỦA LOẠI 2:
 * "element click intercepted: Element <a class='add-to-cart'>
 *  is not clickable at point (790, 920).
 *  Other element would receive the click: <iframe id='aswift_2'
 *  style='width: 100vw !important; height: 100vh !important'>"
 *
 * CHIẾN LƯỢC XỬ LÝ (theo thứ tự ưu tiên):
 * 1. Detect Vignette (URL-based) → click Close → Escape → clean URL
 * 2. Detect Full-viewport iframe (CSS-based) → click Close text → JS hide
 *
 * TẠI SAO history.replaceState() KHÔNG ĐỦ MỘT MÌNH:
 * replaceState() chỉ xóa URL fragment. Nó KHÔNG xóa iframe trong DOM.
 * Phải đóng overlay thật trước, SAU ĐÓ mới clean URL.
 */
public final class AdHandler {

    private static final Logger log = LoggerFactory.getLogger(AdHandler.class);

    private static final Duration CLOSE_BUTTON_TIMEOUT  = Duration.ofSeconds(3);
    private static final Duration OVERLAY_GONE_TIMEOUT  = Duration.ofSeconds(3);
    private static final Duration IFRAME_DETECT_TIMEOUT = Duration.ofSeconds(2);

    // -----------------------------------------------------------------
    // Locators – Nút Close của Vignette (thử theo thứ tự)
    // -----------------------------------------------------------------

    private static final By[] VIGNETTE_CLOSE_LOCATORS = {
        By.xpath("//button[contains(@aria-label,'Close') or contains(@aria-label,'close')]"),
        By.xpath("//div[contains(@id,'dismiss')]//button"),
        By.xpath("//*[text()='Close'][@role='button' or self::button]"),
        By.id("dismiss-button"),
        By.cssSelector("span#dismiss-button"),
    };

    // -----------------------------------------------------------------
    // Locators – Full-viewport iframe ad (Loại 2)
    // -----------------------------------------------------------------

    /**
     * Iframe check: aswift_N có position absolute/fixed + width/height 100vw/100vh.
     * Dùng JS để kiểm tra vì CSS inline style không query được bằng CSS selector đơn giản.
     */
    private static final String FULLSCREEN_IFRAME_JS =
        "var ads = document.querySelectorAll('[id^=\"aswift\"], [id^=\"google_ads\"], ins.adsbygoogle');" +
        "for (var i = 0; i < ads.length; i++) {" +
        "  var s = ads[i].style;" +
        "  if (s.width && s.width.includes('100vw') && s.height && s.height.includes('100vh')) {" +
        "    return true;" +
        "  }" +
        "}" +
        "return false;";

    /**
     * "Close" text xuất hiện phía trên iframe full-viewport.
     * Trên screenshots thấy rõ chữ "Close" ở top-right của vùng mờ.
     * Locator: element chứa text "Close" KHÔNG nằm trong iframe.
     */
    private static final By[] FULLSCREEN_AD_CLOSE_LOCATORS = {
        By.xpath("//div[contains(text(),'Close') and not(ancestor::iframe)]"),
        By.xpath("//p[contains(text(),'Close') and not(ancestor::iframe)]"),
        By.xpath("//*[normalize-space(text())='Close'][not(ancestor::iframe)]"),
        By.xpath("//a[normalize-space(text())='Close']"),
    };

    /**
     * Script ẩn tất cả iframe ads full-viewport bằng display:none.
     * Dùng làm fallback khi không click được Close.
     * display:none để không phá DOM structure, an toàn hơn removeChild.
     */
    private static final String HIDE_FULLSCREEN_IFRAME_JS =
        "document.querySelectorAll('[id^=\"aswift\"], [id^=\"google_ads\"], ins.adsbygoogle').forEach(function(el) {" +
        "  if (el.style.width && el.style.width.includes('100vw')) {" +
        "    el.style.setProperty('display', 'none', 'important');" +
        "    el.style.setProperty('visibility', 'hidden', 'important');" +
        "    el.style.setProperty('pointer-events', 'none', 'important');" +
        "    el.style.setProperty('z-index', '-1', 'important');" +
        "  }" +
        "});";

    private AdHandler() {
        throw new UnsupportedOperationException("AdHandler is a utility class.");
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Kiểm tra và dismiss Google Vignette ad nếu đang hiện (URL-based detection).
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
        if (tryClickCloseButton(driver, VIGNETTE_CLOSE_LOCATORS)) {
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
     * Xử lý full-viewport iframe ads (Loại 2 – KHÔNG có URL fragment).
     *
     * Đây là loại quảng cáo gây lỗi "element click intercepted" vì:
     * - iframe có style="width:100vw; height:100vh; position:absolute"
     * - Che toàn bộ màn hình
     * - URL KHÔNG chứa #google_vignette nên dismissIfPresent() bỏ qua
     *
     * GỌI METHOD NÀY TRƯỚC KHI CLICK bất kỳ element nào trên ProductsPage,
     * CategoryPage, BrandPage (những trang có inline ads mạnh).
     *
     * @param driver WebDriver hiện tại
     */
    public static void dismissFullScreenAd(WebDriver driver) {
        if (!isFullScreenAdPresent(driver)) {
            return;
        }

        log.warn("🔔 Full-viewport iframe ad detected. Dismissing...");

        // Strategy 1: Click "Close" text near the iframe
        if (tryClickCloseButton(driver, FULLSCREEN_AD_CLOSE_LOCATORS)) {
            waitForFullScreenAdGone(driver);
            log.info("✅ Full-viewport ad dismissed via Close text click.");
            return;
        }

        // Strategy 2: JS hide – ẩn iframe khỏi màn hình
        hideFullScreenIframe(driver);
        log.info("✅ Full-viewport ad hidden via JavaScript.");
    }

    /**
     * Ẩn ALL inline banner ads trong DOM.
     *
     * Dùng khi test navigate đến ProductsPage và cần click các elements.
     * An toàn hơn dismissFullScreenAd vì không chờ animation.
     *
     * @param driver WebDriver hiện tại
     */
    public static void hideInlineAds(WebDriver driver) {
        try {
            String script =
                "document.querySelectorAll(" +
                "  'iframe[id*=\"aswift\"], iframe[id*=\"google_ads\"], .adsbygoogle'" +
                ").forEach(function(el) { " +
                "  el.style.display = 'none';" +
                "  el.style.pointerEvents = 'none';" +
                "});";
            ((JavascriptExecutor) driver).executeScript(script);
            log.debug("Inline ads hidden via JavaScript.");
        } catch (Exception e) {
            log.debug("Could not hide inline ads: {}", e.getMessage());
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /** Detect Vignette bằng URL fragment */
    private static boolean isVignettePresent(WebDriver driver) {
        return driver.getCurrentUrl().contains("google_vignette");
    }

    /**
     * Detect full-viewport iframe bằng JavaScript.
     * Check iframe aswift_N có width:100vw + height:100vh.
     */
    private static boolean isFullScreenAdPresent(WebDriver driver) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(FULLSCREEN_IFRAME_JS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.debug("Could not check for fullscreen ad: {}", e.getMessage());
            return false;
        }
    }

    /** Thử click Close button theo danh sách locators */
    private static boolean tryClickCloseButton(WebDriver driver, By[] locators) {
        for (By locator : locators) {
            try {
                WebElement btn = new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
                btn.click();
                log.debug("Clicked Close button: {}", locator);
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
            new WebDriverWait(driver, CLOSE_BUTTON_TIMEOUT)
                .until(d -> !d.getCurrentUrl().contains("google_vignette"));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Dùng history.replaceState() để xóa #google_vignette fragment khỏi URL.
     * Chỉ làm sạch URL bar, KHÔNG tự đóng overlay.
     * Phải gọi SAU KHI overlay đã được đóng thật.
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

    /** Chờ full-screen iframe biến mất khỏi DOM */
    private static void waitForFullScreenAdGone(WebDriver driver) {
        try {
            new WebDriverWait(driver, OVERLAY_GONE_TIMEOUT)
                .until(d -> !isFullScreenAdPresent(d));
        } catch (Exception ignored) {
            // Continue even if wait times out
        }
    }

    /** Ẩn full-viewport iframe bằng JS (fallback) */
    private static void hideFullScreenIframe(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(HIDE_FULLSCREEN_IFRAME_JS);
        } catch (Exception e) {
            log.warn("Could not hide fullscreen iframe: {}", e.getMessage());
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
