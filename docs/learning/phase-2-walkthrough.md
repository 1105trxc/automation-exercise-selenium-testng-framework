# Phase 2 – Page Objects, Test Data & Architecture

## 🎯 Mục tiêu của Phase này

Phase 2 xây dựng từ skeleton của Phase 1 thành một framework thực sự có thể chạy end-to-end test. Sau Phase này:

- Framework chạy được **8 test cases** (TC-001 đến TC-008)
- Test data được đọc từ **JSON files**, không hardcode trong code
- Mỗi test **hoàn toàn độc lập**, không phụ thuộc test nào khác
- Có **Allure report** với screenshot khi fail
- Cơ sở hạ tầng (BasePage, BaseTest) đã được **refactor** theo đúng nguyên tắc thiết kế

---

## 📁 Files được tạo/thay đổi trong Phase 2

```
automation-exercise-framework/
├── src/main/java/com/automationexercise/
│   ├── components/
│   │   └── AdHandler.java                    ← [NEW] Xử lý Google Vignette ad
│   ├── models/
│   │   ├── UserData.java                     ← [NEW] POJO cho user registration
│   │   ├── LoginData.java                    ← [NEW] POJO cho login credentials
│   │   └── ContactFormData.java              ← [NEW] POJO cho contact form
│   ├── pages/
│   │   ├── BasePage.java                     ← [REFACTOR] Tách ad logic, sửa semantics
│   │   ├── SignupPage.java                   ← [EXPANDED] Full registration form
│   │   ├── ContactUsPage.java                ← [NEW] Contact Us page
│   │   ├── ProductsPage.java                 ← [NEW] All products page
│   │   └── ProductDetailPage.java            ← [NEW] Product detail page
│   └── utils/
│       ├── JsonDataReader.java               ← [NEW] Đọc JSON test data
│       └── RandomDataUtils.java              ← [UPDATED] Thêm generateName()
│
├── src/test/java/com/automationexercise/
│   ├── base/
│   │   └── BaseTest.java                     ← [REFACTOR] Abstract, driver() method
│   ├── dataproviders/
│   │   └── TestDataProvider.java             ← [NEW] @DataProvider cho TestNG
│   └── tests/
│       ├── registration/RegistrationTest.java ← [NEW] TC-001, TC-005
│       ├── authentication/LoginTest.java      ← [UPDATED] TC-002, TC-003, TC-004
│       ├── contact/ContactUsTest.java         ← [NEW] TC-006
│       └── products/ProductsTest.java         ← [NEW] TC-007, TC-008
│
├── src/test/resources/
│   └── testdata/
│       ├── users.json                         ← [NEW] User test data
│       └── contact_form.json                  ← [NEW] Contact form data
│
└── docs/
    ├── base-page-base-test-review.md          ← [NEW] Architecture review document
    └── learning/
        └── phase-2-walkthrough.md             ← File bạn đang đọc
```

---

## 📚 Khái niệm quan trọng cần hiểu

### 1. Model Class (POJO) – Tại sao cần?

POJO (Plain Old Java Object) là class đơn giản chỉ chứa data và getter/setter.

**Không dùng POJO:**
```java
// ❌ Dữ liệu lẫn lộn vào test
String name = jsonNode.get("name").asText();
String password = jsonNode.get("password").asText();
String country = jsonNode.get("country").asText();
// ...và còn 10 field nữa
```

**Dùng POJO:**
```java
// ✅ Jackson tự map JSON → Java object
UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
signupPage.fillRegistrationForm(user);  // clean, readable
```

Lợi ích:
- JSON thay đổi field → sửa một chỗ trong POJO
- Null-safe với `@JsonIgnoreProperties(ignoreUnknown = true)`
- IDE autocomplete hoạt động

### 2. @DataProvider – Chạy một test với nhiều bộ data

```java
// TestDataProvider.java
@DataProvider(name = "invalidLoginData")
public Object[][] provideInvalidLoginData() {
    LoginData[] data = JsonDataReader.readAll("users.json", "invalidLoginUsers", LoginData.class);
    return Arrays.stream(data).map(d -> new Object[]{d}).toArray(Object[][]::new);
}

// LoginTest.java
@Test(dataProvider = "invalidLoginData", dataProviderClass = TestDataProvider.class)
public void loginShouldFailWithInvalidCredentials(LoginData loginData) {
    // Test này tự động chạy NHIỀU LẦN – một lần cho mỗi row trong JSON
}
```

