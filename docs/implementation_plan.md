# Phase 3 – Remaining 18 Test Cases (TC-009 to TC-026)

## Background

Phase 2 hoàn thành 8 test cases (TC-001 đến TC-008). Phase 3 implement toàn bộ 18 test cases còn lại, bao gồm Cart, Checkout, Search, Category/Brand, Subscription, Scroll.

---

## Open Questions

> [!IMPORTANT]
> **TC-018 – Step 6 có typo trong test case gốc:**
> Text ghi là `"WOMEN - TOPS PRODUCTS"` nhưng step 5 bảo click "Dress".
> → Thực tế website: click Women → click Tops → heading là `"WOMEN - TOPS PRODUCTS"`.
> **Đề xuất:** Implement đúng theo flow thực tế (Women → Tops), ignore typo.

> [!IMPORTANT]
> **TC-024 – Verify download invoice:**
> "Download Invoice" chỉ verify nút tồn tại và có thể click được (không verify file thực sự tải về, vì điều đó phụ thuộc vào browser profile/OS – nằm ngoài scope UI automation).

> [!NOTE]
> **Shared helper pattern:**
> TC-014, 015, 016, 023, 024 đều cần bước "Add products to cart" (add sản phẩm từ ProductsPage).
> Sẽ tạo **private helper method** `addFirstProductToCart()` trong mỗi test class cần nó – không tạo base class mới để tránh over-engineering.

---

## Proposed Changes

### Nhóm 1 – Page Objects Mới

---

#### [NEW] CartPage.java

**Path:** `src/main/java/com/automationexercise/pages/CartPage.java`

**Dùng cho:** TC-011, 012, 013, 014, 015, 016, 017, 020, 022, 023, 024

**Methods:**
```
isCartPageVisible()                → verify "Shopping Cart" heading
isCartEmpty()                      → check tbody row count == 0
getProductCount()                  → số lượng row trong cart
getProductNameAtRow(int)           → tên sản phẩm theo row
getProductPriceAtRow(int)          → giá theo row
getProductQuantityAtRow(int)       → số lượng theo row
getProductTotalAtRow(int)          → tổng tiền theo row
isProductInCart(String productName)→ tìm theo tên sản phẩm
removeProductAtRow(int)            → click nút X
clickProceedToCheckout()           → return CheckoutPage HOẶC LoginPage
                                     (nếu chưa login sẽ có modal "Register/Login")
clickViewCartModal()               → click "View Cart" từ modal sau Add to Cart
clickContinueShopping()            → click "Continue Shopping" từ modal
isSubscriptionVisible()            → verify "SUBSCRIPTION" text ở footer
enterSubscriptionEmail(String)     → type email vào subscription form
clickSubscribeButton()             → click arrow button
isSubscribeSuccessVisible()        → verify success message
```

**Locator strategy:**
- Cart table rows: `By.cssSelector("tbody tr")`
- Remove button: `By.cssSelector("a.cart_quantity_delete")`
- Proceed to Checkout: `By.cssSelector(".btn.btn-default.check_out")`
- "Register/Login" link in modal: `By.xpath("//div[@id='checkoutModal']//a[contains(text(),'Register')]")`

---

#### [NEW] CheckoutPage.java

**Path:** `src/main/java/com/automationexercise/pages/CheckoutPage.java`

**Dùng cho:** TC-014, 015, 016, 023, 024

**Methods:**
```
isDeliveryAddressVisible()         → verify delivery address section
isBillingAddressVisible()          → verify billing address section
getDeliveryFullName()              → lấy tên đầy đủ của delivery address
getDeliveryAddress()               → lấy địa chỉ delivery
getBillingFullName()               → lấy tên đầy đủ billing
getBillingAddress()                → lấy địa chỉ billing
enterOrderComment(String)          → type vào comment textarea
clickPlaceOrder()                  → return PaymentPage
```

**Locator strategy:**
- Delivery address block: `By.id("address_delivery")`
- Billing address block: `By.id("address_invoice")`
- Comment textarea: `By.cssSelector("textarea.form-control")` hoặc `By.name("message")`
- Place Order button: `By.cssSelector(".btn.btn-default.check_out")`

---

#### [NEW] PaymentPage.java

**Path:** `src/main/java/com/automationexercise/pages/PaymentPage.java`

**Dùng cho:** TC-014, 015, 016, 024

**Methods:**
```
enterCardName(String)             → name on card
enterCardNumber(String)           → card number
enterCVC(String)                  → CVC/CVV
enterExpiryMonth(String)          → MM
enterExpiryYear(String)           → YYYY
clickPayAndConfirm()              → return PaymentSuccessPage
```

