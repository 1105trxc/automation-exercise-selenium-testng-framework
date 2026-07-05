# Test Plan – Automation Exercise Framework

**Document Version:** 1.0 (Draft – Phase 0)  
**Created:** 2026-07-05  
**Author:** [Your Name]  
**Status:** Draft

---

## 1. Overview

| Field | Value |
|---|---|
| **Project Name** | Automation Exercise Selenium TestNG Framework |
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

- [ ] Website https://automationexercise.com is accessible
- [ ] Test environment (local machine or CI runner) is configured
- [ ] Maven build succeeds: `mvn clean compile`
- [ ] All required test data JSON files are in place
- [ ] At least the Smoke suite passes with 0 failures

---

## 6. Exit Criteria

Testing is considered complete when:

- [ ] All 26 test cases are analyzed and classified (automated / not suitable)
- [ ] All suitable test cases are automated and passing
- [ ] Smoke suite: 100% pass rate
- [ ] Regression suite: ≥ 90% pass rate (accounting for known website flakiness)
- [ ] No unresolved **Blocker** or **Critical** bugs
- [ ] Allure report generated successfully
- [ ] GitHub Actions CI pipeline passes
- [ ] README and all documentation are complete

---

## 7. Risks and Mitigations

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| Public demo site instability | High – tests fail intermittently | Medium | Retry mechanism (configurable), known flaky list |
| Intrusive Ads / dynamic overlays | Medium – locators break | Medium | Handle ad overlays in BasePage if needed |
| Network dependency | Medium – timeouts | Low | Increase page load timeout in config |
| Shared public data | Medium – test data collisions | Medium | Use unique emails with timestamp suffix |
| Website data reset | Low – pre-seeded users deleted | Low | No reliance on pre-existing accounts |
| CAPTCHA appearance | High – blocks automation | Low | Flag test as Not Suitable if CAPTCHA triggered |
| UI structure changes | High – locators break | Medium | Use resilient locators (prefer ID > CSS > XPath) |

---

## 8. Deliverables

| Deliverable | Location | Status |
|---|---|---|
| Source code (framework) | `src/` | 🔲 Planned |
| Manual test cases | `docs/test-cases.xlsx` | 🔲 Planned |
| Requirement Traceability Matrix | `docs/requirement-traceability-matrix.md` | 🔄 In Progress |
| Bug reports | `docs/bug-reports/` | 🔲 Planned |
| Allure report | `reports/allure-results/` | 🔲 Planned |
| Execution report | `docs/execution-report.md` | 🔲 Planned |
| README | `README.md` | 🔲 Planned |
| CI/CD workflow | `.github/workflows/` | 🔲 Planned |

---

## 9. Test Schedule (Estimated)

| Phase | Description | Duration |
|---|---|---|
| Phase 0 | Analysis & Planning | 1–2 days |
| Phase 1 | Foundation (Maven, Driver, BasePage, Login tests) | 2–3 days |
| Phase 2 | Page Objects & Test Data (Registration, Products) | 2–3 days |
| Phase 3 | Cart & Checkout | 2–3 days |
| Phase 4 | Observability (Allure, Screenshot, Listener) | 1–2 days |
| Phase 5 | Test Execution (Suites, Cross-browser) | 1–2 days |
| Phase 6 | CI/CD & Portfolio Documentation | 2–3 days |

---

## 10. Test Case Summary

> See full test case file: `docs/test-cases.xlsx` (to be created in Phase 0)  
> See RTM: `docs/requirement-traceability-matrix.md`

| Module | Count | Automated | Not Suitable |
|---|---|---|---|
| Authentication & Registration | 5 | 5 | 0 |
| Contact & Navigation | 2 | 2 | 0 |
| Products | 5 | 5 | 0 |
| Subscription | 2 | 2 | 0 |
| Cart | 4 | 4 | 0 |
| Checkout & Order | 5 | 5 | 0 |
| UI / Scroll | 3 | 3 | 0 |
| **Total** | **26** | **26** | **0** |

> **Note:** All 26 test cases are suitable for automation. Website does not implement CAPTCHA on these flows.

---

*End of Test Plan – v1.0 Draft*