**Kết quả trong TestNG output:**
```
loginShouldFailWithInvalidCredentials[0] → PASS  (email: nonexistent@...)
loginShouldFailWithInvalidCredentials[1] → PASS  (email: another.invalid@...)
```

### 3. Test Independence – Mỗi test tự quản lý data của mình

**Quy tắc:** Không test nào được phụ thuộc vào kết quả của test khác.

**Ví dụ TC-AE-002 (Login):**
Test case yêu cầu login với email đúng. Nhưng nếu không có account → test fail.

**Cách xử lý đúng:**
```java
// Trong TC-AE-002: tự đăng ký user trước khi test login
// Không gọi @dependsOnMethods TC-001

public void loginWithValidCredentials() {
    // ARRANGE: Tự register một user mới
    String uniqueEmail = RandomDataUtils.generateUniqueEmail();
    // ... register flow ...

    // ACT: Logout rồi login lại
    LoginPage loginPage = homePage.clickLogout();
    homePage = loginPage.enterLoginEmail(uniqueEmail)
                        .enterLoginPassword(TEST_PASSWORD)
                        .clickLoginButton();

    // ASSERT: Kiểm tra đã login
    Assert.assertTrue(homePage.isUserLoggedIn());

    // CLEANUP: Xóa account sau khi dùng xong
    homePage.clickDeleteAccount();
}
```

### 4. Fluent Page Object – Method chaining theo trạng thái trang

Mỗi method của Page Object trả về:
- `return this` – nếu vẫn ở cùng trang
- `return new OtherPage(driver)` – nếu navigate sang trang mới

```java
// Pattern thực tế trong framework:
HomePage homePage = loginPage
    .enterSignupName(uniqueName)       // return this (LoginPage)
    .enterSignupEmail(uniqueEmail)     // return this (LoginPage)
    .clickSignupButton();              // return new SignupPage(driver)
    .fillRegistrationForm(user)        // return this (SignupPage)
    .clickCreateAccount()              // return this (SignupPage – ở trang xác nhận)
    .clickContinue();                  // return new HomePage(driver)
```

### 5. AdHandler – Tại sao tách khỏi BasePage?

**Vấn đề ban đầu:**
`BasePage` chứa `dismissVignetteAd()` và `removeAds()` – logic đặc thù của `automationexercise.com`.

**Tại sao sai về kiến trúc:**
- `BasePage` là generic infrastructure – nên dùng được ở bất kỳ project nào
- Logic quảng cáo chỉ đúng với `automationexercise.com` (demo site)
- Làm `BasePage` phình to, khó maintain

**Giải pháp:**
```
components/AdHandler.java  ← Chứa toàn bộ logic vignette
pages/BasePage.java        ← Chỉ có 1 dòng proxy: handleVignette() → AdHandler.dismissIfPresent(driver)
```

**Chiến lược đúng của AdHandler (theo thứ tự):**
```
1. Phát hiện vignette qua URL fragment (#google_vignette)
2. Thử click nút Close → overlay bị đóng thật
3. Chờ overlay biến mất
4. Dùng history.replaceState() để làm sạch URL fragment
5. Thử Escape key nếu Close button không tìm được
6. Fallback cuối: navigate đến clean URL (reload trang)
```

