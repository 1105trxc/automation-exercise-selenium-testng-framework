# Automation Exercise QA Automation Framework Blueprint

## 1. Document Purpose

Tài liệu này là Blueprint tổng thể để xây dựng một dự án Automation Testing hoàn chỉnh cho website:

- Website under test: `https://automationexercise.com/`
- Test scenarios tham chiếu: 26 UI test cases công khai của Automation Exercise
- Mục tiêu chính: xây dựng một Automation Framework có cấu trúc tốt, dễ mở rộng, dễ tái sử dụng, hạn chế hardcode và đủ chất lượng để đưa vào Portfolio/CV QA Engineer.

Tài liệu này được thiết kế để:

1. Giúp người học hiểu toàn bộ context, mục tiêu và kiến trúc trước khi bắt đầu code.
2. Là nguồn đầu vào cho AI Agent phân tích, thiết kế, xây dựng và hướng dẫn từng giai đoạn.
3. Tránh tình trạng AI tự thêm kiến trúc không cần thiết hoặc tạo framework quá phức tạp.
4. Chuẩn hóa cách tổ chức test case, test data, config, report, bug report và tài liệu portfolio.
5. Giúp project có thể được clone, cấu hình và chạy lại bởi người khác.

---

# 2. Project Context

Người thực hiện hiện đang định hướng trở thành QA Engineer có năng lực:

- Manual Testing
- API Testing
- SQL
- Automation Testing

Người thực hiện đang có kinh nghiệm thực tế:

- Manual Tester Intern tại FPT Software
- Tham gia dự án Odoo ERP
- Đọc Blueprint và requirement
- Viết short-term test plan
- Viết happy path test case
- Functional testing
- Regression testing
- Bug retesting
- Business flow testing
- UAT support

Dự án này được xây dựng để bổ sung phần kỹ thuật Automation Testing cho Portfolio cá nhân.

Đây không phải là project automation để phục vụ production thực tế. Đây là project học tập và portfolio, nhưng phải được xây dựng theo tư duy gần với framework thực tế trong doanh nghiệp.

---

# 3. Project Goals

## 3.1 Functional Goals

Framework phải hỗ trợ:

- Tự động hóa các test case UI của Automation Exercise
- Chạy smoke test
- Chạy regression test
- Chạy theo module
- Chạy theo browser
- Chạy headless
- Chạy bằng TestNG suite XML
- Chạy từ command line
- Chạy trên GitHub Actions
- Sinh report
- Chụp screenshot khi fail
- Quản lý test data ngoài code
- Quản lý environment configuration ngoài code

## 3.2 Technical Goals

Framework phải:

- Modular
- Maintainable
- Readable
- Reusable
- Configurable
- Scalable ở mức hợp lý
- Không hardcode dữ liệu quan trọng
- Không hardcode URL
- Không hardcode browser
- Không hardcode timeout
- Không dùng Thread.sleep tùy tiện
- Không phụ thuộc thứ tự chạy test
- Không để test case phụ thuộc lẫn nhau
- Có thể chuẩn bị cho parallel execution
- Có thể mở rộng sang project web khác cùng stack

## 3.3 Portfolio Goals

Repository phải giúp nhà tuyển dụng hiểu được:

- Người thực hiện hiểu test automation framework là gì
- Biết tổ chức project
- Biết sử dụng Selenium, Java, TestNG và Maven
- Biết Page Object Model
- Biết quản lý test data
- Biết quản lý configuration
- Biết logging, screenshot, listener, report
- Biết smoke/regression suite
- Biết CI/CD cơ bản
- Biết test case management
- Biết bug management
- Biết requirement traceability
- Biết ghi tài liệu kỹ thuật và README

---

# 4. Out of Scope

Phiên bản đầu không bắt buộc:

- Mobile automation
- API automation
- Performance testing
- Security testing
- Docker Grid
- Selenium Grid thực tế
- Cloud testing platform
- Database testing
- Jira API integration
- TestRail API integration
- Video recording
- Custom dashboard
- Custom bug tracking system
- Tự xây thư viện assertion
- Tự xây test runner
- Tự xây dependency injection framework
- Reflection framework phức tạp

Các mục trên có thể được thêm ở giai đoạn nâng cao nếu framework nền tảng đã ổn định.

