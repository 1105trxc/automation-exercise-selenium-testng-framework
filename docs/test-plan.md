# Test Plan – Automation Exercise Selenium TestNG Framework

**Document Version:** 2.0  
**Created:** 2026-07-05  
**Last Updated:** 2026-07-12  
**Author:** 1105trxc  
**Status:** In Progress

---

## 1. Overview

| Field | Value |
|---|---|
| **Project Name** | Automation Exercise Selenium TestNG Framework |
| **Repository** | https://github.com/1105trxc/automation-exercise-selenium-testng-framework |
| **System Under Test** | https://automationexercise.com |
| **Test Objective** | Automate UI test cases to validate core e-commerce functionality including authentication, product browsing, cart management, and checkout flow |
| **Stakeholders** | QA Engineer (Author), Hiring Managers (Portfolio audience) |

---

## 2. Scope

### 2.1 In Scope

- UI functional testing of 26 public test scenarios from automationexercise.com
- Authentication flows: Register, Login (valid/invalid), Logout
- Product flows: Browse, Search, View details, Filter by Category/Brand
- Cart flows: Add product, Set quantity, Remove product, Subscription
- Checkout flows: Place order (Register while checkout, Register before checkout, Login before checkout)
- Contact Us form submission
- Scroll Up/Down behavior verification
- Cross-browser testing: Chrome (primary), Firefox (secondary)
- Headless execution mode
- CI/CD pipeline on GitHub Actions

### 2.2 Out of Scope (Phase 1 of Portfolio)

- Mobile automation
- API automation testing
- Performance / load testing
- Security testing
- Database testing
- Docker / Selenium Grid
- Cloud testing platforms (BrowserStack, SauceLabs)
- Video recording
- CAPTCHA handling (flagged as known risk)

---

## 3. Test Types

| Type | Description |
|---|---|
| **Functional Testing** | Verify each feature works per requirements |
| **UI Testing** | Verify visual state, navigation, and UI element behavior |
| **Smoke Testing** | Quick validation of critical paths (login, add-to-cart, checkout) |
| **Regression Testing** | Full 26-test suite run to ensure nothing is broken |
| **Negative Testing** | Invalid credentials, existing email registration |
| **Cross-Browser Testing** | Chrome + Firefox |

---

## 4. Test Environment

| Component | Value |
|---|---|
| **Operating System** | Windows 11 |
| **Browser (Primary)** | Google Chrome (latest stable) |
| **Browser (Secondary)** | Mozilla Firefox (latest stable) |
| **Java** | Java 21 (LTS) |
| **Selenium WebDriver** | 4.x (latest stable at build time) |
| **TestNG** | 7.x (latest stable at build time) |
| **Maven** | 3.9.x |
| **Allure** | 2.x |
| **Website URL** | https://automationexercise.com |

---

## 5. Entry Criteria

The test execution can begin when:

- [x] Website https://automationexercise.com is accessible
- [x] Test environment (local machine or CI runner) is configured
- [x] Maven build succeeds: `mvn clean compile`
- [x] All required test data JSON files are in place
- [x] At least the Smoke suite passes with 0 failures

---

## 6. Exit Criteria

Testing is considered complete when:

- [ ] All 26 test cases are analyzed and classified (automated / not suitable)
- [ ] All suitable test cases are automated and passing
- [ ] Smoke suite: 100% pass rate
- [ ] Regression suite: ≥ 90% pass rate (accounting for known website flakiness)
- [ ] No unresolved **Blocker** or **Critical** bugs
- [x] Allure report generated successfully
- [ ] GitHub Actions CI pipeline passes
- [ ] README and all documentation are complete

---

## 7. Risks and Mitigations

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| Public demo site instability | High – tests fail intermittently | Medium | Retry mechanism (configurable), known flaky list |
| Google Vignette ad overlay | High – blocks all UI interaction | High | **AdHandler** component: click Close → Escape → history.replaceState → navigate fallback |
| Banner ads intercepting clicks | Medium – ElementClickInterceptedException | Medium | AdHandler.hideInlineAds(); clickWithJsFallback() as explicit opt-in |
| Network dependency | Medium – timeouts | Low | Increase page load timeout in config |
| Shared public data | Medium – test data collisions | Medium | Use unique emails with timestamp suffix |
| Website data reset | Low – pre-seeded users deleted | Low | No reliance on pre-existing accounts |
| CAPTCHA appearance | High – blocks automation | Low | Flag test as Not Suitable if CAPTCHA triggered |
| UI structure changes | High – locators break | Medium | Use resilient locators (prefer ID > CSS > XPath) |

---

## 8. Deliverables

| Deliverable | Location | Status |
|---|---|---|
| Source code (framework) | `src/` | ✅ In Progress (Phase 2 complete, TC-001 to TC-008) |
| Requirement Traceability Matrix | `docs/requirement-traceability-matrix.md` | ✅ Maintained |
| Allure HTML report | `target/allure-results/` | ✅ Generated per run |
| README | `README.md` | 🔲 Planned |
| CI/CD workflow | `.github/workflows/` | 🔲 Planned |
| Bug reports | `docs/bug-reports/` | 🔲 Planned |

---

## 9. Test Schedule (Estimated)

| Phase | Description | Status |
|---|---|---|
| Phase 0 | Analysis & Planning | ✅ Done |
| Phase 1 | Foundation (Maven, Driver, BasePage, Login tests) | ✅ Done |
| Phase 2 | Page Objects & Test Data (Registration, Products, Contact) | ✅ Done – TC-001 to TC-008 |
| Phase 2 Refactor | BasePage/BaseTest architecture cleanup, AdHandler | ✅ Done – 2026-07-12 |
| Phase 3 | Cart & Checkout | 🔲 Planned |
| Phase 4 | Remaining test cases (TC-009 to TC-026) | 🔲 Planned |
| Phase 5 | CI/CD & Portfolio Documentation | 🔲 Planned |

---

## 10. Test Case Summary

> See RTM: `docs/requirement-traceability-matrix.md`

| Module | Count | Automated | In Progress | Planned |
|---|---|---|---|---|
| Authentication & Registration | 5 | 5 | 0 | 0 |
| Contact & Navigation | 2 | 2 | 0 | 0 |
| Products | 3 | 3 | 0 | 2 |
| Subscription | 2 | 0 | 0 | 2 |
| Cart | 4 | 0 | 0 | 4 |
| Checkout & Order | 5 | 0 | 0 | 5 |
| UI / Scroll | 3 | 0 | 0 | 3 |
| **Total** | **26** | **8** | **0** | **18** |

> **Note:** TC-001 to TC-008 automated. All 26 test cases are suitable for automation. Website does not implement CAPTCHA on these flows.

---

*End of Test Plan – v2.0*
