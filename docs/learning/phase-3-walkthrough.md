# Phase 3 – Mua Sắm & Thanh Toán (Products, Cart & Checkout)

## 🎯 Mục tiêu của Phase này

Hoàn thiện toàn bộ các luồng e-commerce cốt lõi nhất của hệ thống Automation Exercise, từ việc xem sản phẩm cho đến lúc thanh toán thành công. Sau Phase này:
- 9 test cases quan trọng về giỏ hàng (Cart) và thanh toán (Checkout) được thiết lập thành công.
- Framework **vượt qua được "ải" quảng cáo Google Ads toàn màn hình** cực kỳ khó chịu.
- Framework khắc phục triệt để các lỗi "Click hụt" (Silent Click Failures) thường thấy trong môi trường mạng yếu hoặc giao diện phức tạp.

---

## 📁 Files được tạo/sửa đổi trong Phase 3

```
automation-exercise-framework/
├── src/main/java/com/automationexercise/
│   ├── components/AdHandler.java                     ← Xử lý quảng cáo Google (Vignette Ads)
│   ├── pages/ProductsPage.java                       ← Page Object trang Products (Hover, View)
│   ├── pages/ProductDetailPage.java                  ← Page Object trang chi tiết sản phẩm
│   ├── pages/CartPage.java                           ← Page Object trang Giỏ Hàng
│   ├── pages/CheckoutPage.java                       ← Page Object trang Thanh Toán
│   ├── pages/PaymentPage.java                        ← Page Object nhập thẻ tín dụng
│   └── pages/PaymentSuccessPage.java                 ← Page Object tải Invoice (Hóa đơn)
│
├── src/test/java/com/automationexercise/
│   ├── tests/products/ProductsTest.java              ← Test logic trang sản phẩm
│   ├── tests/cart/CartTest.java                      ← Test logic thêm vào giỏ, kiểm tra số lượng
│   └── tests/checkout/CheckoutTest.java              ← Test logic đặt hàng, nhập địa chỉ
```

---

## 📚 Khái niệm quan trọng cần hiểu

### 1. Tại sao Quảng Cáo (Ads) lại làm Test Fail?

**Vấn đề:** 
Trên trang web `automationexercise.com`, khi người dùng bấm vào một link (ví dụ chuyển từ Cart sang Checkout), Google Ads đôi khi sẽ bung ra một popup tàng hình hoặc một `iframe` che toàn bộ màn hình (Vignette Ad).
Nếu Selenium dùng hàm `click()` ngay lúc đó, nó sẽ click trúng cái quảng cáo thay vì click vào nút Checkout. Tệ hơn, nếu quảng cáo che mất nút, Selenium sẽ văng lỗi `ElementClickInterceptedException`.

**Giải pháp với `AdHandler.java`:**
Chúng ta không thể tắt quảng cáo bằng cách bấm nút "X" vì nút "X" xuất hiện rất chậm và ngẫu nhiên. Thay vào đó, chúng ta dùng **Javascript Executor** để can thiệp trực tiếp vào mã nguồn HTML của trang, biến quảng cáo thành "tàng hình" ngay tức khắc!
```java
// Ép quảng cáo ẩn đi ngay lập tức
js.executeScript(
    "var ads = document.querySelectorAll('iframe, ins');" +
    "for(var i=0; i<ads.length; i++) {" +
    "   ads[i].style.display = 'none';" +
    "   ads[i].style.zIndex = '-9999';" +
    "}"
);
```

### 2. Sự khác biệt giữa `click()` và `jsClick()`

**Selenium `click()` thông thường:**
- Mô phỏng hành vi click của người dùng thật (con trỏ chuột).
- Nó kiểm tra xem element có hiển thị (visible) không, có nằm trong màn hình không (viewport), có bị thẻ khác đè lên không.
- Nếu bị đè, nó sẽ ném lỗi `ElementClickInterceptedException`.
- **Nhược điểm:** Dễ bị fail vô cớ (Flaky) khi trang web có overlay, quảng cáo, hoặc đang có hiệu ứng cuộn (scroll animation).

**Javascript Click `jsClick()`:**
- Ra lệnh trực tiếp cho trình duyệt thực thi hành động click thông qua Javascript DOM API.
- **Bỏ qua mọi sự kiểm tra:** Kể cả khi nút bấm đang bị che khuất bởi quảng cáo, hoặc đang nằm ngoài màn hình, `jsClick()` vẫn click trúng đích!
```java
// Cách sử dụng jsClick trong BasePage
public void jsClick(By locator) {
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
}
```
*Lưu ý: Chỉ nên dùng `jsClick()` khi `click()` thông thường liên tục thất bại vì lý do giao diện, bởi vì `jsClick()` không phản ánh đúng 100% trải nghiệm của người dùng thực.*

