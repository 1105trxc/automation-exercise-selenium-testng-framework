package com.automationexercise.tests.products;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.*;
import com.automationexercise.utils.JsonDataReader;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * ProductSearchTest – Automated tests for TC-AE-009, TC-AE-020.
 *
 * TC-AE-009: Search Product
 *   Navigate to Products page, search for a keyword, verify results.
 *
 * TC-AE-020: Search Products and Verify Cart After Login
 *   Search → add products to cart → verify cart → login → verify cart preserved.
 *   KEY INSIGHT: automationexercise.com giữ cart session sau khi login.
 */
@Listeners(TestListener.class)
@Epic("Products")
@Feature("Product Search")
public class ProductSearchTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchTest.class);

    private static final String SEARCH_KEYWORD = "Blue Top";

    // =====================================================================
    // TC-AE-009: Search Product
    // =====================================================================

    @Test(
        description = "TC-AE-009 - Search Product",
        groups = {"smoke", "regression", "products", "positive"}
    )
    @Story("Search")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Navigates to Products page, searches for a keyword. " +
        "Verifies 'SEARCHED PRODUCTS' heading and search results are visible."
    )
    public void searchProductByKeyword() {
        log.info("TC-AE-009 START | Keyword: '{}'", SEARCH_KEYWORD);

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(), "FAIL: Home page should be visible");

        ProductsPage productsPage = homePage.clickProducts();
        Assert.assertTrue(productsPage.isAllProductsVisible(),
                "FAIL: 'All Products' heading should be visible");

        productsPage.searchFor(SEARCH_KEYWORD);

        Assert.assertTrue(productsPage.isSearchedProductsVisible(),
                "FAIL: 'Searched Products' heading should be visible after search");
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: At least one search result should be visible for keyword: " + SEARCH_KEYWORD);

        log.info("TC-AE-009 PASS | Found {} result(s)", productsPage.getSearchedProductCount());
    }

    // =====================================================================
    // TC-AE-020: Search Products and Verify Cart After Login
    // =====================================================================

    @Test(
        description = "TC-AE-020 - Search Products and Verify Cart After Login",
        groups = {"regression", "products", "positive"}
    )
    @Story("Search")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Searches for products, adds them to cart, verifies cart. " +
        "Then logs in and verifies cart still contains the products (session persistence)."
    )
    public void searchAndVerifyCartAfterLogin() {
        // ── ARRANGE: Register user first (needed for login step) ──────────
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        final String TEST_PASSWORD = "Automation@2026";
        log.info("TC-AE-020 START | Keyword: '{}'", SEARCH_KEYWORD);

        // Register + auto-logged in
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(), "FAIL: Home page should be visible");

        SignupPage signupPage = homePage.clickLoginSignup()
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();
        signupPage.fillRegistrationForm(user);
        signupPage.clickCreateAccount();
        Assert.assertTrue(signupPage.isAccountCreatedMessageVisible(),
                "FAIL: Account should be created");
        homePage = signupPage.clickContinue();

        // Logout to simulate guest state before search
        homePage.clickLogout();

        // ── ACT: Search as guest, add to cart ─────────────────────────────
        // Step 3: Click Products
        ProductsPage productsPage = new HomePage(driver()).clickProducts();
        Assert.assertTrue(productsPage.isAllProductsVisible(),
                "FAIL: All Products page should be visible");

        // Steps 5-7: Search and verify results
        productsPage.searchFor(SEARCH_KEYWORD);
        Assert.assertTrue(productsPage.isSearchedProductsVisible(),
                "FAIL: 'Searched Products' heading should be visible");
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: Search results should not be empty");

        // Step 8: Add searched products to cart
        CartPage cartPage = productsPage.addAllSearchedProductsToCart();

        // Step 9: Verify products in cart
        Assert.assertFalse(cartPage.isCartEmpty(),
                "FAIL: Cart should have products after adding searched items");
        int countBeforeLogin = cartPage.getProductCount();
        log.info("TC-AE-020 | Cart has {} item(s) before login", countBeforeLogin);

        // Step 10: Click Signup/Login and submit login details
        // Since guest, modal appears when proceeding to checkout → navigate to login page
        new HomePage(driver()).clickLoginSignup();
        LoginPage loginPage = new LoginPage(driver());
        loginPage.enterLoginEmail(uniqueEmail)
                 .enterLoginPassword(TEST_PASSWORD)
                 .clickLoginButton();

        // Step 11: Go to Cart page again
        cartPage = new HomePage(driver()).clickCart();

        // Step 12: Verify products still in cart
        Assert.assertFalse(cartPage.isCartEmpty(),
                "FAIL: Cart should still have products after login");
        Assert.assertEquals(cartPage.getProductCount(), countBeforeLogin,
                "FAIL: Cart product count should be preserved after login");

        log.info("TC-AE-020 PASS | Cart preserved {} item(s) after login", cartPage.getProductCount());

        // ── CLEANUP ───────────────────────────────────────────────────────
        homePage = new HomePage(driver());
        homePage.clickDeleteAccount();
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: Account should be deleted");
        log.info("TC-AE-020 CLEANUP done");
    }
}
