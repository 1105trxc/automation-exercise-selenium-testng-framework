# Requirement Traceability Matrix (RTM)

**Project:** Automation Exercise Selenium TestNG Framework  
**Version:** 1.0 (Draft – Phase 0)  
**Date:** 2026-07-05

---

## Legend

| Column | Description |
|---|---|
| **TC-ID** | Test Case ID (our internal mapping) |
| **AE-TC** | Original Automation Exercise test case number |
| **Module** | Feature module |
| **Test Scenario** | Brief description |
| **Type** | Positive / Negative |
| **Priority** | P1 (Critical) / P2 (High) / P3 (Medium) / P4 (Low) |
| **Smoke** | ✅ if included in Smoke suite |
| **Automation Status** | Planned / In Progress / Automated / Not Suitable |
| **Automated Method** | Java test method name |
| **Bug ID** | Linked bug if found |

---

## RTM Table

| TC-ID | AE-TC | Module | Test Scenario | Type | Priority | Smoke | Automation Status | Automated Method | Bug ID |
|---|---|---|---|---|---|---|---|---|---|
| TC-AE-001 | Test Case 1 | Registration | Register new user successfully | Positive | P1 | ✅ | Planned | `registerUserSuccessfully` | – |
| TC-AE-002 | Test Case 2 | Authentication | Login with correct email and password | Positive | P1 | ✅ | Planned | `loginWithValidCredentials` | – |
| TC-AE-003 | Test Case 3 | Authentication | Login with incorrect email and password | Negative | P1 | ✅ | Planned | `loginShouldFailWithInvalidCredentials` | – |
| TC-AE-004 | Test Case 4 | Authentication | Logout user | Positive | P1 | ✅ | Planned | `logoutUserSuccessfully` | – |
| TC-AE-005 | Test Case 5 | Registration | Register with existing email | Negative | P2 | – | Planned | `registerWithExistingEmailShouldShowError` | – |
| TC-AE-006 | Test Case 6 | Contact | Submit Contact Us form with file upload | Positive | P2 | – | Planned | `submitContactUsFormSuccessfully` | – |
| TC-AE-007 | Test Case 7 | Navigation | Verify Test Cases page navigation | Positive | P3 | – | Planned | `navigateToTestCasesPageSuccessfully` | – |
| TC-AE-008 | Test Case 8 | Products | Verify All Products and product detail page | Positive | P2 | ✅ | Planned | `verifyAllProductsAndProductDetailPage` | – |
| TC-AE-009 | Test Case 9 | Products | Search product by keyword | Positive | P2 | ✅ | Planned | `searchProductByKeyword` | – |
| TC-AE-010 | Test Case 10 | Subscription | Verify subscription in home page | Positive | P3 | – | Planned | `verifySubscriptionOnHomePage` | – |
| TC-AE-011 | Test Case 11 | Subscription | Verify subscription in cart page | Positive | P3 | – | Planned | `verifySubscriptionOnCartPage` | – |
| TC-AE-012 | Test Case 12 | Cart | Add products in cart and verify details | Positive | P1 | ✅ | Planned | `addProductsToCartAndVerifyDetails` | – |
| TC-AE-013 | Test Case 13 | Cart | Verify product quantity in cart | Positive | P2 | – | Planned | `verifyProductQuantityInCart` | – |
| TC-AE-014 | Test Case 14 | Checkout | Place order: Register while Checkout | Positive | P1 | ✅ | Planned | `placeOrderRegisterWhileCheckout` | – |
| TC-AE-015 | Test Case 15 | Checkout | Place order: Register before Checkout | Positive | P1 | – | Planned | `placeOrderRegisterBeforeCheckout` | – |
| TC-AE-016 | Test Case 16 | Checkout | Place order: Login before Checkout | Positive | P1 | – | Planned | `placeOrderLoginBeforeCheckout` | – |
| TC-AE-017 | Test Case 17 | Cart | Remove product from cart | Positive | P2 | – | Planned | `removeProductFromCart` | – |
| TC-AE-018 | Test Case 18 | Products | View Category products (Women/Men) | Positive | P2 | – | Planned | `viewCategoryProducts` | – |
| TC-AE-019 | Test Case 19 | Products | View and Cart Brand products | Positive | P2 | – | Planned | `viewAndCartBrandProducts` | – |
| TC-AE-020 | Test Case 20 | Products | Search products and verify cart after login | Positive | P2 | – | Planned | `searchProductsAndVerifyCartAfterLogin` | – |
| TC-AE-021 | Test Case 21 | Products | Add review on product | Positive | P3 | – | Planned | `addReviewOnProduct` | – |
| TC-AE-022 | Test Case 22 | Cart | Add to cart from Recommended items | Positive | P2 | – | Planned | `addToCartFromRecommendedItems` | – |
| TC-AE-023 | Test Case 23 | Checkout | Verify address details in checkout page | Positive | P2 | – | Planned | `verifyAddressDetailsInCheckoutPage` | – |
| TC-AE-024 | Test Case 24 | Checkout | Download Invoice after purchase order | Positive | P2 | – | Planned | `downloadInvoiceAfterPurchaseOrder` | – |
| TC-AE-025 | Test Case 25 | UI/Scroll | Verify Scroll Up using Arrow button | Positive | P3 | – | Planned | `verifyScrollUpUsingArrowButton` | – |
| TC-AE-026 | Test Case 26 | UI/Scroll | Verify Scroll Up without Arrow button | Positive | P3 | – | Planned | `verifyScrollUpWithoutArrowButton` | – |

