# Review và Đề xuất Tối ưu `BasePage` và `BaseTest`

## 1. Mục đích tài liệu

Tài liệu này tổng hợp toàn bộ phần review kỹ thuật đối với hai file:

- `BasePage.java`
- `BaseTest.java`

Mục tiêu là đánh giá:

- Mức độ đúng về kiến trúc
- Khả năng tái sử dụng
- Khả năng bảo trì
- Khả năng mở rộng
- Khả năng chạy song song
- Nguy cơ flaky test
- Nguy cơ che giấu lỗi
- Mức độ phù hợp với một Automation Framework dùng cho Portfolio Fresher QA

---

# 2. Đánh giá tổng quan

## `BaseTest`

Đánh giá đề xuất:

```text
8/10
```

Điểm mạnh:

- Tách setup và teardown rõ ràng
- Sử dụng `DriverFactory`
- Dùng `@BeforeMethod`
- Dùng `@AfterMethod(alwaysRun = true)`
- Có logging
- Không chứa locator
- Không chứa business logic
- Không chứa test data
- Có browser configuration
- Phù hợp với TestNG

Điểm cần cải thiện chính:

- Biến `protected WebDriver driver` chưa an toàn khi chạy parallel
- Browser priority chưa thống nhất
- `baseUrl` vẫn đang hardcode trong Java
- Nên chuyển thành abstract class
- Nên log rõ test method hoặc giao việc đó cho listener

---

## `BasePage`

Đánh giá đề xuất:

```text
6.5–7/10
```

Điểm mạnh:

- Là abstract class
- Có explicit wait
- Có reusable actions
- Không chứa assertion
- Không chứa test data
- Có logging
- Có scroll, dropdown, hover, alert helper
- Có mô tả trách nhiệm khá rõ

Điểm cần cải thiện chính:

- Logic quảng cáo đang làm `BasePage` phụ thuộc riêng vào Automation Exercise
- `click()` đang che giấu quá nhiều loại lỗi
- Có JavaScript fallback quá rộng
- Có `Thread.sleep`
- `type()` log toàn bộ dữ liệu người dùng
- `isDisplayed()` không đúng với comment
- Có dấu hiệu trở thành God Class
- Một số method không wait trước khi thao tác

---

# 3. Vấn đề ưu tiên cao

## 3.1 Logic quảng cáo không nên nằm trong `BasePage`

Hiện tại `BasePage` chứa:

- `removeAds()`
- `dismissVignetteAd()`
- Locator riêng cho quảng cáo
- Logic riêng cho `google_vignette`
- Logic thao tác DOM dành riêng cho Automation Exercise

Điều này làm giảm tính tái sử dụng.

`BasePage` nên chỉ chứa hành vi generic:

```text
BasePage
├── click
├── type
├── getText
├── wait
├── scroll
├── select
└── page information
```

Logic quảng cáo nên được tách thành:

```text
components/
└── AdHandler.java
```

hoặc:

```text
pages/
├── BasePage.java
└── AutomationExercisePage.java
```

### Đề xuất

```java
public final class AdHandler {

    private static final Logger log =
            LoggerFactory.getLogger(AdHandler.class);

    private AdHandler() {
    }

    public static void dismissIfPresent(WebDriver driver) {
        String currentUrl = driver.getCurrentUrl();

        if (!currentUrl.contains("google_vignette")) {
            return;
        }

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(
                    "history.replaceState(null, '', " +
                    "window.location.pathname + window.location.search);"
            );

            log.info("Google vignette URL fragment removed.");
        } catch (JavascriptException exception) {
            log.warn(
                    "Unable to dismiss Google vignette: {}",
                    exception.getMessage()
            );
        }
    }
}
```

Không nên tự động xử lý quảng cáo trước mọi thao tác click.

---

## 3.2 `click()` đang che giấu quá nhiều lỗi

Flow hiện tại:

```text
Click bình thường
→ Nếu intercept thì xóa quảng cáo
→ Click lại
→ Nếu bất kỳ exception nào xảy ra thì JavaScript click
```

Vấn đề:

```java
catch (Exception ex) {
    jsClick(locator);
}
```

Đoạn này có thể bắt cả:

- `TimeoutException`
- `NoSuchElementException`
- `StaleElementReferenceException`
- Locator sai
- Element disabled
- Page chưa load đúng
- UI có bug thật

Sau đó JavaScript click có thể khiến test tiếp tục chạy và che giấu lỗi.

### Đề xuất

Giữ `click()` đơn giản:

```java
protected void click(By locator) {
    try {
        waitUntilClickable(locator).click();
        log.debug("Clicked element: {}", locator);
    } catch (ElementClickInterceptedException exception) {
        throw new ElementActionException(
                "Element was intercepted and could not be clicked: "
                        + locator,
                exception
        );
    }
}
```