**Locator strategy:**
- `input[data-qa='name-on-card']`
- `input[data-qa='card-number']`
- `input[data-qa='cvc']`
- `input[data-qa='expiry-month']`
- `input[data-qa='expiry-year']`
- `button[data-qa='pay-button']`

---

#### [NEW] PaymentSuccessPage.java

**Path:** `src/main/java/com/automationexercise/pages/PaymentSuccessPage.java`

**Dùng cho:** TC-014, 015, 016, 024

**Methods:**
```
isOrderPlacedSuccessfully()       → verify "Your order has been placed successfully!"
clickDownloadInvoice()            → click download button (TC-024)
clickContinue()                   → return HomePage
```

**Locator strategy:**
- Success message: `By.xpath("//h2[@data-qa='order-placed']")` hoặc `//p[contains(text(),'placed successfully')]`
- Download Invoice: `By.xpath("//a[contains(text(),'Download Invoice')]")`
- Continue: `By.cssSelector("a[data-qa='continue-button']")`

---

### Nhóm 2 – Update Page Objects Hiện Có

---

#### [MODIFY] ProductsPage.java

Thêm methods cho:
- TC-012: Hover over product → Add to Cart → Continue Shopping / View Cart modal
- TC-018: Category sidebar (Women, Men, subcategories)
- TC-019: Brand sidebar
- TC-020: searchFor() đã có, cần thêm addAllSearchedProductsToCart()

**Locators mới cần thêm:**
```java
// Hover card
private static final By FIRST_PRODUCT_CARD    = By.cssSelector(".productinfo.text-center");
private static final By FIRST_ADD_TO_CART     = By.xpath("(//a[@class='btn btn-default add-to-cart'])[1]");
private static final By SECOND_ADD_TO_CART    = By.xpath("(//a[@class='btn btn-default add-to-cart'])[2]");

// Modal after Add to Cart
private static final By CONTINUE_SHOPPING_BTN = By.cssSelector(".btn-success.close-modal");
private static final By VIEW_CART_BTN         = By.cssSelector("a[href='/view_cart']");

// Category sidebar
private static final By CATEGORY_WOMEN        = By.xpath("//a[@href='#Women']");
private static final By CATEGORY_WOMEN_TOPS   = By.xpath("//div[@id='Women']//a[contains(@href,'/category_products/')]");
private static final By CATEGORY_MEN          = By.xpath("//a[@href='#Men']");
private static final By CATEGORY_MEN_TSHIRTS  = By.xpath("//div[@id='Men']//a[contains(@href,'/category_products/')]");
private static final By CATEGORY_PAGE_HEADING = By.cssSelector(".title.text-center");

// Brand sidebar (in ProductsPage)
private static final By FIRST_BRAND_LINK      = By.xpath("(//div[@class='brands-name']//a)[1]");
private static final By SECOND_BRAND_LINK     = By.xpath("(//div[@class='brands-name']//a)[2]");
private static final By BRANDS_SECTION        = By.cssSelector(".brands_products");
private static final By BRAND_PAGE_HEADING    = By.cssSelector(".title.text-center");

// Searched products list
private static final By SEARCHED_PRODUCT_ADD_TO_CART_BUTTONS =
    By.xpath("//a[@class='btn btn-default add-to-cart']");
```

**Methods mới:**
```java
hoverAndAddFirstProductToCart() → click Add to Cart sau hover
clickContinueShopping()         → đóng modal, ở lại ProductsPage
hoverAndAddSecondProductToCart()→ click Add to Cart sản phẩm thứ 2
clickViewCart()                 → return CartPage
isCategoryWomenVisible()        → verify category sidebar
clickWomenCategory()            → expand Women accordion
clickFirstWomenSubcategory()    → click Tops (or first link)
getCategoryPageHeading()        → get visible heading text
clickMenCategory()              → expand Men accordion
clickFirstMenSubcategory()
isBrandsSectionVisible()        → verify brand sidebar
clickFirstBrand()               → return ProductsPage (reload for brand)
getBrandPageHeading()           → get heading after brand click
clickSecondBrand()
addAllSearchedProductsToCart()  → click Add to Cart trên tất cả kết quả search
```

---

#### [MODIFY] ProductDetailPage.java

Thêm methods cho TC-013 (set quantity, add to cart):