---

# 5. Technology Stack

## 5.1 Core Technologies

- Java 21
- Selenium WebDriver
- TestNG
- Maven
- Git
- GitHub

## 5.2 Supporting Libraries

- Jackson hoặc Gson: đọc JSON test data
- Allure TestNG: reporting
- SLF4J
- Log4j2 hoặc Logback
- Apache Commons IO nếu cần thao tác file
- WebDriverManager là optional vì Selenium Manager đã hỗ trợ driver management

## 5.3 CI/CD

- GitHub Actions

## 5.4 Documentation

- Markdown
- Excel hoặc Google Sheets cho manual test cases
- Mermaid hoặc PlantUML cho architecture diagram
- GitHub Issues hoặc Jira sample project cho bug reports

## 5.5 Recommended Versions

AI Agent phải kiểm tra version mới và ổn định tại thời điểm triển khai.

Không được tự chọn dependency version quá cũ.

Phải ưu tiên version tương thích giữa:

- Java
- Selenium
- TestNG
- Maven Surefire Plugin
- Allure
- Jackson/Gson
- Logging library

---

# 6. Project Architecture

## 6.1 Target Folder Structure

```text
automation-exercise-framework/
│
├── .github/
│   └── workflows/
│       └── automation-test.yml
│
├── docs/
│   ├── test-plan.md
│   ├── framework-architecture.md
│   ├── test-cases.xlsx
│   ├── requirement-traceability-matrix.xlsx
│   ├── execution-report.md
│   └── bug-reports/
│       ├── BUG-AE-001.md
│       ├── BUG-AE-002.md
│       └── BUG-AE-003.md
│
├── reports/
│   ├── screenshots/
│   ├── logs/
│   └── allure-results/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.automationexercise/
│   │   │       ├── config/
│   │   │       │   └── ConfigManager.java
│   │   │       │
│   │   │       ├── constants/
│   │   │       │   ├── FrameworkConstants.java
│   │   │       │   └── RouteConstants.java
│   │   │       │
│   │   │       ├── driver/
│   │   │       │   ├── DriverFactory.java
│   │   │       │   └── BrowserType.java
│   │   │       │
│   │   │       ├── models/
│   │   │       │   ├── UserData.java
│   │   │       │   ├── LoginData.java
│   │   │       │   ├── ProductData.java
│   │   │       │   ├── CheckoutData.java
│   │   │       │   └── ContactData.java
│   │   │       │
│   │   │       ├── pages/
│   │   │       │   ├── BasePage.java
│   │   │       │   ├── HomePage.java
│   │   │       │   ├── LoginPage.java
│   │   │       │   ├── SignupPage.java
│   │   │       │   ├── ProductsPage.java
│   │   │       │   ├── ProductDetailsPage.java
│   │   │       │   ├── CartPage.java
│   │   │       │   ├── CheckoutPage.java
│   │   │       │   ├── PaymentPage.java
│   │   │       │   ├── ContactUsPage.java
│   │   │       │   └── TestCasesPage.java
│   │   │       │
│   │   │       ├── components/
│   │   │       │   ├── HeaderComponent.java
│   │   │       │   ├── FooterComponent.java
│   │   │       │   ├── ProductCardComponent.java
│   │   │       │   └── CartItemComponent.java
│   │   │       │
│   │   │       ├── utils/
│   │   │       │   ├── JsonDataReader.java
│   │   │       │   ├── ScreenshotUtils.java
│   │   │       │   ├── DateTimeUtils.java
│   │   │       │   ├── RandomDataUtils.java
│   │   │       │   ├── JavaScriptUtils.java
│   │   │       │   └── FileUtils.java
│   │   │       │
│   │   │       └── exceptions/
│   │   │           ├── FrameworkException.java
│   │   │           ├── ConfigurationException.java
│   │   │           └── TestDataException.java
│   │   │
│   │   └── resources/
│   │       ├── config/
│   │       │   ├── local.properties
│   │       │   ├── dev.properties
│   │       │   └── staging.properties
│   │       │
│   │       └── log4j2.xml
│   │
│   └── test/
│       ├── java/
│       │   └── com.automationexercise/
│       │       ├── base/
│       │       │   └── BaseTest.java
│       │       │
│       │       ├── tests/
│       │       │   ├── authentication/
│       │       │   ├── registration/
│       │       │   ├── products/
│       │       │   ├── cart/
│       │       │   ├── checkout/
│       │       │   ├── contact/
│       │       │   └── subscription/
│       │       │
│       │       ├── dataproviders/
│       │       │   ├── AuthenticationDataProvider.java
│       │       │   ├── ProductDataProvider.java
│       │       │   └── CheckoutDataProvider.java
│       │       │
│       │       └── listeners/
│       │           ├── TestListener.java
│       │           ├── SuiteListener.java
│       │           └── RetryAnalyzer.java
│       │
│       └── resources/
│           ├── testdata/
│           │   ├── users.json
│           │   ├── products.json
│           │   ├── checkout.json
│           │   └── contact-messages.json
│           │
│           └── suites/
│               ├── smoke.xml
│               ├── regression.xml
│               ├── authentication.xml
│               └── full-suite.xml
│
├── .env.example
├── .gitignore
├── LICENSE
├── pom.xml
└── README.md
```

