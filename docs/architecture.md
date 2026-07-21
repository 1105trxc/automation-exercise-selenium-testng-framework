# Automation Exercise Framework Architecture

## Dependency direction

```text
Tests -> Flows -> Pages/Components -> Core infrastructure
```

Dependencies chỉ đi từ trái sang phải. Tests có thể dùng Page Object trực tiếp cho hành vi một trang và dùng Flow cho journey tái sử dụng qua nhiều trang.

## Test layer

Path: `src/test/java/com/automationexercise`

Trách nhiệm:

- Chuẩn bị test data.
- Gọi Page Object và Flow.
- Sở hữu TestNG assertions và business oracle.
- Đăng ký listener qua TestNG suite.

`JsonDataReader` và `TestDataProvider` ở test scope vì phụ thuộc vào test resources.

## Flow layer

Path: `src/main/java/com/automationexercise/flows`

- `UserFlow`: registration và positive login journeys.
- `CartFlow`: orchestration thêm nhiều sản phẩm.
- `CheckoutFlow`: comment, payment và order completion.

Flow không chứa TestNG assertion, locator hoặc generated test data. Flow fail-fast khi intermediate page không đạt postcondition cần thiết.

## Page and component layer

Path:

```text
src/main/java/com/automationexercise/pages
src/main/java/com/automationexercise/components
```

Page Objects hiện có:

```text
HomePage
LoginPage
SignupPage
AccountCreatedPage
AccountDeletedPage
ProductsPage
ProductDetailPage
CartPage
CheckoutPage
PaymentPage
PaymentSuccessPage
ContactUsPage
TestCasesPage
```

Reusable components:

```text
HeaderComponent
FooterSubscriptionComponent
AddToCartModal
AdHandler
```

Page actions trả destination thật. Ví dụ `clickCreateAccount()` trả `AccountCreatedPage`; `clickDeleteAccount()` trả `AccountDeletedPage`; `clickFirstProductViewProduct()` chỉ trả `ProductDetailPage` sau khi URL và identity element đạt postcondition.

Assertions không nằm trong Page Object. Public state methods chỉ quan sát UI và trả data/boolean cho test layer quyết định.

## Core infrastructure

### BasePage

Package: `com.automationexercise.pages`

Cung cấp native click/type, configured explicit waits, visibility checks, URL navigation checks, scroll, hover, select và alert handling. `BasePage` không biết Automation Exercise hoặc third-party ads.

### AEBasePage

Package: `com.automationexercise.pages`

Thêm site-specific click contract:

1. Native click một lần.
2. Chỉ khi Selenium ném `ElementClickInterceptedException` và interceptor là known Google ad, đóng ad rồi retry đúng một lần.
3. First-party blocker hoặc unknown interceptor được ném lại để test fail.

Không có post-click URL inspection hoặc automatic re-click cho generic actions.

### AdHandler

Package: `com.automationexercise.components`

Chỉ nhận diện selector Google đã biết. Resolution ưu tiên native close, sau đó Escape cho vignette, rồi optional targeted workaround trên exact third-party selectors. Không quét arbitrary fixed div và không sửa broad `html/body` layout.

`ProductsPage` có một continuation riêng cho `View Product`: chỉ áp dụng cho GET link không side effect sau khi vignette đã được quan sát, destination chưa đạt và browser vẫn ở source URL. Continuation tối đa một lần.

### Driver and configuration

- `DriverFactory` giữ WebDriver trong `ThreadLocal`.
- Chrome, Firefox và Edge có download directory nhất quán.
- `ConfigManager` áp dụng precedence: system property -> classpath properties -> code default.
- `BaseTest` lấy driver qua `DriverFactory` thay vì giữ shared instance field.

### Data safety and artifacts

- `AccountCleanupService` dùng per-thread registry và trả `CleanupResult`.
- `DownloadManager` xóa stale files, poll file completion và đọc UTF-8.
- `TestListener` ghi failure context và chụp screenshot khi driver còn khả dụng.

## Reliability rules

```text
No Thread.sleep
No generic JavaScript click
No retry-to-green
No broad DOM hiding
No assertions or test data in Page Objects
No shared global account cleanup
No Page Object return type that lies about current state
```

## Execution

- Smoke: `src/test/resources/suites/smoke.xml`
- Regression: `src/test/resources/suites/regression.xml`
- CI: `.github/workflows/ui-tests.yml`
- Local config template: `.env.example`
- Current implementation evidence: `docs/implementation_plan.md`