---

## Summary Statistics

| Metric | Value |
|---|---|
| Total Test Cases | 26 |
| Suitable for Automation | 26 |
| Not Suitable | 0 |
| Automated (current) | 0 |
| Planned | 26 |
| In Smoke Suite | 8 |

---

## Smoke Suite Test Cases

The following test cases are designated for the **Smoke Suite** – the most critical path that must pass before any deployment/merge:

| # | TC-ID | Scenario |
|---|---|---|
| 1 | TC-AE-001 | Register new user |
| 2 | TC-AE-002 | Login with valid credentials |
| 3 | TC-AE-003 | Login with invalid credentials |
| 4 | TC-AE-004 | Logout user |
| 5 | TC-AE-008 | Verify all products and product detail page |
| 6 | TC-AE-009 | Search product |
| 7 | TC-AE-012 | Add products in cart |
| 8 | TC-AE-014 | Place order: Register while checkout |

---

## Module → Page Object Mapping

| Module | Pages Involved | Components Involved |
|---|---|---|
| Authentication | `LoginPage`, `HomePage` | `HeaderComponent` |
| Registration | `LoginPage`, `SignupPage`, `HomePage` | `HeaderComponent` |
| Products | `ProductsPage`, `ProductDetailsPage`, `HomePage` | `HeaderComponent`, `ProductCardComponent` |
| Subscription | `HomePage`, `CartPage` | `FooterComponent` |
| Cart | `ProductsPage`, `CartPage`, `ProductDetailsPage` | `ProductCardComponent`, `CartItemComponent` |
| Checkout | `CartPage`, `CheckoutPage`, `PaymentPage`, `LoginPage`, `SignupPage` | `HeaderComponent` |
| Contact | `ContactUsPage`, `HomePage` | `HeaderComponent` |
| Navigation | `TestCasesPage`, `HomePage` | `HeaderComponent` |
| UI/Scroll | `HomePage` | – |

---

## Test Data Requirements

| Data Category | JSON File | Model Class | Usage |
|---|---|---|---|
| Valid user accounts | `users.json` | `UserData.java` | Registration, Login before Checkout |
| Login credentials (valid/invalid) | `users.json` | `LoginData.java` | TC-002, TC-003 |
| Product search keywords | `products.json` | `ProductData.java` | TC-009, TC-020 |
| Checkout address | `checkout.json` | `CheckoutData.java` | TC-014, TC-015, TC-016, TC-023 |
| Contact form data | `contact-messages.json` | `ContactData.java` | TC-006 |

---

*End of RTM – v1.0 Draft*
