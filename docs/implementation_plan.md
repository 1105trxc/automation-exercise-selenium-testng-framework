# Trạng Thái Sửa Automation Exercise Framework

Tài liệu này theo dõi việc triển khai
`HUONG_DAN_SUA_CODE_AUTOMATION_FRAMEWORK_HOAN_CHINH.md` và đối chiếu với
`automation-exercise-framework-blueprint.md`. Chỉ đánh dấu hoàn thành khi source và bằng chứng chạy thực tế cùng tồn tại.

## Trạng thái P0

| Hạng mục | Trạng thái | Kết quả hiện tại |
|---|---|---|
| P0-FIX-00 Baseline & guardrails | Hoàn thành | Có static gates, compile và suite evidence |
| P0-FIX-01 Add-to-cart | Hoàn thành | Native hover/click; modal thuộc component; loop thuộc `CartFlow` |
| P0-FIX-02 Ad handling | Hoàn thành | Retry chung chỉ sau proven interception; selector Google có phạm vi hẹp |
| P0-FIX-03 Account cleanup | Hoàn thành | Registry per-thread, response parse chính xác, cleanup report trung thực |
| P0-FIX-04 Sleep/download | Hoàn thành | Không executable sleep; download polling, stable size và UTF-8 |
| P0-FIX-05 Page transitions | Hoàn thành | Destination Page Object phản ánh trang thật |
| P0-FIX-06 Assertions | Hoàn thành | Exact cart identity/math, brand, scroll, order và invoice oracle |
| P0-FIX-07 Exceptions/dead code | Hoàn thành | Không broad swallowed catch, alias hoặc retry code đã xác nhận là thừa |
| P0-FIX-08 Config/browser | Hoàn thành | CLI precedence; wait/download/ad keys có caller; download đa browser |
| P0-FIX-09 Comments/docs | Hoàn thành | Source comments tập trung vào behavior; README và trạng thái đã đồng bộ |
| P0-FIX-10 Regression evidence | Đang chờ | Cần 3 regression liên tiếp trên cùng HEAD hiện tại |

## Kiến trúc hiện tại

```text
Tests
  -> UserFlow / CartFlow / CheckoutFlow
  -> Pages / HeaderComponent / FooterSubscriptionComponent / AddToCartModal
  -> BasePage / AEBasePage / DriverFactory / ConfigManager / utilities
```

Các Page Object trạng thái chuyên biệt đã tồn tại:

- `AccountCreatedPage`
- `AccountDeletedPage`
- `TestCasesPage`
- `ProductDetailPage`
- `PaymentSuccessPage`

Assertions và generated test data ở test layer. Flows không chứa TestNG assertion; Page Objects không điều phối toàn bộ business journey.

## Hardening đã hoàn tất

### Interaction reliability

- Không `Thread.sleep`.
- Không generic JavaScript click.
- Không retry loop hoặc TestNG retry analyzer.
- `AEBasePage.click()` chỉ retry một lần sau `ElementClickInterceptedException` do known Google ad.
- Add-to-cart đợi hover overlay mở hoàn toàn và click native visible control.
- `View Product` xác nhận URL đích và product identity trước khi trả `ProductDetailPage`.
- Nếu Google vignette đã được chứng minh là nuốt một GET navigation, link chỉ được tiếp tục một lần khi browser vẫn ở URL nguồn.

### Data and assertions

- Account cleanup dùng registry per test thread và báo danh sách email cleanup thất bại.
- Cart snapshot giữ product name, price, quantity và total qua login.
- Order success kiểm tra exact heading/message.
- Invoice phải tồn tại, non-empty và khớp customer cùng purchase total.
- Invalid login kiểm tra exact error và positive unauthenticated state (`Signup / Login` visible).

### Configuration and evidence

- `explicitWait` là wait mặc định duy nhất cho positive Page Object checks.
- `downloadTimeout`, `downloadDir` và `thirdPartyAdWait` có caller thực.
- Console lifecycle logs dùng ASCII để không bị mojibake.
- Failure listener ghi description, environment, browser, current URL, reason và screenshot path khi khả dụng.
- GitHub Actions chạy smoke trên push/PR và cho chọn regression khi chạy thủ công.

## Bằng chứng gần nhất

### Trên source hiện tại

| Verification | Kết quả |
|---|---|
| `mvn clean test-compile` với JDK 21 | PASS, 33 main + 14 test sources |
| Focused Products/Cart/Review | PASS 3/3 |
| Review stability probe | PASS 6/6, retry runner = 0 |
| Smoke suite sau config/wait refactor | PASS 11/11 |
| Invalid-login data rows | PASS 2/2 |
| Scroll + invoice focused suite | PASS 3/3; invoice 64 bytes |
| ASCII lifecycle logging probe | PASS 2/2; output đã quan sát rõ |

Nhánh phục hồi `View Product` sau vignette chưa xuất hiện tự nhiên trong các lần chạy mới. Vì vậy nhánh thường đã có evidence, còn nhánh vignette chỉ có compile/static review và phải tiếp tục được quan sát trong regression.

### Regression lịch sử

Commit `0217d88` từng có 2 regression liên tiếp PASS `27/27`. Run thứ ba bị dừng sau khi phát hiện lỗi `ReviewTest` còn ở Products page sau vignette. Lỗi đó dẫn đến commit `6dea1b2`, nên hai run cũ không được tính là bằng chứng cuối cho HEAD mới.

## Static quality gates

Các pattern sau hiện có 0 executable match trong `src`:

```text
Thread.sleep
generic JS click
catch (Exception)
ignored broad catch
querySelectorAll('div') ad sweep
Bootstrap modal force-hide
retryAnalyzer / IRetryAnalyzer
hard-coded Duration.ofSeconds(<number>) trong source/test
hard-coded isDisplayed(..., <seconds>) trong Page Objects
```

## Việc còn lại

1. Chạy regression suite 3 lần liên tiếp trên cùng commit sau khi mọi thay đổi đã commit.
2. Ghi exact commit SHA, thời gian, pass/fail/skip và artifact path cho từng run.
3. Quan sát log `Vignette consumed View Product navigation` nếu ad xuất hiện; không được giả lập rồi tuyên bố branch đã chạy thật.
4. Xác nhận workflow GitHub Actions trên remote sau khi push.
5. Theo dõi warning Chrome CDP mới hơn module Selenium gần nhất; hiện không gây test failure nhưng phải được ghi nhận.

## Chiến lược commit

Mỗi thay đổi được tách theo behavior để rollback độc lập:

```text
fix(...)       sửa hành vi hoặc false-PASS risk
refactor(...)  chuẩn hóa kiến trúc/config mà không đổi business intent
test(...)      tăng test oracle hoặc coverage
chore(...)     logging/docs/evidence
ci(...)        workflow automation
```

Không squash các nhóm không liên quan thành một commit duy nhất trong giai đoạn hardening.