Tạo fallback riêng:

```java
protected void clickWithJavaScriptFallback(By locator) {
    try {
        click(locator);
    } catch (ElementClickInterceptedException exception) {
        log.warn(
                "Normal click failed. Using JavaScript fallback: {}",
                locator
        );

        jsClick(locator);
    }
}
```

Chỉ dùng fallback ở nơi đã xác định thực sự cần.

---

## 3.3 Không nên xóa quảng cáo khỏi DOM quá rộng

Selector như:

```text
[id^=ad_]
[class^=ad-]
```

có thể xóa nhầm element hợp lệ.

Việc:

```javascript
parentNode.removeChild(ad)
```

cũng làm thay đổi DOM so với trải nghiệm người dùng thật.

Nếu cần workaround cho website demo:

- Tách khỏi `BasePage`
- Chỉ xử lý element quảng cáo đã biết
- Không dùng selector quá rộng
- Ghi rõ đây là giới hạn môi trường demo
- Không để workaround che lỗi click

---

## 3.4 Loại bỏ `Thread.sleep(500)`

Class tuyên bố không dùng `Thread.sleep`, nhưng vẫn có:

```java
Thread.sleep(500);
```

Nên thay bằng explicit wait:

```java
new WebDriverWait(driver, Duration.ofSeconds(2))
        .until(currentDriver ->
                !currentDriver
                        .getCurrentUrl()
                        .contains("google_vignette")
        );
```

Nếu không có điều kiện đáng tin cậy để wait, nên xử lý trực tiếp URL hoặc overlay thay vì chờ cố định.

---

## 3.5 `type()` đang log dữ liệu nhạy cảm

Hiện tại:

```java
log.debug("Typed into {}: [{}]", locator, text);
```

Nếu nhập password, log có thể chứa:

```text
Test@123456
```

Điều này không an toàn.

### Đề xuất

```java
protected void type(By locator, String text) {
    Objects.requireNonNull(
            text,
            "Text to enter must not be null."
    );

    WebElement element = waitUntilVisible(locator);
    element.clear();
    element.sendKeys(text);

    log.debug(
            "Entered text into {}. Character count: {}",
            locator,
            text.length()
    );
}
```

Không log nội dung thật của input.

---

## 3.6 `isDisplayed()` không đúng với comment

Comment hiện mô tả rằng method dùng default explicit wait.

Nhưng implementation lại gọi:

```java
driver.findElement(locator).isDisplayed();
```

Đây là kiểm tra tức thời, không wait.

### Phương án 1: đổi tên thành kiểm tra tức thời

```java
protected boolean isDisplayedNow(By locator) {
    return driver.findElements(locator)
            .stream()
            .anyMatch(WebElement::isDisplayed);
}
```

### Phương án 2: dùng explicit wait

```java
protected boolean isDisplayed(By locator) {
    return isDisplayed(
            locator,
            Duration.ofSeconds(
                    FrameworkConstants.EXPLICIT_WAIT_SECONDS
            )
    );
}

protected boolean isDisplayed(
        By locator,
        Duration timeout
) {
    try {
        new WebDriverWait(driver, timeout)
                .until(
                        ExpectedConditions
                                .visibilityOfElementLocated(locator)
                );

        return true;
    } catch (TimeoutException exception) {
        return false;
    }
}
```

Nên dùng `Duration` thay vì `int timeoutSeconds`.

---

# 4. Các điểm cần cải thiện trong `BasePage`

## 4.1 `scrollIntoView()` nên wait element

Hiện tại dùng:

```java
driver.findElement(locator);
```

Nên dùng:

```java
protected void scrollIntoView(By locator) {
    WebElement element = waitUntilVisible(locator);

    javascriptExecutor().executeScript(
            "arguments[0].scrollIntoView({block: 'center'});",
            element
    );

    log.debug("Scrolled element into view: {}", locator);
}
```

---

## 4.2 `jsClick()` nên wait element

Đề xuất:

```java
protected void jsClick(By locator) {
    WebElement element = waitUntilClickable(locator);

    javascriptExecutor().executeScript(
            "arguments[0].click();",
            element
    );

    log.debug(
            "Clicked element using JavaScript: {}",
            locator
    );
}
```

Thêm helper:

```java
private JavascriptExecutor javascriptExecutor() {
    return (JavascriptExecutor) driver;
}
```

---

## 4.3 Explicit wait nên lấy từ config

Hiện tại lấy từ:

```java
FrameworkConstants.EXPLICIT_WAIT_SECONDS
```

Nên dùng config linh hoạt:

```java
protected BasePage(WebDriver driver) {
    this.driver = Objects.requireNonNull(
            driver,
            "WebDriver must not be null."
    );

    long explicitWaitSeconds =
            ConfigManager.getLong(
                    "explicitWait",
                    15L
            );

    this.wait = new WebDriverWait(
            driver,
            Duration.ofSeconds(explicitWaitSeconds)
    );
}
```

Config:

```properties
explicitWait=15
```

Chạy override:

```bash
mvn clean test -DexplicitWait=20
```

---

## 4.4 Constructor nên validate driver

Nên fail sớm:

```java
protected BasePage(WebDriver driver) {
    this.driver = Objects.requireNonNull(
            driver,
            "WebDriver must not be null."
    );

    this.wait = new WebDriverWait(
            this.driver,
            Duration.ofSeconds(
                    FrameworkConstants.EXPLICIT_WAIT_SECONDS
            )
    );
}
```

---

## 4.5 `BasePage` đang có dấu hiệu trở thành God Class

Hiện class chứa:

- Element actions
- Wait actions
- Ad handling
- Scroll
- Hover
- Dropdown
- JavaScript
- Navigation
- Alert

Nếu tiếp tục thêm:

- Window switching
- File upload
- Download
- Cookie
- Frame
- Screenshot
- Keyboard

class sẽ khó bảo trì.

### Cấu trúc đề xuất

```text
BasePage
├── action cơ bản
├── wait cơ bản
├── scroll cơ bản
└── page information cơ bản

AdHandler
└── xử lý quảng cáo riêng website

WindowUtils
└── tab và window khi thực sự cần

DownloadUtils
└── xử lý download khi thực sự cần

ScreenshotUtils
└── screenshot khi fail
```

Không cần tách mọi method ngay.

Nên tách những phần:

1. Phụ thuộc riêng Automation Exercise
2. Có trách nhiệm khác rõ ràng
3. Làm `BasePage` phình quá lớn

---

# 5. Các điểm cần cải thiện trong `BaseTest`

## 5.1 Biến `protected WebDriver driver` không hoàn toàn parallel-safe

`DriverFactory` dùng `ThreadLocal`, nhưng `BaseTest` lại lưu driver vào biến instance:

```java
protected WebDriver driver;
```

Khi chạy parallel methods trong cùng một class:

```text
Thread 1: driver = ChromeDriver A
Thread 2: driver = ChromeDriver B
Thread 1: có thể đọc nhầm B
```

### Đề xuất

Loại field:

```java
protected WebDriver driver;
```

Thay bằng:

```java
protected WebDriver driver() {
    return DriverFactory.getDriver();
}
```

Test sử dụng:

```java
HomePage homePage = new HomePage(driver());
```

---

## 5.2 `BaseTest` nên là abstract class

Hiện tại:

```java
public class BaseTest
```

Nên đổi thành:

```java
public abstract class BaseTest
```

Vì lớp này không đại diện cho test cụ thể và không nên được khởi tạo trực tiếp.

---

## 5.3 Browser priority cần thống nhất

Nên dùng thứ tự:

```text
1. Command line: -Dbrowser
2. TestNG XML parameter
3. Properties hoặc environment
4. Default chrome
```

Ví dụ:

```java
private String resolveBrowser(String browserParam) {
    String systemBrowser =
            System.getProperty("browser");

    if (systemBrowser != null
            && !systemBrowser.isBlank()) {
        return systemBrowser;
    }

    if (browserParam != null
            && !browserParam.isBlank()) {
        return browserParam;
    }

    return ConfigManager.get("browser", "chrome");
}
```

Cách này thuận tiện hơn khi chạy CI và local.

---

## 5.4 Không nên hardcode `baseUrl` trong Java

Hiện tại có fallback:

```java
ConfigManager.get(
    "baseUrl",
    "https://automationexercise.com"
);
```

Điều này vẫn là hardcode URL.

Nên yêu cầu config bắt buộc:

```java
String baseUrl =
        ConfigManager.getRequired("baseUrl");
```

Nếu thiếu:

```text
Missing required configuration: baseUrl
```

Config:

```properties
baseUrl=https://automationexercise.com
```

Browser có thể có default, nhưng URL ứng dụng nên là required config.

---

## 5.5 Teardown nên được bảo vệ bằng `finally`

Đề xuất:

```java
@AfterMethod(alwaysRun = true)
public void tearDown() {
    try {
        log.info("Closing browser");
    } finally {
        DriverFactory.quitDriver();
    }
}
```

Mục tiêu là đảm bảo driver luôn được đóng.

---

## 5.6 Có thể log tên test method

Có thể dùng:

```java
@BeforeMethod(alwaysRun = true)
@Parameters("browser")
public void setUp(
        @Optional("") String browserParam,
        Method testMethod
) {
    log.info(
            "Starting test: {}",
            testMethod.getName()
    );
}
```

Hoặc dùng `ITestListener` để log:

- Start
- Pass
- Fail
- Skip

Nếu listener đã làm việc này thì không cần lặp lại trong `BaseTest`.

---

# 6. Phiên bản `BaseTest` đề xuất

```java
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

public abstract class BaseTest {

    private static final Logger log =
            LoggerFactory.getLogger(BaseTest.class);

    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(
            @Optional("") String browserParam
    ) {
        String browserName =
                resolveBrowser(browserParam);

        BrowserType browserType =
                BrowserType.fromString(browserName);

        log.info(
                "Initializing browser: {}",
                browserType
        );

        DriverFactory.initDriver(browserType);

        String baseUrl =
                ConfigManager.getRequired("baseUrl");

        driver().get(baseUrl);

        log.info(
                "Navigated to: {}",
                baseUrl
        );
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            log.info("Closing browser");
        } finally {
            DriverFactory.quitDriver();
        }
    }

    private String resolveBrowser(
            String browserParam
    ) {
        String systemBrowser =
                System.getProperty("browser");

        if (systemBrowser != null
                && !systemBrowser.isBlank()) {
            return systemBrowser;
        }

        if (browserParam != null
                && !browserParam.isBlank()) {
            return browserParam;
        }

        return ConfigManager.get(
                "browser",
                "chrome"
        );
    }
}
```

---

# 7. Cấu trúc đích được đề xuất

```text
src/main/java/com/automationexercise/
├── config/
│   └── ConfigManager.java
│
├── driver/
│   ├── BrowserType.java
│   └── DriverFactory.java
│
├── pages/
│   ├── BasePage.java
│   ├── HomePage.java
│   └── LoginPage.java
│
├── components/
│   └── AdHandler.java
│
├── exceptions/
│   └── ElementActionException.java
│
└── utils/
    ├── JavaScriptUtils.java
    └── ScreenshotUtils.java

src/test/java/com/automationexercise/
├── base/
│   └── BaseTest.java
│
├── listeners/
│   └── TestListener.java
│
└── tests/
```

---

# 8. Thứ tự refactor đề xuất

## Ưu tiên 1: sửa ngay

1. Không log dữ liệu nhập trong `type()`
2. Loại `protected WebDriver driver` khỏi `BaseTest`
3. Tách advertisement handling khỏi `BasePage`
4. Loại `catch (Exception)` trong `click()`
5. Không tự động JavaScript click mọi lỗi
6. Sửa `isDisplayed()`
7. Loại `Thread.sleep()`

---

## Ưu tiên 2: sửa trước khi đạt 10 test cases

1. Chuyển explicit wait sang config
2. Bỏ hardcode base URL
3. Sửa browser priority
4. Dùng wait trong `scrollIntoView()`
5. Dùng wait trong `jsClick()`
6. Chuyển `BaseTest` thành abstract
7. Validate driver không null

---

## Ưu tiên 3: thực hiện sau

1. Custom exception cho element action
2. Log test method rõ ràng
3. Window và tab helper
4. Download helper
5. Parallel execution
6. FluentWait nếu có nhiều stale element
7. Mở rộng browser support

---

# 9. Các điểm đang làm tốt và nên giữ

## `BasePage`

- Là abstract class
- Không chứa assertion
- Không chứa test data
- Dùng protected method
- Có explicit wait
- Có reusable action
- Có alert wait
- Có Select encapsulation
- Naming tương đối rõ

## `BaseTest`

- Dùng `DriverFactory`
- Có `@BeforeMethod`
- Có `@AfterMethod(alwaysRun = true)`
- Setup và teardown tách biệt
- Có browser enum
- Có logging
- Không chứa locator
- Không chứa business logic
- Không chứa test data

---

# 10. Kết luận

Hai file hiện tại đủ tốt để tiếp tục xây Page Object và một số test đầu tiên.

Tuy nhiên:

- `BaseTest` cần bỏ biến WebDriver instance để thực sự an toàn với parallel execution.
- `BasePage` cần tách logic quảng cáo, thu gọn trách nhiệm và tránh JavaScript fallback quá rộng.
- Logging phải tránh lộ dữ liệu nhập.
- Wait strategy phải nhất quán.
- Configuration phải thật sự nằm ngoài Java code.

Sau khi xử lý các vấn đề ưu tiên cao, hai class này sẽ đủ tốt để làm nền tảng cho toàn bộ bộ test Automation Exercise và phù hợp hơn với mục tiêu Portfolio QA Automation.
