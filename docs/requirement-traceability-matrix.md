# Requirement Traceability Matrix (RTM)

**Project:** Automation Exercise Selenium TestNG Framework  
**Repository:** https://github.com/1105trxc/automation-exercise-selenium-testng-framework  
**Version:** 2.0  
**Last Updated:** 2026-07-22

---

## Legend

| Column | Description |
|---|---|
| **TC-ID** | Test Case ID (internal mapping) |
| **AE-TC** | Original Automation Exercise test case number |
| **Module** | Feature module |
| **Test Scenario** | Brief description |
| **Type** | Positive / Negative |
| **Priority** | P1 (Critical) / P2 (High) / P3 (Medium) |
| **Smoke** | ✅ if included in Smoke suite |
| **Status** | ✅ Automated |
| **Automated Method** | Java test method name |
| **Class** | Test class file |

---

## RTM Table

| TC-ID | AE-TC | Module | Test Scenario | Type | Priority | Smoke | Status | Automated Method | Class |
|---|---|---|---|---|---|---|---|---|---|
| TC-AE-001 | Test Case 1 | Registration | Register user successfully | Positive | P1 | ✅ | ✅ Automated | `registerNewUserSuccessfully` | `RegistrationTest` |
| TC-AE-002 | Test Case 2 | Authentication | Login with correct email and password | Positive | P1 | ✅ | ✅ Automated | `loginWithValidCredentials` | `LoginTest` |
| TC-AE-003 | Test Case 3 | Authentication | Login with incorrect email and password (*runs ×2 via DataProvider*) | Negative | P1 | ✅ | ✅ Automated | `loginShouldFailWithInvalidCredentials` | `LoginTest` |
| TC-AE-004 | Test Case 4 | Authentication | Logout user | Positive | P1 | ✅ | ✅ Automated | `logoutUserSuccessfully` | `LoginTest` |
| TC-AE-005 | Test Case 5 | Registration | Register with existing email | Negative | P2 | – | ✅ Automated | `registerShouldFailWithExistingEmail` | `RegistrationTest` |
| TC-AE-006 | Test Case 6 | Contact | Submit Contact Us form | Positive | P2 | ✅ | ✅ Automated | `submitContactUsFormSuccessfully` | `ContactUsTest` |
| TC-AE-007 | Test Case 7 | Navigation | Verify Test Cases page navigation | Positive | P3 | ✅ | ✅ Automated | `navigateToTestCasesPageSuccessfully` | `ProductsTest` |
| TC-AE-008 | Test Case 8 | Products | Verify All Products and product detail page | Positive | P2 | ✅ | ✅ Automated | `verifyAllProductsAndProductDetailPage` | `ProductsTest` |
| TC-AE-009 | Test Case 9 | Products | Search product by keyword | Positive | P2 | ✅ | ✅ Automated | `searchProductByKeyword` | `ProductSearchTest` |
| TC-AE-010 | Test Case 10 | Subscription | Verify subscription in home page | Positive | P3 | – | ✅ Automated | `verifySubscriptionOnHomePage` | `SubscriptionTest` |
| TC-AE-011 | Test Case 11 | Subscription | Verify subscription in cart page | Positive | P3 | – | ✅ Automated | `verifySubscriptionOnCartPage` | `SubscriptionTest` |
| TC-AE-012 | Test Case 12 | Cart | Add products in cart and verify details | Positive | P1 | ✅ | ✅ Automated | `addTwoProductsAndVerifyCart` | `CartTest` |
| TC-AE-013 | Test Case 13 | Cart | Verify product quantity in cart | Positive | P2 | – | ✅ Automated | `verifyProductQuantityInCart` | `CartTest` |
| TC-AE-014 | Test Case 14 | Checkout | Place order: Register while Checkout | Positive | P1 | ✅ | ✅ Automated | `placeOrderRegisterWhileCheckout` | `CheckoutTest` |
| TC-AE-015 | Test Case 15 | Checkout | Place order: Register before Checkout | Positive | P1 | – | ✅ Automated | `placeOrderRegisterBeforeCheckout` | `CheckoutTest` |
| TC-AE-016 | Test Case 16 | Checkout | Place order: Login before Checkout | Positive | P1 | – | ✅ Automated | `placeOrderLoginBeforeCheckout` | `CheckoutTest` |
| TC-AE-017 | Test Case 17 | Cart | Remove product from cart | Positive | P2 | – | ✅ Automated | `removeProductFromCart` | `CartTest` |
| TC-AE-018 | Test Case 18 | Products | View Category products (Women/Men) | Positive | P2 | – | ✅ Automated | `viewCategoryProducts` | `CategoryBrandTest` |
| TC-AE-019 | Test Case 19 | Products | View and Cart Brand products | Positive | P2 | – | ✅ Automated | `viewBrandProducts` | `CategoryBrandTest` |
| TC-AE-020 | Test Case 20 | Products | Search products and verify cart after login | Positive | P2 | – | ✅ Automated | `searchAndVerifyCartAfterLogin` | `ProductSearchTest` |
| TC-AE-021 | Test Case 21 | Products | Add review on product | Positive | P3 | – | ✅ Automated | `addReviewOnProduct` | `ReviewTest` |
| TC-AE-022 | Test Case 22 | Cart | Add to cart from Recommended items | Positive | P2 | – | ✅ Automated | `addRecommendedItemToCart` | `CartTest` |
| TC-AE-023 | Test Case 23 | Checkout | Verify address details in checkout page | Positive | P2 | – | ✅ Automated | `verifyAddressDetailsInCheckout` | `CheckoutTest` |
| TC-AE-024 | Test Case 24 | Checkout | Download Invoice after purchase order | Positive | P2 | – | ✅ Automated | `downloadInvoiceAfterOrder` | `CheckoutTest` |
| TC-AE-025 | Test Case 25 | UI/Scroll | Verify Scroll Up using Arrow button | Positive | P3 | – | ✅ Automated | `scrollUpUsingArrowButton` | `ScrollTest` |
| TC-AE-026 | Test Case 26 | UI/Scroll | Verify Scroll Up without Arrow button | Positive | P3 | – | ✅ Automated | `scrollUpWithoutArrowButton` | `ScrollTest` |