> **Quan trọng:** `history.replaceState()` chỉ xóa URL fragment (#google_vignette).
> Nó KHÔNG xóa overlay đang che màn hình. Phải đóng overlay thật TRƯỚC, sau đó mới gọi replaceState().

### 6. isDisplayedNow() vs isDisplayed() – Tại sao cần phân biệt?

**Vấn đề cũ:** `BasePage.isDisplayed(By)` dùng `driver.findElement().isDisplayed()` – kiểm tra tức thì, KHÔNG wait. Nhưng comment nói "dùng default explicit wait" → Sai.

**Phân biệt sau refactor:**

| Method | Behavior | Khi nào dùng |
|---|---|---|
| `isDisplayedNow(By)` | Tức thì, không wait | Negative assertion: kiểm tra element KHÔNG tồn tại |
| `isDisplayed(By)` | Wait default 15s | Positive assertion: kiểm tra element PHẢI tồn tại |
| `isDisplayed(By, int)` | Wait custom timeout | Khi biết cần bao lâu (ví dụ: slow network = 20s) |

**Ví dụ sử dụng:**
```java
// ✅ ĐÚNG: Kiểm tra element phải có mặt (có thể cần đợi page load)
Assert.assertTrue(homePage.isUserLoggedIn(), "Should be logged in");
// isUserLoggedIn() gọi isDisplayed(LOGOUT_LINK) → wait 15s

// ✅ ĐÚNG: Kiểm tra element không được tồn tại (không muốn chờ 15s vô ích)
Assert.assertFalse(page.isDisplayedNow(LOGIN_BUTTON), "Should be logged out");
// Kiểm tra tức thì, nếu không có → return false ngay, không chờ 15s
```

### 7. click() đơn giản – Tại sao không tự fallback JavaScript?

**Cũ:**
```java
protected void click(By locator) {
    try {
        dismissVignetteAd();
        waitUntilClickable(locator).click();
    } catch (ElementClickInterceptedException e) {
        removeAds();
        try {
            waitUntilClickable(locator).click();
        } catch (Exception ex) {
            jsClick(locator);  // ← Tự động fallback
        }
    }
}
```

**Vấn đề:** `catch (Exception ex) { jsClick() }` bắt **mọi loại exception**:
- Locator sai → JS click vẫn chạy, test tiếp tục → **bug thật bị che giấu**
- Element disabled → JS click bypass disabled state → **test pass nhưng UI có bug**
- Page chưa load → JS click → **action sai context**

**Sau refactor:**
```java
// click() – đơn giản, fail-fast
protected void click(By locator) {
    waitUntilClickable(locator).click();
    log.debug("Clicked: {}", locator);
}

// clickWithJsFallback() – opt-in, chỉ dùng khi đã biết thực sự cần
protected void clickWithJsFallback(By locator) {
    try {
        waitUntilClickable(locator).click();
    } catch (ElementClickInterceptedException e) {
        log.warn("⚠️ Click intercepted. Using JS fallback (verify this is expected): {}", locator);
        jsClick(locator);
    }
}
```

### 8. protected WebDriver driver field vs driver() method

**Vấn đề với field:**
```java
// BaseTest.java
protected WebDriver driver;  // ← Instance field

// setUp()
DriverFactory.initDriver(browserType);
driver = DriverFactory.getDriver();  // ← Copy reference vào field
```

Khi chạy `parallel="methods"`:
```
Thread 1: driver = ChromeDriver A
Thread 2: driver = ChromeDriver B
Thread 1: đọc this.driver → có thể nhận được B (race condition)
```

**Giải pháp:**
```java
// BaseTest.java
protected WebDriver driver() {
    return DriverFactory.getDriver();  // Luôn lấy từ ThreadLocal của thread hiện tại
}

// Test class
HomePage homePage = new HomePage(driver());  // Luôn đúng driver
```

### 9. DriverFactory.quitDriver() – Tại sao cần finally?

```java
// ❌ Cũ – ThreadLocal leak nếu quit() throw exception
public static void quitDriver() {
    WebDriver driver = driverThreadLocal.get();
    if (driver != null) {
        driver.quit();               // ← Nếu đây throw exception...
        driverThreadLocal.remove();  // ← Dòng này không bao giờ chạy!
    }
}
// Kết quả: Stale driver reference còn trong ThreadLocal
// Test tiếp theo của thread này nhận được driver đã chết
```

```java
// ✅ Mới – ThreadLocal.remove() LUÔN chạy dù quit() có fail
public static void quitDriver() {
    WebDriver driver = driverThreadLocal.get();
    if (driver == null) return;
    try {
        driver.quit();
    } catch (Exception e) {
        log.warn("Exception while quitting driver: {}", e.getMessage());
    } finally {
        driverThreadLocal.remove();  // ← Guaranteed
    }
}
```

---

## 🏃 Commands

```bash
# Chạy tất cả test
mvn clean test

# Chạy chỉ một test class
mvn clean test -Dtest=RegistrationTest

# Chạy chỉ một test method
mvn clean test -Dtest=LoginTest#loginShouldFailWithInvalidCredentials

# Xem Allure report
mvn allure:serve

# Chạy headless
mvn clean test -Dheadless=true

# Override browser từ CLI (priority cao nhất)
mvn clean test -Dbrowser=firefox
```

---

## ✅ Expected Results (TC-001 đến TC-008)

```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
Time elapsed: ~120 sec

TC-AE-001 - Register User                              PASSED
TC-AE-002 - Login User with correct email              PASSED
TC-AE-003 - Login with incorrect[0] (wrong email)      PASSED
TC-AE-003 - Login with incorrect[1] (wrong password)   PASSED
TC-AE-004 - Logout User                                PASSED
TC-AE-005 - Register with existing email               PASSED
TC-AE-006 - Contact Us Form                            PASSED
TC-AE-007 - Navigate to Test Cases Page                PASSED
TC-AE-008 - All Products and Product Detail            PASSED
```

> TC-AE-003 chạy 2 lần vì dùng `@DataProvider` với 2 data rows trong `users.json`.

---

## ⚠️ Known Issues & Workarounds

### Google Vignette Ad

**Triệu chứng:** Test đứng lại hoặc `ElementNotInteractableException` sau khi navigate về trang chủ.

**URL khi có vignette:**
```
https://automationexercise.com/#google_vignette
```

**Xử lý:** `AdHandler.dismissIfPresent()` được gọi tự động:
1. `BaseTest.setUp()` – sau khi mở trang chủ lần đầu
2. `SignupPage.clickContinue()` – sau khi tạo account xong, về trang chủ
3. `ContactUsPage.clickHome()` – sau khi submit form, về trang chủ

### CSS text-transform: uppercase

**Triệu chứng:** `isAccountCreatedMessageVisible()` fail dù text hiển thị đúng.

**Nguyên nhân:** CSS `text-transform: uppercase` làm browser hiển thị "ACCOUNT CREATED!" nhưng DOM thực chứa "Account Created!". XPath `text()` match DOM, không match display.

**Fix:**
```java
// ❌ Sai – match display text, không match DOM
By.xpath("//h2[contains(text(),'ACCOUNT CREATED!')]")

// ✅ Đúng – match class/structure, không match text
By.xpath("//h2[contains(@class,'title')]//b")
```

---

## ✅ Checklist Phase 2

- [x] `mvn clean compile` thành công (0 errors)
- [x] TC-001 đến TC-008 tất cả PASS
- [x] TC-003 chạy 2 lần qua @DataProvider
- [x] Không có hardcode credential trong test class
- [x] Không có `Thread.sleep()` trong bất kỳ file nào
- [x] Không có `catch (Exception)` trong `click()` che giấu lỗi
- [x] `type()` không log nội dung password
- [x] `isDisplayedNow()` và `isDisplayed()` semantics rõ ràng
- [x] Ad logic tách khỏi `BasePage` vào `AdHandler`
- [x] `BaseTest` là abstract class
- [x] `driver()` method thay `protected WebDriver driver` field
- [x] Browser priority: CLI > XML > config > default
- [x] `DriverFactory.quitDriver()` có finally block
- [x] Allure report hiển thị đúng TC name, Epic, Feature, Severity

---

## ❓ Câu hỏi tự kiểm tra

1. Tại sao TC-AE-002 phải tự đăng ký user thay vì dùng user đã có từ TC-AE-001?
2. `@DataProvider` giúp gì so với viết 2 method test riêng biệt?
3. Tại sao `history.replaceState()` một mình không đủ để xử lý Google Vignette?
4. Sự khác biệt giữa `isDisplayedNow()` và `isDisplayed(By, 0)` là gì?
5. Nếu `DriverFactory.quitDriver()` không có `finally`, điều gì xảy ra khi chạy 100 test?
6. Tại sao `clickWithJsFallback()` không được gọi tự động trong `click()`?
7. `BaseTest.resolveBrowser()` xử lý thứ tự priority như thế nào?

---

## 🚀 Phase tiếp theo: Phase 3 – Cart & Checkout

Các test case phức tạp nhất:
- TC-AE-009: Search product → cần `ProductsPage.searchProduct()`
- TC-AE-012: Add to cart → cần `CartPage`
- TC-AE-014: Place order → end-to-end flow dài nhất
- TC-AE-023: Verify address in checkout → cần `CheckoutPage`
