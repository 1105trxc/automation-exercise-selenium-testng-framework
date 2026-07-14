package com.automationexercise.pages;

import com.automationexercise.components.AdHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AEBasePage – Site-specific base page cho automationexercise.com.
 *
 * TẠI SAO TẠO CLASS NÀY:
 * BasePage là generic infrastructure, không biết gì về ads của một website cụ thể.
 * AEBasePage là lớp trung gian chứa logic đặc thù của automationexercise.com.
 *
 * VẤN ĐỀ NÓ GIẢI QUYẾT:
 * automationexercise.com hiển thị full-viewport iframe ads (aswift_N) che toàn
 * màn hình SAU KHI trang load, không có #google_vignette trong URL.
 * Những ads này gây ElementClickInterceptedException trên bất kỳ click nào.
 *
 * THIẾT KẾ:
 * Override click() để dismiss full-screen ad TRƯỚC mỗi lần click.
 * Tất cả Page Objects của project này extend AEBasePage thay vì BasePage trực tiếp.
 * → Không page nào phải tự lo về ads. DRY được bảo đảm.
 *
 * TẠI SAO KHÔNG ĐẶT TRONG BasePage:
 * BasePage phải reusable cho nhiều project khác nhau.
 * Đặt site-specific logic vào BasePage là vi phạm Single Responsibility.
 */
public abstract class AEBasePage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(AEBasePage.class);

    protected AEBasePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Override click() để tự động dismiss full-viewport ads trước khi click.
     *
     * LÝ DO:
     * - Full-viewport iframe (100vw/100vh) có thể xuất hiện bất cứ lúc nào
     *   trên automationexercise.com, đặc biệt trước khi click elements.
     * - Dismiss xảy ra nhanh (2-3s) nếu ad đang hiện, là no-op nếu không có.
     * - Thay thế pattern copy-paste AdHandler calls ở mỗi method.
     *
     * CHÚ Ý:
     * Không gọi dismissIfPresent() ở đây vì nó dùng URL check (#google_vignette)
     * và chỉ nên được gọi lúc navigate (đã có trong BaseTest.setUp()).
     */
    @Override
    protected void click(By locator) {
        AdHandler.dismissFullScreenAd(driver);
        super.click(locator);
    }
}