## 6.2 Architecture Principles

Framework phải tách rõ:

```text
Test Layer
    ↓
Page Object / Component Layer
    ↓
BasePage / Reusable Action Layer
    ↓
Driver Layer
    ↓
Browser
```

Configuration flow:

```text
Command Line
    ↓
Environment Variable
    ↓
Properties File
    ↓
Default Value
```

Test data flow:

```text
JSON File
    ↓
Model Class
    ↓
Data Provider
    ↓
Test Method
```

Failure handling flow:

```text
Test Failure
    ↓
TestNG Listener
    ↓
Screenshot + URL + Log
    ↓
Allure Attachment
```

---

# 7. Framework Design Requirements

## 7.1 Driver Management

Phải có `DriverFactory`.

Yêu cầu:

- Không tạo WebDriver trực tiếp trong test class
- Hỗ trợ Chrome
- Hỗ trợ Firefox
- Edge là optional
- Hỗ trợ headless
- Hỗ trợ local execution
- Dùng ThreadLocal để chuẩn bị cho parallel execution
- Có initialize driver
- Có get driver
- Có quit driver
- Không để driver bị null mà không báo lỗi rõ ràng
- Không giữ driver static dùng chung giữa nhiều test thread

## 7.2 BaseTest

Phải có:

- `@BeforeMethod`
- `@AfterMethod`
- Browser setup
- Navigate base URL
- Driver cleanup
- Listener integration
- Không chứa business logic
- Không chứa locator
- Không chứa test data

## 7.3 BasePage

Phải cung cấp reusable actions:

- click
- type
- clear
- getText
- isDisplayed
- waitUntilVisible
- waitUntilClickable
- waitUntilInvisible
- waitForUrl
- scrollIntoView
- select dropdown
- hover nếu cần
- JavaScript click chỉ dùng khi thực sự cần

Không được biến BasePage thành God Class.

Mỗi method phải có mục đích rõ ràng.

## 7.4 Page Object Model

Mỗi page class phải:

- Đại diện cho một page hoặc business screen
- Chứa locator của page đó
- Chứa action method
- Không chứa assertion của test
- Không chứa TestNG annotation
- Không chứa test data hardcode
- Không chứa WebDriver setup
- Có thể trả về page tiếp theo sau action điều hướng

Ví dụ:

```java
public HomePage clickLogin() {
    click(loginButton);
    return new HomePage(driver);
}
```

hoặc:

```java
public LoginPage openLoginPage() {
    click(loginLink);
    return new LoginPage(driver);
}
```

## 7.5 Component Object

Dùng cho các vùng UI lặp lại:

- Header
- Footer
- Product card
- Cart item
- Navigation menu

Không tạo component nếu chỉ dùng một lần.

## 7.6 Configuration Management

Configuration phải nằm ngoài test code.

Các config tối thiểu:

```properties
baseUrl=https://automationexercise.com
browser=chrome
headless=false
explicitWait=15
pageLoadTimeout=30
retryCount=0
executionMode=local
```

Phải hỗ trợ command line override:

```bash
mvn clean test -Dbrowser=firefox -Dheadless=true -Denv=staging
```

Không commit:

- Password thật
- Token
- Secret
- Private key
- Email credentials

