# Hướng dẫn tạo GitHub Repository và Push Code

## Bước 1: Tạo Repository trên GitHub

1. Mở trình duyệt → vào: **https://github.com/new**

2. Điền thông tin:
   - **Repository name:** `automation-exercise-selenium-testng-framework`
   - **Description:** `Selenium Java TestNG automation framework for automationexercise.com - Portfolio project`
   - **Visibility:** ✅ **Public** (để nhà tuyển dụng xem được)
   - ⚠️ **KHÔNG tick** "Add a README file" (chúng ta sẽ tự push)
   - ⚠️ **KHÔNG tick** "Add .gitignore" (đã có rồi)
   - ⚠️ **KHÔNG chọn** License

3. Click **"Create repository"**

---

## Bước 2: Push Code lên GitHub

Sau khi tạo repo xong, mở **PowerShell / Terminal** tại thư mục project:

```powershell
cd "d:\Project_Automation_Testing\automation-exercise-framework"
git push -u origin main
```

Nếu được hỏi credentials:
- Username: `1105trxc`
- Password: **Dùng Personal Access Token (PAT)**, KHÔNG phải mật khẩu GitHub

---

## Bước 3: Tạo Personal Access Token (nếu chưa có)

GitHub không cho phép dùng password thông thường qua HTTPS nữa.

1. Vào: **https://github.com/settings/tokens/new**
2. Note: `automation-framework-push`
3. Expiration: `90 days`
4. Scopes: tick ✅ **repo** (tất cả sub-options)
5. Click **"Generate token"**
6. **COPY token ngay** (chỉ hiển thị một lần!)
7. Dùng token này làm "Password" khi git hỏi

---

## Bước 4: Verify thành công

Sau khi push thành công:
```
Enumerating objects: 47, done.
...
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

Kiểm tra tại: **https://github.com/1105trxc/automation-exercise-selenium-testng-framework**

---

## Lưu ý về cấu hình git global (tùy chọn)

Cập nhật tên và email thật của bạn trong git config:
```bash
git config --global user.name "Tên của bạn"
git config --global user.email "email@example.com"
```

---

## Commit History đã tạo

```
a14180e docs: add test plan, RTM and Phase 0-1 learning walkthroughs
ed337e7 test: automate TC-AE-002, TC-AE-003, TC-AE-004 (authentication)  
27523e2 feat: add BaseTest with browser lifecycle management
5e9658f feat: add RandomDataUtils for unique test data generation
0a09421 feat: implement Page Objects for authentication flow
373b0fd feat: add BasePage with reusable Selenium actions
b11f259 feat: add WebDriver factory and browser configuration
dd4d393 chore: add configuration and logging setup
7c39a02 chore: initialize Maven Selenium TestNG project
```
