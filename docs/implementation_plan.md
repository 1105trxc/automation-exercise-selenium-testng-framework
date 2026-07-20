# Kế Hoạch Sửa Code Theo HUONG_DAN_SUA_CODE_AUTOMATION_FRAMEWORK_HOAN_CHINH.md

## Tóm tắt vấn đề

Sau đợt Refactor & Hardening trước, code đã đi lệch khỏi các nguyên tắc kiến trúc quan trọng. Tài liệu hướng dẫn yêu cầu sửa 10 nhóm vấn đề (P0-FIX-00 → P0-FIX-10) theo thứ tự nghiêm ngặt.

---

## Tiến Độ Triển Khai Thực Tế (Progress Status)

### ✅ ĐÃ HOÀN THÀNH:

1. **P0-FIX-00: Baseline & Guardrails**
   - Đã quét static analysis và kiểm tra compile.

2. **P0-FIX-01: Sửa Add-to-cart đúng root cause**
   - `AddToCartModal.java`: Sửa `clickContinueShopping()` để wait cho modal hoàn toàn `invisibilityOfElementLocated` trước khi return `ProductsPage`. Bỏ `Thread.sleep(500)`.
   - `ProductsPage.java`: Xóa `addAllSearchedProductsToCart()` (chứa retry 3 lần, JS click, `$('#cartModal').modal('hide')`). Thêm `addSearchedProductAt(int)` để thực hiện đơn thao tác chuẩn (scroll -> hover -> native click).
   - `CartFlow.java`: Thêm `addAllVisibleSearchedProducts(ProductsPage)` chuyển trách nhiệm lặp và điều phối flow về đúng lớp Flow.
   - `ProductSearchTest.java`: Cập nhật TC-AE-020 sử dụng `CartFlow`.

3. **P0-FIX-02: Thu hẹp AdHandler & AEBasePage.click()**
   - `AEBasePage.java`: Xóa post-click URL check `#google_vignette` và re-click tự động sau khi click thành công (tránh double submit). Đặt hợp đồng click rõ ràng: chỉ retry 1 lần DUY NHẤT khi catch `ElementClickInterceptedException` đến từ 3rd-party ad đã được chứng minh.
   - `AdHandler.java`: Xóa `querySelectorAll('div')` quét toàn DOM, xóa reset style `html/body`. Thu hẹp selector chỉ nhắm đến iframe Google Ad (`aswift_`, `google_ads_`, `adsbygoogle`). Xóa dead code (`IFRAME_DETECT_TIMEOUT`, `isFullScreenAdPresent()`, `waitForFullScreenAdGone()`).

4. **P0-FIX-03: Account Cleanup Service per-test/thread-safe**
   - `AccountCleanupService.java`: Chuyển registry sang `ThreadLocal<Map<String, String>>` đảm bảo mỗi thread test hoàn toàn độc lập (parallel-safe).
   - Đã tạo record `CleanupResult(int attempted, List<String> failedEmails)`.
   - Cập nhật `deleteAccountViaApi()` parse JSON chính xác (`responseCode == 200` và `message` chứa "deleted"). Bắt cụ thể `IOException` và restore interrupt cho `InterruptedException`. Base URL lấy từ `ConfigManager`.
   - `BaseTest.java`: Cập nhật `tearDown()` sử dụng `AccountCleanupService.cleanupCurrentTestAccounts()`.

5. **P0-FIX-04: Xóa Thread.sleep & Sửa DownloadManager**
   - `DownloadManager.java`: Xóa bỏ toàn bộ `Thread.sleep()`. Thay thế bằng Selenium `FluentWait` polling 250ms kiểm tra file tồn tại, non-empty, không còn file tạm `.crdownload` và size ổn định qua 2 lần poll.
   - Thư mục download lấy từ `ConfigManager` (`downloadDir`), dùng NIO `Files.readString(..., StandardCharsets.UTF_8)` đọc file. `cleanDownloadDirectory()` throw `IllegalStateException` nếu không xóa được file rác.

