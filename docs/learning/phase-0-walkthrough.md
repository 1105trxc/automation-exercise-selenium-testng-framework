# Phase 0 – Analysis & Planning

## 🎯 Mục tiêu của Phase này

> "Đừng bao giờ bắt đầu code khi chưa biết mình đang code cái gì."

Phase 0 không viết một dòng code Java nào. Mục tiêu là **hiểu rõ toàn bộ scope** trước khi bắt đầu build framework. Đây là bước mà nhiều fresher bỏ qua và sau đó gặp khó khăn khi code vì không có bức tranh tổng thể.

---

## 📋 Những gì chúng ta đã làm trong Phase 0

### 1. Đọc và phân tích 26 test cases

Chúng ta đã truy cập trực tiếp vào `https://automationexercise.com/test_cases` để lấy danh sách chính xác 26 test cases. Tại sao cần làm vậy?

- Tránh viết automation cho test case sai hoặc lỗi thời
- Hiểu rõ từng bước của test case (step-by-step)
- Xác định được các **assertion** cần thực hiện (verify cái gì?)

### 2. Phân nhóm (Module Grouping)

Chúng ta đã nhóm 26 test cases thành 7 module:

| Module | Số TC | Lý do nhóm |
|---|---|---|
| Authentication | 3 | Cùng liên quan đến login/logout |
| Registration | 2 | Cùng liên quan đến tạo tài khoản |
| Products | 5 | Cùng liên quan đến xem/tìm kiếm sản phẩm |
| Subscription | 2 | Cùng liên quan đến đăng ký email |
| Cart | 4 | Cùng liên quan đến giỏ hàng |
| Checkout | 5 | Cùng liên quan đến đặt hàng/thanh toán |
| Contact & Navigation | 2 | Các tính năng hỗ trợ khác |
| UI/Scroll | 3 | Kiểm tra scroll behavior |

**Tại sao phân nhóm?**
- Tổ chức code theo module → dễ tìm kiếm, dễ maintain
- Chạy test theo nhóm (group) → tiết kiệm thời gian khi debug
- Dễ phân công nếu làm nhóm

### 3. Xác định Pages cần tạo Page Object

**Page Object Model (POM)** là design pattern quan trọng nhất trong Selenium Automation.

**Ý tưởng:** Mỗi trang web (page) được đại diện bởi một Java class. Class này chứa:
- Locators (cách tìm các element trên trang)
- Actions (các thao tác có thể thực hiện trên trang)

```
Ví dụ: Trang Login
→ LoginPage.java chứa:
  - Locator: emailInput, passwordInput, loginButton
  - Action: enterEmail(), enterPassword(), clickLogin()
```

**Danh sách Pages xác định:**

| Page Class | URL / Trigger | Mục đích |
|---|---|---|
| `HomePage` | `/` | Trang chủ, điểm xuất phát |
| `LoginPage` | `/login` | Trang đăng nhập & đăng ký tên |
| `SignupPage` | `/signup` | Form đăng ký chi tiết |
| `ProductsPage` | `/products` | Danh sách tất cả sản phẩm |
| `ProductDetailsPage` | `/product_details/{id}` | Chi tiết một sản phẩm |
| `CartPage` | `/view_cart` | Giỏ hàng |
| `CheckoutPage` | `/checkout` | Trang xác nhận đặt hàng |
| `PaymentPage` | `/payment` | Trang thanh toán |
| `ContactUsPage` | `/contact_us` | Form liên hệ |
| `TestCasesPage` | `/test_cases` | Trang test cases (TC-007) |

### 4. Xác định Components (UI tái sử dụng)

Component là các **vùng UI xuất hiện lặp lại** trên nhiều trang. Thay vì viết lại locator ở mỗi Page, chúng ta tạo một class riêng.

| Component | Xuất hiện ở | Chứa gì |
|---|---|---|
| `HeaderComponent` | Tất cả các trang | Nav menu, Login/Logout button, Logo |
| `FooterComponent` | Tất cả các trang | Subscription form |
| `ProductCardComponent` | ProductsPage, HomePage | Card sản phẩm: tên, giá, nút "Add to cart" |
| `CartItemComponent` | CartPage | Row sản phẩm: tên, giá, số lượng, nút xóa |

**Lưu ý quan trọng từ Blueprint:**
> "Không tạo component nếu chỉ dùng một lần."

### 5. Xác định Test Data cần chuẩn bị

Test data là dữ liệu đầu vào cho test. Trong framework này, chúng ta lưu test data trong file JSON, không hardcode trong code.

**Tại sao dùng JSON?**
- Thay đổi test data không cần sửa code
- Nhiều test có thể dùng chung một file data
- Dễ đọc, dễ review

```json
// users.json – ví dụ cấu trúc
{
  "validUsers": [
    {
      "name": "Test User",
      "email": "testuser@example.com",
      "password": "Test@12345"
    }
  ],
  "invalidLoginUsers": [
    {
      "email": "wrong@example.com",
      "password": "wrongpass",
      "expectedError": "Your email or password is incorrect!"
    }
  ]
}
```

**Các file JSON cần tạo:**

| File | Dùng cho TC |
|---|---|
| `users.json` | TC-001, TC-002, TC-003, TC-004, TC-005 |
| `products.json` | TC-009, TC-020 |
| `checkout.json` | TC-014, TC-015, TC-016, TC-023 |
| `contact-messages.json` | TC-006 |