### 3. CSS Locator Ẩn Tàng - Cạm Bẫy Trong HTML

Trong test `TC-AE-023: Verify address details`, Selenium liên tục báo lỗi Timeout khi tìm khối chứa Địa Chỉ (Address).
**Locator cũ:** `#address_delivery .address_address1.address_address2`
(Tìm element vừa có class `address_address1` vừa có class `address_address2`)

**Tại sao sai?**
Khi chúng ta dump HTML của trang web, chúng ta phát hiện trang web sinh ra **3 thẻ `<li>` giống hệt nhau** có chung class này:
- Thẻ 1: Tên Công Ty (nếu lúc đăng ký bỏ trống, thẻ này rỗng và tàng hình).
- Thẻ 2: Địa chỉ 1 (Có chứa text thực sự).
- Thẻ 3: Địa chỉ 2 (Bỏ trống).

Vì Selenium mặc định lấy **element đầu tiên** nó tìm thấy, nó sẽ bốc trúng Thẻ 1 (đang tàng hình). Và vì nó tàng hình, Selenium ngồi chờ nó hiện ra cho đến khi... Timeout!

**Cách Fix bằng `:nth-child`:**
```java
// ✅ ĐÚNG – Chỉ đích danh thẻ li thứ 4 trong danh sách (luôn luôn là Địa chỉ 1)
private static final By DELIVERY_ADDRESS = By.cssSelector("#address_delivery li:nth-child(4)");
```

---

## 🏃 Commands để chạy

### Chạy toàn bộ Test Giỏ hàng và Thanh toán (Phase 3)
```bash
# Chạy nhóm test Cart
mvn clean test -Dtest="CartTest"

# Chạy nhóm test Checkout
mvn clean test -Dtest="CheckoutTest"

# Chạy tất cả test liên quan đến Products
mvn clean test -Dtest="ProductsTest"
```

---

## ✅ Expected Results

Sau khi chạy `mvn clean test -Dtest="CheckoutTest"`:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.automationexercise.tests.checkout.CheckoutTest

TC-AE-014 START | email: ae.test.20260713214248@example.com
...
TC-AE-014 PASS | Order Placed Successfully
...
TC-AE-023 PASS | Addresses verified

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

---

## ⚠️ Lỗi thường gặp và cách xử lý

### 1. `ElementClickInterceptedException` (Vẫn xuất hiện)
**Nguyên nhân:** Quảng cáo mới xuất hiện hoặc AdHandler chưa bắt được.
**Giải pháp:** Đổi sang sử dụng `jsClick(locator)` trong Page Object cho cái nút bấm đang bị lỗi đó.

### 2. `TimeoutException: Expected condition failed: waiting for visibility of element`
**Nguyên nhân:** Có thể CSS Locator của bạn không còn đúng, hoặc nó vô tình trỏ vào một element đang bị ẩn (display: none).
**Giải pháp:** Mở trình duyệt, F12, kiểm tra lại DOM xem có bao nhiêu element bị trùng class. Sử dụng các kỹ thuật định vị chính xác hơn như `nth-child`, Xpath index `(//div)[1]`, hoặc Xpath text `//p[contains(.,'text')]`.

---

## ✅ Checklist Phase 3

Trước khi sang Phase tiếp theo, xác nhận tất cả:

- [ ] Lớp `AdHandler.java` đã được tích hợp và ẩn Iframe thành công.
- [ ] Các hành động điều hướng quan trọng trên `CartPage` và `CheckoutPage` sử dụng `jsClick()`.
- [ ] `CheckoutTest` tự động tải được file hóa đơn (`.txt`) ở cuối luồng thanh toán.
- [ ] Tất cả các test cases từ TC-11 đến TC-16 và TC-23, TC-24 đều PASS.

---

## ❓ Câu hỏi tự kiểm tra

1. Tại sao đôi khi dùng `driver.findElement(By...click())` thì lỗi, nhưng chạy qua Javascript Executor thì lại mượt mà?
2. Nếu trang web có 3 sản phẩm trùng class `.product-title`, làm sao để bạn chỉ click vào sản phẩm thứ 2?
3. Bạn sẽ làm gì nếu trang web xuất hiện quảng cáo nhưng không có id/class cố định mà đổi tên liên tục sau mỗi lần refresh?

---

## 🚀 Phase tiếp theo: Phase 4 – Authentication & Other Scenarios

Ở Phase cuối cùng này, chúng ta sẽ hoàn thành các test case còn sót lại:
- Đăng nhập/Đăng ký nâng cao
- Review sản phẩm
- Download Hóa đơn nâng cao
- ...

Hãy nói "Phase 4" khi sẵn sàng! 🎉