6. **P0-FIX-05: Truthful Page Object transitions**
   - Đã tạo mới 3 Page Objects phản ánh đúng trạng thái thực tế:
     - `AccountCreatedPage.java` (`/account_created`)
     - `AccountDeletedPage.java` (`/delete_account`)
     - `TestCasesPage.java` (`/test_cases`)
   - `SignupPage.java`: `clickCreateAccount()` trả về `AccountCreatedPage` (thay vì `SignupPage`). Xóa `clickContinue()` khỏi `SignupPage` vì nút này thuộc `AccountCreatedPage`.
   - `HeaderComponent.java`: `clickDeleteAccount()` trả về `AccountDeletedPage`, `clickTestCases()` trả về `TestCasesPage`.
   - `UserFlow.java`: `registerNewUser()` fail-fast, tự động register cleanup trước khi click submit, trả về `AccountCreatedPage`. Thêm `loginSuccessfully()` cho positive flow và `submitInvalidLogin()` cho negative test.
   - `HomePage.java`: Xóa `ACCOUNT_DELETED_MSG` và `isAccountDeletedMessageVisible()` (đã chuyển về `AccountDeletedPage`).
   - `RegistrationTest.java` & `ProductSearchTest.java`: Cập nhật toàn bộ callers sang đúng Page Objects mới.

7. **P0-FIX-06: Hoàn thiện Assertions (Một phần)**
   - `CartPage.java`: Tạo record `CartItemSnapshot(name, unitPrice, quantity, total)` và method `getCartItems()` trả về danh sách snapshot.
   - `ProductSearchTest.java`: TC-AE-020 sử dụng `getCartItems()` so sánh chính xác danh sách sản phẩm trước và sau khi đăng nhập (không chỉ so sánh `getProductCount()`).
   - `ProductsPage.java`: Thêm `getBrandNameAt(int)` và `clickBrandAt(int)` chuẩn hóa tên brand (xóa tiền tố `(6)`).

---

### ⏳ ĐANG TIẾP TỤC THỰC HIỆN:
- **P0-FIX-06 (tiếp theo)**: Sửa `HomePage.isHeroTextVisible()` kiểm tra đầy đủ `display`, `visibility`, `opacity`, `width/height` và viewport boundary.
- **P0-FIX-07**: Audit exception handling và dead code còn lại.
- **P0-FIX-08**: Audit & document config options.
- **P0-FIX-09**: Dọn dẹp comment thừa.
- **P0-FIX-10**: Thực thi Smoke & Regression 3 lần liên tiếp để thu thập bằng chứng PASS.

---

## Phát hiện: Những gì cần sửa (Anti-patterns hiện tại)

### BLOCKER — Vi phạm nguyên tắc kiến trúc nghiêm trọng

| # | File | Vấn đề |
|---|------|--------|
| 1 | `ProductsPage.addAllSearchedProductsToCart()` | Có `Thread.sleep(500)`, `JavascriptExecutor.click()`, retry loop 3 lần, `$('#cartModal').modal('hide')` — tất cả đều BỊ CẤM |
| 2 | `AEBasePage.click()` | Post-click URL check dẫn đến re-click tự động sau click thành công — có thể submit action 2 lần |
| 3 | `AdHandler` `HIDE_FULLSCREEN_IFRAME_JS_SCRIPT` | `querySelectorAll('div')` quét toàn bộ DOM, ẩn mọi fixed div z-index cao — có thể ẩn app modal |
| 4 | `AdHandler.navigateToCleanUrl()` | Reset `html/body` style rộng (`overflow`, `height`, `width`) — thay đổi layout trang không có evidence |
| 5 | `AccountCleanupService` | Global `ConcurrentHashMap` — cleanup global xóa account của test khác khi parallel |
| 6 | `AccountCleanupService.deleteAccountViaApi()` | `catch(Exception)` nuốt mọi lỗi, check `response.body().contains("200")` quá yếu |
| 7 | `DownloadManager.waitForDownload()` | `Thread.sleep(1000)` + `Thread.sleep(500)`, `file.delete()` không kiểm tra kết quả |
| 8 | `UserFlow.registerNewUser()` | Kiểm tra URL rồi silently fail, trả về `SignupPage` ngay cả khi tạo account thất bại |
| 9 | `HeaderComponent.clickDeleteAccount()` | Return `new HomePage(driver)` là sai semantics — thực tế đang ở `AccountDeletedPage` |
| 10 | `HeaderComponent.clickTestCases()` | Return `new HomePage(driver)` là sai — thực tế đang ở `TestCasesPage` |
| 11 | `SignupPage.clickCreateAccount()` | Return `SignupPage` là sai — thực tế đang ở `AccountCreatedPage` |
| 12 | `BaseTest.tearDown()` | Gọi `cleanupAllRegisteredAccounts()` global thay vì per-test |
| 13 | `HomePage` | Chứa `ACCOUNT_DELETED_MSG` và `isAccountDeletedMessageVisible()` — không nên có ở đây |
| 14 | `AddToCartModal.clickContinueShopping()` | Không chờ modal biến mất trước khi return |