```java
// Locators mới
private static final By QUANTITY_INPUT = By.id("quantity");
private static final By ADD_TO_CART_BTN = By.cssSelector(".cart .btn");
private static final By VIEW_CART_LINK = By.cssSelector("a[href='/view_cart']");

// Methods mới
setQuantity(int quantity)        → clear field, type new value
clickAddToCart()                 → click "Add to cart" button
clickViewCart()                  → return CartPage
```

---

#### [MODIFY] HomePage.java

Thêm methods cho TC-010, 022, 025, 026:

```java
// Locators mới
private static final By CART_LINK              = By.cssSelector("a[href='/view_cart']");
private static final By SUBSCRIPTION_HEADING   = By.xpath("//h2[normalize-space()='Subscription']");
private static final By SUBSCRIBE_EMAIL_INPUT  = By.id("susbscribe_email");
private static final By SUBSCRIBE_BUTTON       = By.id("subscribe");
private static final By SUBSCRIBE_SUCCESS_MSG  = By.cssSelector("div.alert-success");
private static final By RECOMMENDED_ITEMS      = By.cssSelector("#recommended-item-carousel");
private static final By FIRST_RECOMMENDED_ADD  = By.xpath("(//div[@id='recommended-item-carousel']//a[@class='btn btn-default add-to-cart'])[1]");
private static final By VIEW_CART_MODAL        = By.cssSelector("a[href='/view_cart']");
private static final By SCROLL_UP_BUTTON       = By.id("scrollUp");
private static final By HERO_TEXT              = By.xpath("//div[@class='item active']//h2");

// Methods mới
clickCart()                        → return CartPage
scrollToFooter()                   → scrollToBottom()
isSubscriptionHeadingVisible()     → verify "SUBSCRIPTION"
enterSubscriptionEmail(String)     → type email
clickSubscribeButton()             → click arrow
isSubscribeSuccessVisible()        → verify success message
isRecommendedItemsVisible()        → verify section
clickAddFirstRecommendedToCart()   → click Add to Cart
clickViewCartFromModal()           → return CartPage
clickScrollUpButton()              → click arrow button
isHeroTextVisible()                → verify top hero text
scrollUpWithKeyboard()             → sendKeys PAGE_UP on body
```

---

### Nhóm 3 – Model Classes Mới

---

#### [NEW] PaymentData.java

**Path:** `src/main/java/com/automationexercise/models/PaymentData.java`

```java
// Fields từ checkout.json
private String nameOnCard;   // "Test Card"
private String cardNumber;   // "4111111111111111"
private String cvc;          // "123"
private String expiryMonth;  // "12"
private String expiryYear;   // "2027"
private String orderComment; // "Test order comment"
```

---

### Nhóm 4 – Test Data Files Mới

---

#### [NEW] checkout.json

```json
{
  "paymentData": [
    {
      "nameOnCard": "Test Automation",
      "cardNumber": "4111111111111111",
      "cvc": "123",
      "expiryMonth": "12",
      "expiryYear": "2027",
      "orderComment": "Automated test order. Please ignore."
    }
  ]
}
```

---

#### [MODIFY] users.json

Không thêm field mới vào users.json vì `UserData` đã đủ địa chỉ cho TC-023.
(TC-023 dùng lại `UserData.getAddress()` để verify trên CheckoutPage.)

---

### Nhóm 5 – Test Classes Mới

---

#### [NEW] ProductSearchTest.java

**Path:** `src/test/java/com/automationexercise/tests/products/ProductSearchTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-009 | `searchProductByKeyword()` | Search "Blue Top", verify SEARCHED PRODUCTS heading + results visible |
| TC-AE-020 | `searchAndVerifyCartAfterLogin()` | Add searched products → view cart → login → verify cart preserved |

---

#### [NEW] SubscriptionTest.java

**Path:** `src/test/java/com/automationexercise/tests/subscription/SubscriptionTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-010 | `verifySubscriptionOnHomePage()` | Scroll to footer, enter email, verify success |
| TC-AE-011 | `verifySubscriptionOnCartPage()` | Navigate to cart, scroll, enter email, verify success |

---

#### [NEW] CartTest.java

