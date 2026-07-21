# Automation Exercise Selenium TestNG Framework

Framework UI automation cho [Automation Exercise](https://automationexercise.com), dùng Java 21, Selenium, TestNG, Maven và Allure.

## Kiến trúc

```text
Tests -> Flows -> Pages/Components -> Core infrastructure
```

- `tests`: test data, business assertions và test oracle.
- `flows`: hành trình tái sử dụng qua nhiều trang.
- `pages`: hành vi trực tiếp của từng trang.
- `components`: header, footer, modal và ad handling theo phạm vi hẹp.
- `driver`, `config`, `utils`: WebDriver, cấu hình, download và cleanup.

Framework không dùng `Thread.sleep`, generic JavaScript click, retry-to-green hoặc broad DOM hiding để ép test PASS.

## Yêu cầu

- JDK 21
- Maven 3.9+
- Chrome, Firefox hoặc Edge

Selenium Manager tự quản lý browser driver. Test download được cấu hình cho cả ba browser trong `DriverFactory`.

## Cấu hình

Tạo file local từ mẫu:

```powershell
Copy-Item .env.example src/main/resources/config/local.properties
```

Trên bash:

```bash
cp .env.example src/main/resources/config/local.properties
```

`ConfigManager` đọc `classpath:config/<env>.properties`; mặc định là `config/local.properties`. System properties `-D...` luôn có ưu tiên cao nhất.

Các key chính:

```properties
baseUrl=https://automationexercise.com
browser=chrome
headless=false
explicitWait=15
pageLoadTimeout=30
downloadTimeout=15
downloadDir=target/downloads
thirdPartyAdWorkaround=true
thirdPartyAdWait=3
```

`local.properties` bị Git ignore để giữ cấu hình máy cá nhân ngoài repository.

## Chạy test

Biên dịch sạch:

```bash
mvn clean test-compile
```

Smoke suite:

```bash
mvn clean test "-Dheadless=true" "-DsuiteXmlFile=src/test/resources/suites/smoke.xml"
```

Regression suite:

```bash
mvn clean test "-Dheadless=true" "-DsuiteXmlFile=src/test/resources/suites/regression.xml"
```

Override browser:

```bash
mvn clean test "-Dbrowser=firefox" "-Dheadless=true" \
  "-DsuiteXmlFile=src/test/resources/suites/smoke.xml"
```

## Evidence

- Surefire: `target/surefire-reports/`
- Allure results: `target/allure-results/`
- Failure screenshots: `reports/screenshots/`
- Runtime logs: `reports/logs/`
- Downloads: cấu hình bởi `downloadDir`

Test failure log bao gồm test description, environment, browser, current URL, reason và screenshot path khi WebDriver còn khả dụng.

## CI

`.github/workflows/ui-tests.yml` chạy smoke suite trên push/PR. `workflow_dispatch` cho phép chọn `smoke` hoặc `regression`; evidence được upload kể cả khi job fail.

Chi tiết kiến trúc nằm trong [docs/architecture.md](docs/architecture.md). Trạng thái sửa lỗi và bằng chứng chạy gần nhất nằm trong [docs/implementation_plan.md](docs/implementation_plan.md).