### MAJOR — Cần sửa nhưng không phá vỡ ngay

| # | File | Vấn đề |
|---|------|--------|
| 15 | `ProductsPage` | Không có `addSearchedProductAt(int)` — Page orchestrate toàn bộ flow thay vì `CartFlow` |
| 16 | `CartFlow` | Không có `addAllVisibleSearchedProducts()` — thiếu Flow orchestration |
| 17 | `AccountCleanupService` | URL hard-code `https://automationexercise.com` thay vì lấy từ `ConfigManager` |
| 18 | `DownloadManager` | Path `target/downloads` hard-code, không lấy từ config; `readFileContent` dùng default charset |
| 19 | `AdHandler.HIDE_FULLSCREEN_IFRAME_JS` | Dead code — `HIDE_FULLSCREEN_IFRAME_JS` (cái cũ) vs `HIDE_FULLSCREEN_IFRAME_JS_SCRIPT` (cái mới) |
| 20 | `AdHandler.isFullScreenAdPresent()` | Dead code — luôn trả về `false` |
| 21 | `AdHandler.waitForFullScreenAdGone()` | Dead code — không có caller thực |
| 22 | `ProductSearchTest.searchAndVerifyCartAfterLogin()` | Assert `cartPage.getProductCount()` — false PASS nếu sản phẩm đúng số nhưng sai tên |
| 23 | `HomPage.isHeroTextVisible()` | JS chỉ check `rect.top` — thiếu check `display`, `visibility`, `opacity`, `width/height` |

---

## Những gì KHÔNG sửa (đang tốt)

- `ConfigManager` cho phép CLI override
- `RandomDataUtils` dùng UUID
- Cart math assertion (Price × Quantity = Total)
- Exact username assertion
- `DriverFactory` dùng `ThreadLocal`
- `HeaderComponent.getLoggedInUsername()` — đúng
- Scroll mechanism `waitForScrollToTop()` — đúng về approach

---

## Thứ tự triển khai

### P0-FIX-00: Baseline
- Chạy smoke + regression để ghi nhận baseline hiện tại

### P0-FIX-01: Add-to-cart (Xóa retry/JS/force-hide)
**Files sửa:**
- `ProductsPage.java` — thêm `addSearchedProductAt(int)`, xóa `addAllSearchedProductsToCart()`
- `CartFlow.java` — thêm `addAllVisibleSearchedProducts(ProductsPage)`
- `AddToCartModal.java` — `clickContinueShopping()` phải chờ modal invisible

**Files gọi sửa theo:**
- `ProductSearchTest.java` — dùng `CartFlow.addAllVisibleSearchedProducts()`

### P0-FIX-02: AdHandler thu hẹp + AEBasePage.click() sửa
**Files sửa:**
- `AEBasePage.java` — xóa post-click URL check và re-click tự động
- `AdHandler.java` — xóa `querySelectorAll('div')`, xóa html/body style reset, xóa dead code