Phải có `.env.example` hoặc `config.example.properties`.

## 7.7 Test Data Management

Test data phải được lưu ngoài test code.

Định dạng chính:

- JSON

Test data nên gồm:

- Valid users
- Invalid login users
- Registration data
- Search keyword
- Product information
- Checkout address
- Contact form message
- Expected validation message nếu hợp lý

Không đưa locator vào JSON.

Không đưa test flow vào JSON.

Không dùng `Map<String, Object>` ở khắp nơi.

Phải map JSON thành Java model.

## 7.8 Random Test Data

Các case registration phải tránh trùng email.

Phải có utility sinh dữ liệu:

- Unique email
- Random name
- Timestamp suffix

Ví dụ:

```text
portfolio.user.20260705143020@example.com
```

Random data phải:

- Có format dễ đọc
- Có thể trace trong log
- Không phụ thuộc thư viện fake data phức tạp nếu chưa cần

## 7.9 Wait Strategy

Ưu tiên Explicit Wait.

Không dùng implicit wait và explicit wait trộn lẫn tùy tiện.

Không dùng:

```java
Thread.sleep(5000);
```

trừ khi có lý do đặc biệt và được ghi chú.

Phải có strategy cho:

- Element visible
- Element clickable
- Text present
- URL change
- Loading spinner disappear
- Page navigation

## 7.10 Assertions

Assertion phải nằm trong test class.

Assertion phải rõ nghĩa.

Không chỉ verify URL nếu business result cần kiểm tra UI state.

Ví dụ login thành công có thể kiểm tra:

- Username hiển thị
- Logout link hiển thị
- Login link không còn
- Trang đúng trạng thái

Không viết assertion mơ hồ.

Thông báo assertion phải giúp debug.

---

# 8. TestNG Requirements

## 8.1 Required TestNG Features

Framework phải sử dụng:

- `@Test`
- `@BeforeMethod`
- `@AfterMethod`
- `@DataProvider`
- `groups`
- `description`
- `testng.xml`
- `ITestListener`

Optional:

- `@BeforeSuite`
- `@AfterSuite`
- RetryAnalyzer
- Parallel execution

## 8.2 Test Groups

Các group tối thiểu:

```text
smoke
regression
authentication
registration
products
cart
checkout
contact
subscription
positive
negative
```

## 8.3 Suite Files

Phải có:

- smoke.xml
- regression.xml
- full-suite.xml

Optional:

- authentication.xml
- cart.xml
- checkout.xml

## 8.4 Test Method Naming

Tên method phải mô tả hành vi.

Ví dụ tốt:

```java
loginWithValidCredentials()
loginShouldFailWithInvalidCredentials()
registeredUserCanPlaceOrder()
productCanBeRemovedFromCart()
```

Không đặt tên:

```java
test1()
test2()
runLogin()
```

## 8.5 Test Case ID Mapping

Mỗi automated test phải map với manual test case ID.

Ví dụ:

```java
@Test(description = "TC-AE-002 - Login with valid credentials")
```

Có thể sử dụng custom annotation ở giai đoạn nâng cao.

---

# 9. Test Case Scope

Bộ 26 test cases tham chiếu từ Automation Exercise phải được phân nhóm.

## 9.1 Authentication and Registration

- Register User
- Login User with correct email and password
- Login User with incorrect email and password
- Logout User
- Register User with existing email

## 9.2 Contact and Navigation

- Contact Us Form
- Verify Test Cases Page

## 9.3 Products

- Verify All Products and Product Detail Page
- Search Product
- Verify All Products and product detail after login nếu có
- View Category Products
- View and Cart Brand Products

## 9.4 Subscription

- Verify Subscription in home page
- Verify Subscription in Cart page

## 9.5 Cart

- Add Products in Cart
- Verify Product quantity in Cart
- Remove Products From Cart
- Add review on product nếu thuộc module products

## 9.6 Checkout and Order

- Place Order: Register while Checkout
- Place Order: Register before Checkout
- Place Order: Login before Checkout
- Download Invoice after purchase order
- Verify address details in checkout page

## 9.7 Additional UI Scenarios

- Add to cart from Recommended items
- Verify Scroll Up using Arrow button
- Verify Scroll Up without Arrow button