**Path:** `src/test/java/com/automationexercise/tests/cart/CartTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-012 | `addTwoProductsAndVerifyCart()` | Hover add × 2, verify prices/qty/total |
| TC-AE-013 | `verifyProductQuantityInCart()` | Set qty=4 from detail page, verify in cart |
| TC-AE-017 | `removeProductFromCart()` | Add 1 product, click X, verify cart empty |
| TC-AE-022 | `addRecommendedItemToCart()` | Scroll to recommended section, add, view cart |

---

#### [NEW] CheckoutTest.java

**Path:** `src/test/java/com/automationexercise/tests/checkout/CheckoutTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-014 | `placeOrderRegisterWhileCheckout()` | Add to cart → checkout → register via modal → back to cart → checkout → pay → delete |
| TC-AE-015 | `placeOrderRegisterBeforeCheckout()` | Register → add to cart → checkout → pay → delete |
| TC-AE-016 | `placeOrderLoginBeforeCheckout()` | Register/login → add to cart → checkout → pay → delete |
| TC-AE-023 | `verifyAddressDetailsInCheckout()` | Register (save address data) → add to cart → checkout → verify address matches → delete |
| TC-AE-024 | `downloadInvoiceAfterOrder()` | Full order flow → verify success → click Download Invoice → continue → delete |

---

#### [NEW] CategoryBrandTest.java

**Path:** `src/test/java/com/automationexercise/tests/products/CategoryBrandTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-018 | `viewCategoryProducts()` | Home → Women → Tops → verify heading → Men → Tshirts → verify heading |
| TC-AE-019 | `viewBrandProducts()` | Products → click Brand 1 → verify heading → click Brand 2 → verify heading |

---

#### [NEW] ReviewTest.java

**Path:** `src/test/java/com/automationexercise/tests/products/ReviewTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-021 | `addReviewOnProduct()` | Products → View Product → enter name/email/review → Submit → verify "Thank you" |

---

#### [NEW] ScrollTest.java

**Path:** `src/test/java/com/automationexercise/tests/scroll/ScrollTest.java`

| TC | Method | Notes |
|---|---|---|
| TC-AE-025 | `scrollUpUsingArrowButton()` | Scroll down → verify "SUBSCRIPTION" → click scroll-up arrow → verify hero text |
| TC-AE-026 | `scrollUpWithoutArrowButton()` | Scroll down → verify "SUBSCRIPTION" → sendKeys PAGE_UP → verify hero text |

---

### Nhóm 6 – TestNG Suite Files

---

#### [MODIFY] smoke.xml

Thêm test classes mới vào smoke suite:
- `CartTest` (groups="smoke")
- `CheckoutTest` (groups="smoke" – chỉ TC-014)

#### [MODIFY] regression.xml

Thêm toàn bộ test classes mới.

---

## Architecture Decisions

### 1. Tại sao `PaymentSuccessPage` là page riêng thay vì method trong CheckoutPage?

Sau khi pay, URL thay đổi thành `/payment_done` và nội dung trang hoàn toàn khác.
Cần page riêng để thể hiện đúng trạng thái navigation.

### 2. TC-023 verify address như thế nào?

`UserData` đã có field `firstName`, `lastName`, `address`, `city`, `state`, `country`, `zipcode`.
Test sẽ:
1. Register với `UserData` data từ JSON (random email, fixed profile)
2. Lấy expected address từ `UserData` object
3. So sánh với `checkoutPage.getDeliveryAddress()` và `checkoutPage.getBillingAddress()`

### 3. TC-014 – cart không mất khi login?

Website `automationexercise.com` giữ cart trong session (cookie-based).
Khi guest thêm sản phẩm → checkout → register/login → cart vẫn còn.
Nếu site không giữ: step 12 của TC-014 "Click Cart button" → sẽ add lại.
Test implement đúng theo từng step của TC-014, không workaround.

### 4. Hover add-to-cart – dùng Actions hay jsClick?

Homepage product cards dùng hover reveal: `overlay` chứa "Add to cart" chỉ xuất hiện khi hover.
→ Dùng `hoverOver()` rồi `click()` bình thường.
→ Nếu flaky, fallback: `jsClick()` với lý do rõ ràng trong comment.

### 5. Subscription test email

Dùng `RandomDataUtils.generateUniqueEmail()` để tránh rate-limit từ website.

---

## Verification Plan

### Automated Tests
```bash
# Compile và chạy toàn bộ
mvn clean compile test-compile

# Chạy từng group
mvn clean test -Dtest=CartTest
mvn clean test -Dtest=CheckoutTest
mvn clean test -Dtest=SubscriptionTest
mvn clean test -Dtest=ProductSearchTest
mvn clean test -Dtest=CategoryBrandTest
mvn clean test -Dtest=ReviewTest
mvn clean test -Dtest=ScrollTest

# Chạy full regression
mvn clean test -DsuiteXmlFile=src/test/resources/suites/regression.xml
```

### Manual Verification
- Xem Allure report: `mvn allure:serve`
- Verify tất cả 26 TC xuất hiện đúng trong RTM
- Verify smoke suite chạy < 10 phút