---

## Summary Statistics

| Metric | Value |
|---|---|
| Logical Test Cases | 26 |
| Total TestNG Executions | 27 (*TC-AE-003 runs 2 data rows via DataProvider*) |
| Suitable for Automation | 26 |
| Not Suitable | 0 |
| ✅ Automated | **26** (100% of logical test cases) |
| 🔲 Planned | 0 |
| In Smoke Suite | 10 logical TCs (11 TestNG executions) |

---

## Smoke Suite Test Cases

The following 10 test cases (11 executions) are designated for the **Smoke Suite**:

| # | TC-ID | Scenario | Class | Method | Status |
|---|---|---|---|---|---|
| 1 | TC-AE-001 | Register new user | `RegistrationTest` | `registerNewUserSuccessfully` | ✅ Automated |
| 2 | TC-AE-002 | Login with valid credentials | `LoginTest` | `loginWithValidCredentials` | ✅ Automated |
| 3 | TC-AE-003 | Login with invalid credentials (*×2*) | `LoginTest` | `loginShouldFailWithInvalidCredentials` | ✅ Automated |
| 4 | TC-AE-004 | Logout user | `LoginTest` | `logoutUserSuccessfully` | ✅ Automated |
| 5 | TC-AE-006 | Submit Contact Us form | `ContactUsTest` | `submitContactUsFormSuccessfully` | ✅ Automated |
| 6 | TC-AE-007 | Verify Test Cases page | `ProductsTest` | `navigateToTestCasesPageSuccessfully` | ✅ Automated |
| 7 | TC-AE-008 | Verify all products and product detail page | `ProductsTest` | `verifyAllProductsAndProductDetailPage` | ✅ Automated |
| 8 | TC-AE-009 | Search product | `ProductSearchTest` | `searchProductByKeyword` | ✅ Automated |
| 9 | TC-AE-012 | Add products in cart | `CartTest` | `addTwoProductsAndVerifyCart` | ✅ Automated |
| 10 | TC-AE-014 | Place order: Register while checkout | `CheckoutTest` | `placeOrderRegisterWhileCheckout` | ✅ Automated |

---

## Module → Page Object Mapping

| Module | Pages Involved | Components |
|---|---|---|
| Authentication | `LoginPage`, `HomePage` | `HeaderComponent` |
| Registration | `LoginPage`, `SignupPage`, `AccountCreatedPage`, `AccountDeletedPage`, `HomePage` | `HeaderComponent` |
| Products | `ProductsPage`, `ProductDetailPage`, `HomePage` | `HeaderComponent`, `AddToCartModal` |
| Subscription | `HomePage`, `CartPage` | `FooterSubscriptionComponent`, `HeaderComponent` |
| Cart | `ProductsPage`, `CartPage`, `ProductDetailPage` | `AddToCartModal`, `HeaderComponent` |
| Checkout | `CartPage`, `CheckoutPage`, `PaymentPage`, `PaymentSuccessPage`, `LoginPage`, `SignupPage` | `HeaderComponent` |
| Contact | `ContactUsPage`, `HomePage` | `HeaderComponent` |
| Navigation | `TestCasesPage`, `HomePage` | `HeaderComponent` |
| UI/Scroll | `HomePage` | `HeaderComponent` |

---

## Test Data Requirements

| Data Category | JSON File | Model Class | Used In TC |
|---|---|---|---|
| Valid user accounts | `users.json` | `UserData.java` | TC-001, TC-002, TC-004, TC-005, TC-014, TC-015, TC-016, TC-023, TC-024 |
| Login credentials (invalid) | `users.json` | `LoginData.java` | TC-003 |
| Contact form data | `contact_form.json` | `ContactFormData.java` | TC-006 |
| Checkout payment data | `checkout.json` | `PaymentData.java` | TC-014, TC-015, TC-016, TC-024 |

---

*End of RTM – v2.0*