AI Agent phải đối chiếu lại danh sách 26 test cases chính thức của Automation Exercise trước khi triển khai để đảm bảo không thiếu hoặc sai thứ tự.

---

# 10. Test Independence Requirements

Mỗi test phải độc lập.

Không được thiết kế:

```text
TC01 tạo user
TC02 dùng user từ TC01
TC03 xóa user từ TC02
```

Mỗi test phải tự:

- Chuẩn bị dữ liệu cần thiết
- Tạo user nếu cần
- Login nếu cần
- Cleanup nếu phù hợp
- Không phụ thuộc test khác chạy trước

Nếu test tạo user:

- Dùng email unique
- Có thể xóa account cuối test nếu website hỗ trợ
- Nếu cleanup fail, phải log rõ

Không dùng `dependsOnMethods` cho business test thông thường.

---

# 11. Logging Requirements

Phải dùng logging framework.

Không sử dụng `System.out.println()` làm logging chính.

Log tối thiểu:

- Test start
- Browser initialized
- URL accessed
- Major action
- Important test data identifier
- Assertion milestone
- Test pass
- Test fail
- Screenshot path
- Driver closed

Không log:

- Password đầy đủ
- Token
- Secret
- Thông tin nhạy cảm

Ví dụ:

```text
INFO Starting TC-AE-002
INFO Browser initialized: chrome
INFO Navigating to login page
INFO Entering email: p***@example.com
INFO Clicking login button
INFO Verifying logged-in user
INFO Test passed: TC-AE-002
```

---

# 12. Screenshot and Failure Evidence

Khi test fail, framework phải tự động lưu:

- Screenshot
- Current URL
- Method name
- Test case ID
- Timestamp
- Browser
- Environment

Tên file gợi ý:

```text
TC-AE-002_loginWithValidCredentials_20260705_143020.png
```

Screenshot phải được attach vào Allure report.

Optional:

- Page source
- Browser console log

---

# 13. Reporting Requirements

Sử dụng Allure Report.

Report nên hiển thị:

- Test case name
- Test case ID
- Description
- Module
- Severity
- Steps
- Browser
- Environment
- Duration
- Screenshot
- Failure message
- Stack trace

Nên sử dụng:

- `@Epic`
- `@Feature`
- `@Story`
- `@Severity`
- `@Description`

Không lạm dụng annotation.

---

# 14. Retry Policy

Retry mặc định:

```properties
retryCount=0
```

Chỉ bật retry để phân tích flaky test.

Không được dùng retry để che lỗi framework.

Nếu test pass sau retry:

- Report phải thể hiện đã retry
- Không được coi đó là hoàn toàn ổn định
- Cần điều tra root cause

---

# 15. Parallel Execution

Parallel execution chỉ triển khai khi:

- Test chạy tuần tự ổn định
- DriverFactory dùng ThreadLocal
- Test data không xung đột
- Mỗi test độc lập
- Không dùng static mutable state
- Không dùng chung account có thay đổi dữ liệu

Giai đoạn đầu:

```text
parallel=false
```

Giai đoạn nâng cao:

```xml
<suite name="Regression Suite" parallel="methods" thread-count="3">
```

---

# 16. Bug Management

Framework không tự xây hệ thống quản lý bug.

Bug được quản lý bằng:

- GitHub Issues
- Hoặc Jira sample project
- Hoặc file Markdown trong `docs/bug-reports`

Bug report phải có:

- Bug ID
- Title
- Environment
- Preconditions
- Test data
- Steps to reproduce
- Actual result
- Expected result
- Severity
- Priority
- Evidence
- Related test case
- Status

Ví dụ mapping:

```text
Bug ID: BUG-AE-001
Related Test Case: TC-AE-014
Automation Status: Automated Regression
```

Portfolio nên có ít nhất 3 đến 5 bug reports mẫu.

---

# 17. Test Case Management

Manual test case không bị thay thế bởi automation code.

Phải có file manual test case riêng.

Các cột gợi ý:

- Test Case ID
- Module
- Test Scenario
- Preconditions
- Test Data
- Steps
- Expected Result
- Actual Result
- Status
- Priority
- Automation Status
- Automated Method
- Notes

Automation Status:

```text
Not Automated
Planned
In Progress
Automated
Not Suitable for Automation
```

