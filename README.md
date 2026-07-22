# Automation Exercise Selenium TestNG Framework

Selenium Java TestNG automation framework for [Automation Exercise](https://automationexercise.com). A clean, maintainable portfolio / QA automation testing framework.

## Project Overview

- **Language:** Java 21
- **Testing Framework:** TestNG 7.10+
- **Automation Tool:** Selenium WebDriver 4.45+
- **Build Tool:** Maven 3.9+
- **Reporting:** Allure Report & Surefire
- **Test Coverage:** 26 logical test cases / 27 TestNG executions (100% automated)

---

## Architecture

```text
Tests -> Flows -> Pages/Components -> Core Infrastructure
```

- **`tests`**: Test cases owning business assertions, TestNG annotations, and data providers.
- **`flows`**: Reusable multi-step business journeys (`UserFlow`, `CartFlow`).
- **`pages`**: Page Objects encapsulating single-page UI interactions (`HomePage`, `LoginPage`, `CartPage`, `CheckoutPage`, `ProductsPage`, etc.).
- **`components`**: Reusable UI components (`HeaderComponent`, `FooterSubscriptionComponent`, `AddToCartModal`) and site-specific `AdHandler`.
- **`driver` / `config` / `utils`**: ThreadLocal WebDriver lifecycle, configuration, download management, and per-thread account cleanup.

*No `Thread.sleep`, generic JavaScript clicks, retry transformers, or artificial test masking are used.*

---

## Documentation Links

- [Requirement Traceability Matrix (RTM)](docs/requirement-traceability-matrix.md)
- [Architecture Documentation](docs/architecture.md)
- [Execution Report](docs/execution-report.md)

---

## Test Coverage & Execution Evidence

The framework covers **26 logical test cases** mapped to **27 TestNG executions** (TC-AE-003 runs twice via `@DataProvider("invalidLoginData")`).

| Suite | Executions | Status | Details |
|---|---|---|---|
| **Smoke Suite (Headed)** | 11 / 11 | **100% PASS** ✅ | Critical path tests (Authentication, Cart, Checkout, Products) |
| **Smoke Suite (Headless)** | 11 / 11 | **100% PASS** ✅ | Headless execution mode verification |
| **Regression Suite (Run 3)** | 27 / 27 | **100% PASS** ✅ | Complete regression suite execution |

*Detailed execution logs, run-by-run breakdown, and failure classifications are available in the [Test Execution Report](docs/execution-report.md).*

---

## Prerequisites & Setup

### Requirements
- JDK 21
- Maven 3.9+
- Chrome, Firefox, or Edge browser

Selenium Manager automatically manages browser driver binaries.

### Configuration

Create a local configuration file from the template:

**PowerShell:**
```powershell
Copy-Item .env.example src/main/resources/config/local.properties
```

**Bash:**
```bash
cp .env.example src/main/resources/config/local.properties
```

Key configuration parameters (`local.properties`):
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

---

## Running Tests

### Compile
```bash
mvn clean test-compile
```

### Smoke Suite
```bash
mvn clean test "-DsuiteXmlFile=src/test/resources/suites/smoke.xml"
```

### Headless Smoke Suite
```bash
mvn clean test "-Dheadless=true" "-DsuiteXmlFile=src/test/resources/suites/smoke.xml"
```

### Full Regression Suite
```bash
mvn clean test "-DsuiteXmlFile=src/test/resources/suites/regression.xml"
```

### Override Browser
```bash
mvn clean test "-Dbrowser=firefox" "-Dheadless=true" "-DsuiteXmlFile=src/test/resources/suites/smoke.xml"
```

---

## Reports & Artifacts

- **Surefire Reports:** `target/surefire-reports/`
- **Allure Results:** `target/allure-results/`
- **Failure Screenshots:** `reports/screenshots/`
- **Downloads:** Configured via `downloadDir` (`target/downloads`)

---

## CI / CD Integration

GitHub Actions workflow (`.github/workflows/ui-tests.yml`) executes the Smoke suite on every push and pull request. Manual triggers (`workflow_dispatch`) allow executing either `smoke` or `regression` suites with automatic evidence artifact upload.

---

## Known Limitations

- **Public SUT Delays:** `automationexercise.com` is a public demo website subject to occasional network delays and renderer timeouts.
- **Third-Party Ads:** Dynamic Google AdSense overlays (`aswift_*`, `grippy-host`) are served externally by Google and managed via `AdHandler`.
- **Download Directory:** File download verification is configured for Chrome, Edge, and Firefox via native browser preferences.
