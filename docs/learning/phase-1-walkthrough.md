# Phase 1 – Foundation

## 🎯 Mục tiêu của Phase này

Xây dựng "bộ khung xương sống" (skeleton) của framework. Sau Phase này:
- Maven project **build được**
- Framework **chạy được** 3 test đầu tiên
- Không một dòng code nào hardcode URL, browser, hay timeout

---

## 📁 Files được tạo trong Phase 1

```
automation-exercise-framework/
├── pom.xml                                           ← Maven project config
├── .gitignore                                        ← Git ignore rules
├── .env.example                                      ← Config template
│
├── src/main/resources/
│   ├── config/local.properties                       ← Environment config
│   └── log4j2.xml                                    ← Logging config
│
├── src/main/java/com/automationexercise/
│   ├── config/ConfigManager.java                     ← Reads config with priority
│   ├── constants/FrameworkConstants.java             ← Timeout/path constants
│   ├── constants/RouteConstants.java                 ← URL paths
│   ├── driver/BrowserType.java                       ← Enum: CHROME/FIREFOX/EDGE
│   ├── driver/DriverFactory.java                     ← WebDriver lifecycle (ThreadLocal)
│   ├── pages/BasePage.java                           ← Reusable Selenium actions
│   ├── pages/HomePage.java                           ← Home page object
│   ├── pages/LoginPage.java                          ← Login page object
│   ├── pages/SignupPage.java                         ← Signup page object (minimal)
│   └── utils/RandomDataUtils.java                    ← Unique email/name generator
│
├── src/test/java/com/automationexercise/
│   ├── base/BaseTest.java                            ← @BeforeMethod / @AfterMethod
│   └── tests/authentication/LoginTest.java           ← 3 test cases
│
├── src/test/resources/
│   ├── testdata/users.json                           ← Test data (used in Phase 2)
│   └── suites/smoke.xml                              ← Smoke test suite
```

---

## 📚 Khái niệm quan trọng cần hiểu

### 1. Maven Project Structure – Tại sao có src/main và src/test?

Maven convention tách rõ:

```
src/main/java/    ← Production code (framework infrastructure)
                    - DriverFactory, BasePage, Page Objects, Config, Utils
                    - Code này KHÔNG phụ thuộc TestNG

src/test/java/    ← Test code (test cases)
                    - BaseTest, LoginTest
                    - Code này PHỤ THUỘC TestNG (@Test, @BeforeMethod...)
```

**Tại sao tách?**
- Code trong `src/main` có thể dùng lại cho nhiều project
- Khi build production JAR, test code không được đóng gói vào
- Rõ ràng về phân tách trách nhiệm

### 2. ThreadLocal trong DriverFactory – Khái niệm quan trọng

```java
// ThreadLocal: mỗi thread có một "ngăn riêng" để lưu giá trị
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
```

**Hình dung như thế này:**
```
Thread 1 (Test A) → Ngăn 1: ChromeDriver instance A
Thread 2 (Test B) → Ngăn 2: FirefoxDriver instance B
Thread 3 (Test C) → Ngăn 3: ChromeDriver instance C
```

Mỗi thread truy cập `driverThreadLocal.get()` sẽ nhận lại driver của chính nó, không bị lẫn với thread khác.

**Nếu không dùng ThreadLocal:**
```java
// ❌ SAI – tất cả test dùng chung 1 driver
private static WebDriver driver;

// Test A chạy → mở Chrome
// Test B chạy song song → cướp mất Chrome của Test A!
// → Test A bị fail ngẫu nhiên (flaky test)
```

**Với ThreadLocal:**
```java
// ✅ ĐÚNG – mỗi test có driver riêng
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
```

### 3. Configuration Priority – Cách ConfigManager hoạt động

```
Priority 1: System Property  → mvn test -Dbrowser=firefox
Priority 2: Properties file  → browser=chrome trong local.properties
Priority 3: Default value    → "chrome" trong code
```

**Ví dụ thực tế:**

```bash
# Trường hợp 1: Chạy mặc định → đọc local.properties → browser=chrome
mvn clean test

# Trường hợp 2: Override bằng CLI → dùng firefox bất kể local.properties nói gì
mvn clean test -Dbrowser=firefox

# Trường hợp 3: Chạy headless trên CI
mvn clean test -Dheadless=true -Dbrowser=chrome
```