---

# 18. Requirement Traceability Matrix

Phải có RTM.

Cột gợi ý:

- Requirement ID
- Requirement Description
- Test Case ID
- Test Type
- Priority
- Automation Status
- Automated Method
- Execution Status
- Bug ID

Ví dụ:

| Requirement ID | Test Case ID | Test Type | Automation Method | Status |
|---|---|---|---|---|
| REQ-LOGIN-01 | TC-AE-002 | Positive | loginWithValidCredentials | Automated |
| REQ-LOGIN-02 | TC-AE-003 | Negative | loginShouldFailWithInvalidCredentials | Automated |

---

# 19. Test Plan Requirements

File `docs/test-plan.md` phải có:

## 19.1 Overview

- Project name
- System under test
- Test objective
- Stakeholders giả định

## 19.2 Scope

- In scope
- Out of scope

## 19.3 Test Types

- Functional testing
- UI testing
- Smoke testing
- Regression testing
- Negative testing
- Cross-browser testing

## 19.4 Test Environment

- OS
- Browser
- Java version
- Selenium version
- TestNG version

## 19.5 Entry Criteria

Ví dụ:

- Website available
- Test environment accessible
- Framework build succeeds
- Required test data available

## 19.6 Exit Criteria

Ví dụ:

- All smoke cases passed
- No unresolved blocker
- Regression pass rate đạt tiêu chí
- Report generated successfully

## 19.7 Risks

- Public demo site instability
- Ads or dynamic UI
- Network dependency
- Shared public data
- Website data reset
- CAPTCHA nếu xuất hiện
- UI thay đổi không báo trước

## 19.8 Deliverables

- Source code
- Test cases
- RTM
- Bug reports
- Allure report
- Execution report
- README
- CI workflow

---

# 20. GitHub Actions CI Requirements

CI phải chạy khi:

- Push vào main
- Push vào develop
- Pull request vào main
- Manual trigger

Pipeline phải:

1. Checkout repository
2. Setup Java
3. Cache Maven
4. Install dependencies
5. Run smoke suite
6. Upload reports
7. Upload screenshots
8. Preserve artifacts dù test fail

Command gợi ý:

```bash
mvn clean test -Dheadless=true -Dbrowser=chrome -DsuiteXmlFile=src/test/resources/suites/smoke.xml
```

CI đầu tiên chỉ chạy smoke.

Regression CI có thể chạy:

- Manual trigger
- Scheduled nightly
- Khi merge main

---

# 21. README Requirements

README phải là tài liệu chính của Portfolio.

## 21.1 Required Sections

- Project title
- Project overview
- Objectives
- Website under test
- Test scope
- Tech stack
- Framework architecture
- Folder structure
- Key features
- Prerequisites
- Installation
- Configuration
- How to run
- How to run suites
- How to run browser options
- How to generate Allure report
- CI/CD overview
- Test coverage
- Bug management
- Known limitations
- Future improvements
- Author

## 21.2 Required Commands

```bash
mvn clean test
```

```bash
mvn clean test -Dbrowser=firefox
```

```bash
mvn clean test -Dheadless=true
```

```bash
mvn clean test -Denv=staging
```

```bash
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
```

## 21.3 Portfolio Evidence

README nên có:

- Architecture diagram
- Allure screenshot
- Test execution screenshot
- CI screenshot
- Coverage table
- Bug report link
- Manual test case link

---

# 22. Coding Standards

## 22.1 Java Standards

- Class name: PascalCase
- Method name: camelCase
- Constant: UPPER_SNAKE_CASE
- Package name: lowercase
- Không viết method quá dài
- Không duplicate code
- Không dùng magic number
- Không dùng wildcard import
- Không để empty catch block
- Không nuốt exception
- Exception message phải rõ

## 22.2 Test Standards

Mỗi test phải theo flow:

```text
Arrange
Act
Assert
```

Có thể dùng comment nếu cần, nhưng không lạm dụng.

Ví dụ:

```java
@Test(description = "TC-AE-002 - Login with valid credentials")
public void loginWithValidCredentials() {
    // Arrange
    UserData user = TestData.getValidUser();

    // Act
    HomePage homePage = new HomePage(driver)
        .openLoginPage()
        .login(user);

    // Assert
    Assert.assertTrue(
        homePage.isLoggedInUserDisplayed(),
        "Logged-in user should be displayed."
    );
}
```