### P0-FIX-03: AccountCleanupService per-thread
**Files sửa:**
- `AccountCleanupService.java` — ThreadLocal, proper exception handling, JSON parse
- `UserFlow.java` — `registerNewUser()` tự register cleanup, fail-fast, trả về `AccountCreatedPage`
- `BaseTest.java` — dùng `cleanupCurrentTestAccounts()`

### P0-FIX-04: Xóa Thread.sleep, sửa DownloadManager
**Files sửa:**
- `DownloadManager.java` — FluentWait thay Thread.sleep, NIO, config-driven path, UTF-8

### P0-FIX-05: Truthful Page Object transitions
**Files tạo mới:**
- `AccountCreatedPage.java` — [NEW]
- `AccountDeletedPage.java` — [NEW]
- `TestCasesPage.java` — [NEW]

**Files sửa:**
- `SignupPage.java` — `clickCreateAccount()` trả về `AccountCreatedPage`
- `HeaderComponent.java` — `clickDeleteAccount()` trả về `AccountDeletedPage`, `clickTestCases()` trả về `TestCasesPage`
- `UserFlow.java` — `loginSuccessfully()` thay vì `login()`
- `HomePage.java` — xóa `ACCOUNT_DELETED_MSG` và `isAccountDeletedMessageVisible()`

**Files gọi sửa theo:**
- Tất cả test callers của `clickDeleteAccount()`, `clickTestCases()`, `clickCreateAccount()`

### P0-FIX-06: Strengthened assertions
**Files sửa:**
- `HomePage.java` — `isHeroTextVisible()` full viewport check
- `ProductsPage.java` — `getBrandNameAt(int)`, `clickBrandAt(int)`
- `CartPage.java` — `getCartItems()` trả về `List<CartItemSnapshot>`
- Tests update để dùng `CartItemSnapshot` comparison

### P0-FIX-07: Exception handling + dead code
**Files sửa:**
- `AdHandler.java` — xóa dead code đã confirm
- `AEBasePage.java` — xóa `catch(Exception ignored)`

### P0-FIX-08: Config + Browser consistency
- Document rõ browser constraint cho download test

### P0-FIX-09: Comment cleanup
- Xóa redundant step-by-step comments

### P0-FIX-10: Chạy regression 3 lần, ghi evidence

---

## Open Questions

> [!IMPORTANT]
> **Câu hỏi 1**: Một số test dùng `signupPage.clickContinue()` thay vì `signupPage.clickCreateAccount()` → `accountCreatedPage.clickContinue()`. Khi tách `AccountCreatedPage`, cần review tất cả callers.

> [!IMPORTANT]
> **Câu hỏi 2**: Tài liệu yêu cầu `UserFlow.loginSuccessfully()` nhưng negative tests hiện tại vẫn dùng `UserFlow.login()`. Tôi sẽ **giữ `login()` method cũ** cho negative tests và thêm `loginSuccessfully()` cho positive flow — tránh break existing tests.

> [!WARNING]
> **Câu hỏi 3**: `ProductSearchTest.searchAndVerifyCartAfterLogin()` hiện assert bằng `getProductCount()`. Tài liệu yêu cầu dùng `CartItemSnapshot`. Tuy nhiên điều này sẽ yêu cầu sửa cả `CartPage` và test. Tôi sẽ implement `CartItemSnapshot` và `getCartItems()` nhưng cần test thực tế để xác nhận locators.

> [!CAUTION]
> **TC-020 test flow**: `UserFlow.registerNewUser()` sẽ trả về `AccountCreatedPage` sau khi sửa, nhưng test hiện tại assign vào `SignupPage`. Cần update test.

---

## Verification Plan

### Sau mỗi P0-FIX
```bash
mvn clean test-compile
mvn test -Dtest=<focused_test>
```

### Sau P0-FIX-10
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
mvn clean test -DsuiteXmlFile=src/test/resources/suites/regression.xml  # Chạy 3 lần
```
