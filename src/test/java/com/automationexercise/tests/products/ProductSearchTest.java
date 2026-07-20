package com.automationexercise.tests.products;

import com.automationexercise.base.BaseTest;
import com.automationexercise.flows.CartFlow;
import com.automationexercise.flows.UserFlow;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.AccountCreatedPage;
import com.automationexercise.pages.AccountDeletedPage;
import com.automationexercise.pages.CartPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.LoginPage;
import com.automationexercise.pages.ProductsPage;
import com.automationexercise.utils.AccountCleanupService;
import com.automationexercise.utils.JsonDataReader;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

/**
 * ProductSearchTest – TC-AE-009 and TC-AE-020.
 *
 * TC-AE-009: Search Product
 * TC-AE-020: Search Products and Verify Cart After Login
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

        ProductsPage productsPage = homePage.getHeader().clickProducts();
        Assert.assertTrue(productsPage.isAllProductsVisible(),
                "FAIL: 'All Products' heading should be visible");

        productsPage.searchFor(SEARCH_KEYWORD);

        Assert.assertTrue(productsPage.isSearchedProductsVisible(),
                "FAIL: 'Searched Products' heading should be visible after search");
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: At least one search result should be visible for keyword: " + SEARCH_KEYWORD);

        List<String> productNames = productsPage.getVisibleProductNames();
        for (String name : productNames) {
            Assert.assertTrue(name.toLowerCase(Locale.ROOT)
                            .contains(SEARCH_KEYWORD.toLowerCase(Locale.ROOT)),
                    "FAIL: Product name '" + name + "' does not contain keyword '" + SEARCH_KEYWORD + "'");
        }

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
        "Searches for products, adds them to cart via CartFlow, verifies cart. " +
        "Then logs in and verifies cart identity is preserved (session persistence)."
    )
    public void searchAndVerifyCartAfterLogin() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        final String TEST_PASSWORD = "Automation@2026";
        log.info("TC-AE-020 START | Keyword: '{}'", SEARCH_KEYWORD);

        // ARRANGE: Register user for the login step later
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        user.setPassword(TEST_PASSWORD);

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(), "FAIL: Home page should be visible");

        AccountCreatedPage accountCreatedPage = new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user);
        Assert.assertTrue(accountCreatedPage.isAccountCreatedVisible(),
                "FAIL: Account should be created");
        homePage = accountCreatedPage.clickContinue();

        // Logout to simulate guest state before search
        LoginPage loginPage = homePage.getHeader().clickLogout();

        // ACT: Search as guest, add to cart via CartFlow
        ProductsPage productsPage = loginPage.getHeader().clickProducts();
        Assert.assertTrue(productsPage.isAllProductsVisible(),
                "FAIL: All Products page should be visible");

        productsPage.searchFor(SEARCH_KEYWORD);
        Assert.assertTrue(productsPage.isSearchedProductsVisible(),
                "FAIL: 'Searched Products' heading should be visible");
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: Search results should not be empty");

        // CartFlow orchestrates adding all searched products without retry/JS/sleep
        CartPage cartPage = new CartFlow(driver()).addAllVisibleSearchedProducts(productsPage);

        Assert.assertFalse(cartPage.isCartEmpty(),
                "FAIL: Cart should have products after adding searched items");

        // Snapshot before login for exact identity comparison
        List<CartPage.CartItemSnapshot> itemsBeforeLogin = cartPage.getCartItems();
        log.info("TC-AE-020 | Cart has {} item(s) before login", itemsBeforeLogin.size());

        // Login with the registered account
        homePage = new UserFlow(driver()).loginSuccessfully(uniqueEmail, TEST_PASSWORD);
        cartPage = homePage.getHeader().clickCart();

        // Verify cart identity preserved (not just count)
        List<CartPage.CartItemSnapshot> itemsAfterLogin = cartPage.getCartItems();
        Assert.assertEquals(itemsAfterLogin, itemsBeforeLogin,
                "FAIL: Cart items must be preserved exactly after login");

        log.info("TC-AE-020 PASS | Cart identity preserved ({} item(s))", itemsAfterLogin.size());

        // CLEANUP: Delete account via UI, then unregister from API cleanup
        AccountDeletedPage deletedPage = cartPage.getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(),
                "FAIL: Account should be deleted");
        AccountCleanupService.unregisterAccount(uniqueEmail);
        log.info("TC-AE-020 CLEANUP done");
    }
}