---

# 23. Security and Repository Hygiene

`.gitignore` phải loại trừ:

```text
target/
.idea/
.vscode/
*.iml
.env
local.properties
secrets.properties
allure-results/
allure-report/
reports/screenshots/
reports/logs/
```

Không commit:

- Credentials thật
- Token thật
- Password thật
- Generated report nặng
- IDE metadata không cần thiết

Phải có:

- `.env.example`
- Sample config
- Sample test data an toàn

---

# 24. Development Roadmap

## Phase 0: Analysis

Mục tiêu:

- Đọc 26 test cases
- Nhóm test cases
- Xác định page
- Xác định component
- Xác định test data
- Xác định test nào phù hợp automation
- Tạo test plan draft
- Tạo RTM draft

Deliverables:

- Test case list
- Module mapping
- Architecture draft

## Phase 1: Foundation

Xây dựng:

- Maven project
- Dependencies
- DriverFactory
- BrowserType
- ConfigManager
- BaseTest
- BasePage
- Logging cơ bản
- LoginPage
- HomePage

Automate:

- Login valid
- Login invalid
- Logout

Deliverables:

- Framework chạy được
- 3 automated tests

## Phase 2: Page Object and Test Data

Xây dựng:

- JSON reader
- Model class
- DataProvider
- SignupPage
- ProductsPage
- Components cơ bản

Automate thêm:

- Registration
- Existing email registration
- Product search
- Product detail

Deliverables:

- Khoảng 8 đến 10 automated tests

## Phase 3: Cart and Checkout

Xây dựng:

- CartPage
- CheckoutPage
- PaymentPage
- ProductCardComponent
- CartItemComponent
- RandomDataUtils

Automate:

- Add product
- Quantity
- Remove product
- Checkout flows
- Invoice download

Deliverables:

- Khoảng 18 automated tests

## Phase 4: Observability

Xây dựng:

- TestListener
- ScreenshotUtils
- Allure integration
- Log improvement
- Failure evidence
- Environment metadata

Deliverables:

- Allure report đầy đủ
- Screenshot khi fail

## Phase 5: Test Execution Management

Xây dựng:

- Smoke suite
- Regression suite
- Full suite
- Groups
- Cross-browser
- Headless
- Optional retry
- Parallel-ready architecture

Deliverables:

- Đủ 26 test cases
- Suite execution

## Phase 6: CI and Portfolio

Xây dựng:

- GitHub Actions
- README
- Test plan
- RTM
- Bug reports
- Architecture document
- Execution report
- Demo screenshots
- Demo video optional

Deliverables:

- Portfolio hoàn chỉnh

---

# 25. Definition of Done

Project được xem là hoàn thành khi:

## Framework

- Maven build thành công
- Test chạy bằng TestNG
- DriverFactory hoạt động
- Config linh hoạt
- JSON test data hoạt động
- Page Object rõ ràng
- Không hardcode URL/browser/timeout
- Không dùng Thread.sleep tùy tiện
- Screenshot khi fail
- Allure report hoạt động
- Logging hoạt động
- Suite chạy được
- Headless chạy được
- GitHub Actions chạy được

## Test Coverage

- 26 test cases được phân tích
- Các case phù hợp được automation
- Case không phù hợp phải có lý do
- Smoke suite rõ ràng
- Regression suite rõ ràng
- Test độc lập

## Documentation

- README đầy đủ
- Test plan
- Test case file
- RTM
- Bug reports
- Architecture document
- Execution report

## Portfolio

- Repository public
- Không có secret
- Commit history rõ
- Code dễ đọc
- Có report screenshot
- Có CI evidence
- Người thực hiện giải thích được mọi thành phần

---

# 26. Expected Commit Strategy

Không push toàn bộ project trong một commit.

Commit gợi ý:

```text
chore: initialize Maven Selenium TestNG project
feat: add WebDriver factory and browser configuration
feat: add base test and base page
feat: implement login page object
test: automate valid and invalid login scenarios
feat: add JSON test data reader
feat: add registration page object
test: automate registration scenarios
feat: add cart and product page objects
test: automate cart scenarios
feat: integrate Allure reporting
feat: capture screenshots on test failure
ci: add GitHub Actions smoke workflow
docs: add test plan and framework architecture
docs: add README and execution guide
```