ConfigManager đọc theo thứ tự này:
```java
public static String get(String key, String defaultValue) {
    // 1. Kiểm tra System property trước
    String systemValue = System.getProperty(key);   // từ -Dkey=value
    if (systemValue != null) return systemValue;

    // 2. Kiểm tra file properties
    String fileValue = properties.getProperty(key); // từ local.properties
    if (fileValue != null) return fileValue;

    // 3. Dùng default
    return defaultValue;
}
```

### 4. Page Object Model – Tại sao cần?

**Không dùng POM (Anti-pattern):**
```java
@Test
public void loginTest() {
    driver.findElement(By.cssSelector("input[data-qa='login-email']")).sendKeys("user@email.com");
    driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("pass123");
    driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();
    // Nếu selector thay đổi → phải tìm và sửa ở TẤT CẢ các test!
}
```

**Dùng POM (Pattern đúng):**
```java
// LoginPage.java
private static final By LOGIN_EMAIL = By.cssSelector("input[data-qa='login-email']");
public LoginPage enterLoginEmail(String email) {
    type(LOGIN_EMAIL, email);  // Selector chỉ ở một chỗ
    return this;
}

// LoginTest.java
@Test
public void loginTest() {
    loginPage.enterLoginEmail("user@email.com")  // Clean, readable
             .enterLoginPassword("pass123")
             .clickLoginButton();
    // Nếu selector thay đổi → sửa DUY NHẤT trong LoginPage.java
}
```

### 5. Explicit Wait – Tại sao KHÔNG dùng Thread.sleep()?

```java
// ❌ TUYỆT ĐỐI KHÔNG – Thread.sleep
Thread.sleep(5000); // Luôn chờ 5 giây dù page load trong 1 giây
                    // Lãng phí thời gian + flaky nếu mạng chậm > 5 giây

// ✅ ĐÚNG – Explicit Wait
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
// → Chờ tối đa 15 giây, nhưng sẽ tiếp tục ngay khi element xuất hiện
// → Nếu element hiện sau 2 giây → chờ 2 giây, không phải 5 giây
```

**BasePage.waitUntilVisible() hoạt động như thế này:**
```
Polling every 500ms:
  t=0.0s → check → not visible → wait
  t=0.5s → check → not visible → wait
  t=1.0s → check → VISIBLE! → return element immediately
  (không cần chờ đến 15 giây)
```

### 6. Test Independence – Tại sao mỗi test tự đăng ký user?

**❌ Pattern SAI (dependent tests):**
```java
// TC-002 tạo user
// TC-004 dùng user từ TC-002
// → Nếu TC-002 fail → TC-004 cũng fail dù code của TC-004 đúng!
```

**✅ Pattern ĐÚNG (independent tests):**
```java
// TC-002 tự đăng ký user → login → delete
// TC-004 tự đăng ký user → login → logout
// → Mỗi test hoàn toàn độc lập
// → Chạy song song được
// → Chạy theo thứ tự bất kỳ đều OK
```

**Unique email strategy:**
```java
// Timestamp trong email đảm bảo mỗi test run có email khác nhau
public static String generateUniqueEmail() {
    return "ae.test." + LocalDateTime.now().format("yyyyMMddHHmmss") + "@example.com";
}
// Run 1: ae.test.20260705183045@example.com
// Run 2: ae.test.20260705183102@example.com
// → Không bao giờ trùng
```

### 7. Fluent API – Tại sao method return "this" hoặc page object mới?

```java
// Pattern: mỗi action method return page object để chain
loginPage
    .enterLoginEmail(email)    // → return this (LoginPage)
    .enterLoginPassword(pass)  // → return this (LoginPage)
    .clickLoginButton();       // → return new HomePage (vì navigate sang trang mới)
```

**Tại sao return "this"?**
- Cho phép viết code dạng chain (method chaining)
- Code test đọc như một câu văn xuôi
- Dễ thêm step mới mà không phá vỡ existing code

**Tại sao một số method return page object MỚI?**
```java
// clickLoginButton() → return new HomePage(driver)
// Vì sau khi login, browser đang ở HomePage
// Trả về HomePage để test có thể thao tác với HomePage tiếp theo
```

---

## 🏃 Commands để chạy