### 6. Xác định Smoke Suite

Smoke Suite là tập hợp các test case **quan trọng nhất**, chạy nhanh nhất, giúp biết ngay "framework có đang chạy được không?"

8 test cases được đưa vào Smoke Suite:
- TC-AE-001: Register User ← Luồng quan trọng nhất
- TC-AE-002: Login valid ← Tiền điều kiện của nhiều test khác
- TC-AE-003: Login invalid ← Negative case quan trọng
- TC-AE-004: Logout ← Đảm bảo session management
- TC-AE-008: View Products ← Core feature
- TC-AE-009: Search Product ← Core feature
- TC-AE-012: Add to Cart ← Core e-commerce flow
- TC-AE-014: Place Order ← End-to-end flow

---

## 📁 Files được tạo trong Phase 0

```
automation-exercise-framework/
└── docs/
    ├── test-plan.md                          ← Tài liệu này
    ├── requirement-traceability-matrix.md    ← Ma trận traceability
    └── learning/
        └── phase-0-walkthrough.md            ← File bạn đang đọc
```

---

## 📚 Khái niệm quan trọng cần hiểu

### Test Plan là gì?

Test Plan là tài liệu mô tả **cách chúng ta sẽ test**, bao gồm:
- Phạm vi testing (In Scope / Out of Scope)
- Loại testing sẽ thực hiện
- Môi trường test
- Tiêu chí để bắt đầu test (Entry Criteria) và kết thúc (Exit Criteria)
- Rủi ro và cách xử lý

**Trong môi trường doanh nghiệp thực tế:**
- Test Plan được viết trước khi sprint bắt đầu
- Được review bởi Tech Lead / PM
- Là tài liệu tham chiếu trong suốt quá trình test

### RTM (Requirement Traceability Matrix) là gì?

RTM là bảng mapping giữa:
```
Requirement → Test Case → Automation Method → Execution Status → Bug
```

**Tại sao cần RTM?**
- Đảm bảo không bỏ sót requirement nào chưa được test
- Khi có bug, biết ngay requirement nào bị ảnh hưởng
- PM/Stakeholder nhìn vào RTM biết test coverage

**Trong portfolio của bạn:**
- RTM chứng minh bạn biết liên kết requirement ↔ test case ↔ automation
- Đây là kỹ năng quan trọng của một QA Engineer thực thụ

### Page Object Model (POM) là gì?

POM là design pattern để tổ chức Selenium code:

```
❌ Không dùng POM:
@Test
public void testLogin() {
    driver.findElement(By.id("email")).sendKeys("user@test.com");
    driver.findElement(By.id("password")).sendKeys("123456");
    driver.findElement(By.id("login-btn")).click();
    Assert.assertTrue(driver.findElement(By.id("user-name")).isDisplayed());
}
// Vấn đề: Nếu locator thay đổi → phải sửa ở nhiều chỗ
```

```
✅ Dùng POM:
@Test
public void testLogin() {
    loginPage.enterEmail("user@test.com");
    loginPage.enterPassword("123456");
    loginPage.clickLoginButton();
    Assert.assertTrue(homePage.isUserLoggedIn());
}
// Nếu locator thay đổi → chỉ sửa trong LoginPage.java
```

**Lợi ích:**
- Tách biệt test logic và UI interaction
- Dễ maintain khi UI thay đổi
- Code dễ đọc hơn
- Tái sử dụng được

---

## ✅ Checklist Phase 0

Trước khi sang Phase 1, hãy xác nhận:

- [ ] Bạn đã đọc tất cả 26 test cases tại `https://automationexercise.com/test_cases`
- [ ] Bạn hiểu mỗi test case cần verify cái gì (assertion)
- [ ] Bạn hiểu sự khác biệt giữa TC-014, TC-015, TC-016 (3 checkout flows)
- [ ] Bạn đã đọc `docs/test-plan.md` và hiểu Entry/Exit Criteria
- [ ] Bạn đã đọc `docs/requirement-traceability-matrix.md`
- [ ] Bạn hiểu Page Object Model là gì
- [ ] Bạn biết Smoke Suite gồm những TC nào và tại sao
- [ ] Bạn đã cài Java 21, Maven, IntelliJ IDEA

---

## ❓ Câu hỏi tự kiểm tra

1. Tại sao TC-014 (Register while Checkout) lại phức tạp hơn TC-016 (Login before Checkout)?
2. Tại sao chúng ta cần unique email cho mỗi lần chạy test registration?
3. Sự khác biệt giữa Page và Component là gì?
4. Tại sao test data phải nằm ngoài code (trong JSON) thay vì hardcode trong Java?
5. Smoke Suite và Regression Suite khác nhau như thế nào?

*(Gợi ý: Câu trả lời nằm trong Blueprint và walkthrough này)*

---

## 🚀 Phase tiếp theo: Phase 1 – Foundation

Khi bạn sẵn sàng, chúng ta sẽ bắt đầu Phase 1:
- Khởi tạo Maven project
- Tạo `pom.xml` với đúng dependencies
- Xây dựng `DriverFactory` (quản lý WebDriver)
- Xây dựng `BasePage` (các action tái sử dụng)
- Xây dựng `BaseTest` (setup/teardown)
- Viết 3 test đầu tiên: Login valid, Login invalid, Logout

Hãy nói "Phase 1" khi bạn sẵn sàng! 🎉