---

# 27. AI Agent Instructions

AI Agent sử dụng Blueprint này phải tuân thủ:

## 27.1 General Rules

- Không build toàn bộ framework trong một lần
- Chia nhỏ theo phase
- Giải thích trước khi code
- Mỗi lần chỉ triển khai một nhóm chức năng hợp lý
- Không tự thêm công nghệ ngoài Blueprint nếu chưa giải thích lý do
- Không over-engineering
- Không tạo class không cần thiết
- Không tạo utility trước khi có nhu cầu thực tế
- Không dùng code giả
- Code phải compile được
- Dependency phải tương thích
- Mỗi bước phải có cách chạy và kiểm tra

## 27.2 Teaching Rules

AI Agent phải:

1. Giải thích mục tiêu của bước hiện tại.
2. Giải thích file nào sẽ tạo.
3. Giải thích trách nhiệm của từng file.
4. Cung cấp code đầy đủ.
5. Chỉ rõ đường dẫn file.
6. Cung cấp command chạy.
7. Nêu expected result.
8. Nêu lỗi thường gặp.
9. Đưa checklist xác nhận.
10. Không chuyển sang phase tiếp theo nếu nền tảng hiện tại chưa ổn định.

## 27.3 Code Review Rules

AI Agent phải review:

- Duplicate code
- Hardcode
- Bad naming
- God class
- Page Object chứa assertion
- Test phụ thuộc nhau
- Driver leak
- Thread safety
- Flaky wait
- Secret leakage
- JSON misuse
- Overuse static
- Retry misuse

## 27.4 Decision Rules

Khi có nhiều lựa chọn, AI Agent phải:

- Nêu lựa chọn
- Nêu ưu nhược điểm
- Chọn phương án phù hợp Fresher Portfolio
- Không chọn kiến trúc enterprise quá phức tạp

## 27.5 Output Format for Each Phase

Mỗi phase AI Agent phải trả lời theo format:

```text
1. Phase Goal
2. Architecture Decision
3. Files to Create
4. Dependencies
5. Implementation
6. How to Run
7. Expected Result
8. Common Errors
9. Review Checklist
10. Next Phase
```

---

# 28. Suggested AI Agent Starting Prompt

```text
Bạn là Senior QA Automation Engineer và mentor.

Hãy sử dụng file Blueprint này làm nguồn yêu cầu chính để hướng dẫn tôi xây dựng Automation Exercise Framework từ đầu.

Yêu cầu:
- Không build toàn bộ project một lần.
- Bắt đầu từ Phase 0 và Phase 1.
- Giải thích rõ từng quyết định.
- Mỗi file phải có đường dẫn.
- Code phải compile được.
- Dùng Java, Selenium, TestNG, Maven.
- Không hardcode config và test data.
- Không over-engineering.
- Mỗi bước phải có command chạy và checklist kiểm tra.
- Sau mỗi phase, review code theo góc nhìn framework maintainability.
- Không tự thêm công nghệ ngoài Blueprint nếu chưa giải thích.
```

---

# 29. Final Portfolio Description

Có thể sử dụng trong CV:

```text
Built a modular Selenium Java automation framework using Maven, TestNG, Page Object Model, JSON-based test data, environment-based configuration, reusable UI components, explicit waits, logging, failure screenshots, Allure reporting, TestNG suites, and GitHub Actions CI. Automated e-commerce test scenarios and documented the test plan, requirement traceability matrix, execution results, and sample defect reports.
```

Tên project gợi ý:

```text
Selenium Java Test Automation Framework for Automation Exercise
```

GitHub repository gợi ý:

```text
automation-exercise-selenium-testng-framework
```

---

# 30. Final Principle

Framework tốt không phải framework có nhiều class nhất.

Framework tốt phải:

- Chạy được
- Dễ hiểu
- Dễ cấu hình
- Dễ bảo trì
- Dễ mở rộng
- Có report
- Có evidence
- Có documentation
- Không hardcode
- Không phụ thuộc test
- Không over-engineering
- Người xây dựng giải thích được toàn bộ quyết định