### Chạy test lần đầu tiên
```bash
# Vào thư mục project
cd d:\Project_Automation_Testing\automation-exercise-framework

# Compile và chạy smoke suite
mvn clean test

# Chạy với Firefox
mvn clean test -Dbrowser=firefox

# Chạy headless (không mở browser)
mvn clean test -Dheadless=true

# Chạy chỉ một test cụ thể
mvn clean test -Dtest=LoginTest#loginShouldFailWithInvalidCredentials
```

### Kiểm tra build không có test
```bash
mvn clean compile
```

---

## ✅ Expected Results

Sau khi chạy `mvn clean test`:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running Smoke Test Suite

TC-AE-002 - Login User with correct email and password ... PASSED
TC-AE-003 - Login User with incorrect email and password ... PASSED
TC-AE-004 - Logout User ... PASSED

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

---

## ⚠️ Lỗi thường gặp và cách xử lý

### 1. `ChromeDriver version mismatch`
```
SessionNotCreatedException: Could not start a new session.
Chrome version must be between ...
```
**Nguyên nhân:** Chrome browser và ChromeDriver không khớp version.
**Giải pháp:** Selenium 4 tích hợp Selenium Manager tự động download driver. Hãy đảm bảo:
- Selenium version là 4.45.0 (mới nhất)
- Chrome browser được cập nhật

### 2. `Could not find or load config file`
```
WARN - Config file not found: 'config/local.properties'
```
**Nguyên nhân:** File `src/main/resources/config/local.properties` không tồn tại.
**Giải pháp:** File đã được tạo trong Phase 1. Kiểm tra lại cấu trúc folder.

### 3. `WebDriver is not initialized`
```
IllegalStateException: WebDriver is not initialized for this thread.
```
**Nguyên nhân:** `@BeforeMethod` không chạy trước test.
**Giải pháp:** Đảm bảo test class extends `BaseTest`.

### 4. `Element not found` (NoSuchElementException)
```
NoSuchElementException: no such element: Unable to locate element
```
**Nguyên nhân:** Locator sai hoặc element chưa xuất hiện.
**Giải pháp:** Dùng browser DevTools (F12) để verify selector. Explicit wait đã được BasePage xử lý, nhưng locator phải chính xác.

### 5. Build thành công nhưng test fail ngay từ đầu
**Kiểm tra:**
- Website `https://automationexercise.com` có accessible không?
- Có ad overlay che mất element không? (website này có nhiều quảng cáo)
- Thử chạy không headless để xem browser thực tế

---

## ✅ Checklist Phase 1

Trước khi sang Phase 2, xác nhận tất cả:

- [ ] `mvn clean compile` thành công (0 errors)
- [ ] `mvn clean test` chạy được 3 tests
- [ ] 3 tests đều PASS (TC-002, TC-003, TC-004)
- [ ] Không có hardcode URL trong bất kỳ file Java nào
- [ ] Không có hardcode browser trong bất kỳ file Java nào
- [ ] Không có `Thread.sleep()` ở bất kỳ đâu
- [ ] Log xuất hiện trong console (INFO level)
- [ ] File `reports/logs/test-execution.log` được tạo ra

---

## ❓ Câu hỏi tự kiểm tra

1. Tại sao `DriverFactory` không thể được instantiate (có `private constructor`)?
2. Nếu bạn chạy `mvn test -Dbrowser=firefox` nhưng `local.properties` có `browser=chrome`, trình tự nào sẽ thắng?
3. Tại sao `@AfterMethod(alwaysRun = true)` quan trọng? Điều gì xảy ra nếu không có `alwaysRun = true`?
4. `LoginPage.clickLoginButton()` trả về `HomePage` nhưng `clickLoginButtonExpectingFailure()` trả về `LoginPage`. Tại sao khác nhau?
5. Tại sao `BasePage` là `abstract`? Bạn có thể `new BasePage(driver)` không?

---

## 🚀 Phase tiếp theo: Phase 2 – Page Object & Test Data

Sau khi 3 tests pass ổn định, sang Phase 2:
- Thêm Jackson để đọc JSON
- Tạo Model classes (UserData, ProductData...)
- Tạo DataProvider để test chạy với nhiều data sets
- Tạo SignupPage đầy đủ
- Thêm 5–7 test cases mới

Hãy nói "Phase 2" khi sẵn sàng! 🎉
