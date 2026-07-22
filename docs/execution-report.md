# Test Execution Report

## Execution Context
- **Date:** 2026-07-22
- **Commit:** `dc6ff34` (style: simplify framework source documentation)
- **Java Version:** Java 23 (pom.xml target: Java 21)
- **Browser:** Google Chrome 150.0.7871.129
- **Headless Mode:** Verified (Headed & Headless runs executed)
- **OS:** Windows 11 Home (amd64, 10.0)

## Logical Coverage
- **Logical Test Cases:** 26 (TC-AE-001 through TC-AE-026)
- **TestNG Executions:** 27
- **Note:** TC-AE-003 runs twice via `@DataProvider("invalidLoginData")` (1 logical test case, 2 executions).

## Smoke Suite (Headed)
- **Total Executions:** 11 (10 logical TCs)
- **Passed:** 11
- **Failed:** 0
- **Skipped:** 0
- **Duration:** 152.5 s (~2.5 min)
- **Status:** BUILD SUCCESS ✅

## Regression Run 1
- **Total Executions:** 27 (26 logical TCs)
- **Passed:** 23
- **Failed:** 4
- **Skipped:** 0
- **Duration:** 577.1 s (~9.6 min)
- **Status:** BUILD FAILURE ❌ (4 transient/network failures)

## Regression Run 2
- **Total Executions:** 27 (26 logical TCs)
- **Passed:** 25
- **Failed:** 2
- **Skipped:** 0
- **Duration:** 445.6 s (~7.4 min)
- **Status:** BUILD FAILURE ❌ (2 transient/ad failures)

## Regression Run 3
- **Total Executions:** 27 (26 logical TCs)
- **Passed:** 27
- **Failed:** 0
- **Skipped:** 0
- **Duration:** 481.3 s (~8.0 min)
- **Status:** BUILD SUCCESS ✅ (100% PASS)

## Headless Smoke
- **Total Executions:** 11 (10 logical TCs)
- **Passed:** 11
- **Failed:** 0
- **Skipped:** 0
- **Duration:** 168.6 s (~2.8 min)
- **Status:** BUILD SUCCESS ✅

## Failure Classification & Analysis

During Regression Runs 1 and 2, intermittent failures occurred due to live SUT network environment and dynamic third-party Google ad behavior on `automationexercise.com`:

| Test Execution | Root Cause Category | Observed Exception | Explanation |
|---|---|---|---|
| `CheckoutTest.placeOrderRegisterBeforeCheckout` | Environment / SUT Renderer Timeout | `TimeoutException: Timed out receiving message from renderer: 30.000` | SUT page loading stalled during remote navigation; resolved on Run 3. |
| `CartTest.verifyProductQuantityInCart` | SUT Page Load / Network Sync | `TimeoutException: waiting for visibility of div.product-information h2` | Product detail page load stalled due to live SUT delay; resolved on Run 3. |
| `CheckoutTest.downloadInvoiceAfterOrder` | SUT Redirect Delay | `AssertionError: FAIL: Home page should be visible` | SUT delayed homepage redirect after account creation; resolved on Run 3. |
| `CategoryBrandTest.viewBrandProducts` | Third-Party Ad Interception | `ElementClickInterceptedException: <div class="grippy-host"></div>` | Dynamic Google AdSense anchor overlay (`grippy-host`) intercepted brand link; resolved on Run 3. |
| `RegistrationTest.registerShouldFailWithExistingEmail` | Third-Party Ad Interception | `ElementClickInterceptedException: iframe id="aswift_4"` | Full-width Google ad container `aswift_4` covered Create Account submit button; resolved on Run 3. |

*Note: All 26 test cases passed 100% cleanly in Regression Run 3 (27/27 PASS) without code changes.*

## Retry Usage
- **Retry masking used:** No (No custom retry transformer or test re-run masking applied).

## Evidence Artifacts
- **Surefire Reports:** `target/surefire-reports/`
- **Screenshots:** `reports/screenshots/`
- **Logs:** `.system_generated/tasks/task-3600.log`, `task-3606.log`, `task-3615.log`, `task-3624.log`, `task-3630.log`
