package com.automationexercise.tests.cart;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.pages.CartPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.ProductDetailPage;
import com.automationexercise.pages.ProductsPage;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * CartTest – Automated tests for TC-AE-012, TC-AE-013, TC-AE-017, TC-AE-022.
 *
 * TC-AE-012: Add Products in Cart
 *   Hover product 1 → Add to cart → Continue Shopping
 *   Hover product 2 → Add to cart → View Cart
 *   Verify both products in cart with prices, quantities, totals.
 *
 * TC-AE-013: Verify Product quantity in Cart
 *   View product detail → set quantity to 4 → Add to cart → View Cart
 *   Verify quantity in cart = 4.
 *
 * TC-AE-017: Remove Products From Cart
 *   Add product → go to cart → click X → verify cart empty.
 *
 * TC-AE-022: Add to Cart from Recommended Items
 *   Scroll to Recommended Items → Add to Cart → View Cart → verify.
 */
@Listeners(TestListener.class)
@Epic("Cart")
@Feature("Cart Management")
public class CartTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CartTest.class);

    // =====================================================================
    // TC-AE-012: Add Products in Cart
    // =====================================================================

    @Test(
        description = "TC-AE-012 - Add Products in Cart",
        groups = {"smoke", "regression", "cart", "positive"}
    )
    @Story("Add Products to Cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Hovers over first product and adds to cart, clicks Continue Shopping. " +
        "Hovers over second product and adds to cart, clicks View Cart. " +
        "Verifies both products appear in cart with correct prices, quantities, and totals."
    )
    public void addTwoProductsAndVerifyCart() {
        log.info("TC-AE-012 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Click 'Products' button
        ProductsPage productsPage = homePage.getHeader().clickProducts();

        // Step 5: Hover over first product and click 'Add to cart'
        productsPage.hoverAndAddFirstProductToCart();

        // Step 6: Click 'Continue Shopping' button
        productsPage.getAddToCartModal().waitForModal().clickContinueShopping();

        // Step 7: Hover over second product and click 'Add to cart'
        productsPage.hoverAndAddSecondProductToCart();

        // Step 8: Click 'View Cart' button
        CartPage cartPage = productsPage.getAddToCartModal().waitForModal().clickViewCart();

        // Step 9: Verify both products are added to Cart
        Assert.assertTrue(cartPage.isCartPageVisible(),
                "FAIL: Cart page should be visible");
        Assert.assertEquals(cartPage.getProductCount(), 2,
                "FAIL: Cart should contain exactly 2 products");

        // Step 10: Verify prices, quantities and totals
        String price1  = cartPage.getProductPriceAtRow(1);
        String qty1    = cartPage.getProductQuantityAtRow(1);
        String total1  = cartPage.getProductTotalAtRow(1);
        String price2  = cartPage.getProductPriceAtRow(2);
        String qty2    = cartPage.getProductQuantityAtRow(2);
        String total2  = cartPage.getProductTotalAtRow(2);

        Assert.assertNotNull(price1,  "FAIL: Product 1 price should not be null");
        Assert.assertNotNull(total1,  "FAIL: Product 1 total should not be null");
        Assert.assertEquals(qty1, "1", "FAIL: Product 1 quantity should be 1");
        Assert.assertNotNull(price2,  "FAIL: Product 2 price should not be null");
        Assert.assertNotNull(total2,  "FAIL: Product 2 total should not be null");
        Assert.assertEquals(qty2, "1", "FAIL: Product 2 quantity should be 1");

        // Numeric verification: Price * Qty == Total
        int p1 = extractPrice(price1);
        int q1 = Integer.parseInt(qty1);
        int t1 = extractPrice(total1);
        Assert.assertEquals(p1 * q1, t1, "FAIL: Product 1 Total does not match Price * Quantity");

        int p2 = extractPrice(price2);
        int q2 = Integer.parseInt(qty2);
        int t2 = extractPrice(total2);
        Assert.assertEquals(p2 * q2, t2, "FAIL: Product 2 Total does not match Price * Quantity");

        log.info("TC-AE-012 PASS | Product1: price={} total={} | Product2: price={} total={}",
                price1, total1, price2, total2);
    }

    private int extractPrice(String rawPriceText) {
        // Example: "Rs. 500" -> 500
        String clean = rawPriceText.replaceAll("[^0-9]", "");
        return Integer.parseInt(clean);
    }

    // =====================================================================
    // TC-AE-013: Verify Product quantity in Cart
    // =====================================================================

    @Test(
        description = "TC-AE-013 - Verify Product quantity in Cart",
        groups = {"regression", "cart", "positive"}
    )
    @Story("Cart Quantity")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Views product detail page, sets quantity to 4, adds to cart, " +
        "navigates to cart and verifies the product appears with quantity = 4."
    )
    public void verifyProductQuantityInCart() {
        log.info("TC-AE-013 START");

        int expectedQuantity = 4;

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Click 'View Product' for any product on home page
        // Dùng Products page để click View Product đầu tiên
        ProductsPage productsPage = homePage.getHeader().clickProducts();
        ProductDetailPage detailPage = productsPage.clickFirstProductViewProduct();

        // Step 5: Verify product detail is opened
        Assert.assertTrue(detailPage.isProductDetailVisible(),
                "FAIL: Product detail page should be visible");

        // Step 6: Increase quantity to 4
        detailPage.setQuantity(expectedQuantity);

        // Step 7: Click 'Add to cart' button
        detailPage.clickAddToCart();

        // Step 8: Click 'View Cart' button
        CartPage cartPage = detailPage.getAddToCartModal().waitForModal().clickViewCart();

        // Step 9: Verify product is displayed in cart with exact quantity
        Assert.assertTrue(cartPage.isCartPageVisible(),
                "FAIL: Cart page should be visible");
        Assert.assertFalse(cartPage.isCartEmpty(),
                "FAIL: Cart should not be empty");

        String actualQty = cartPage.getProductQuantityAtRow(1);
        Assert.assertEquals(actualQty, String.valueOf(expectedQuantity),
                "FAIL: Cart quantity should be " + expectedQuantity + " but was " + actualQty);

        log.info("TC-AE-013 PASS | Quantity in cart: {}", actualQty);
    }

    // =====================================================================
    // TC-AE-017: Remove Products From Cart
    // =====================================================================

    @Test(
        description = "TC-AE-017 - Remove Products From Cart",
        groups = {"regression", "cart", "positive"}
    )
    @Story("Remove from Cart")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Adds a product to cart, navigates to cart page, " +
        "removes the product by clicking X button, " +
        "and verifies the cart is now empty."
    )
    public void removeProductFromCart() {
        log.info("TC-AE-017 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 4: Add product to cart (via ProductsPage)
        ProductsPage productsPage = homePage.getHeader().clickProducts();
        productsPage.hoverAndAddFirstProductToCart();

        // Step 5: Click 'Cart' button (via View Cart from modal)
        CartPage cartPage = productsPage.getAddToCartModal().waitForModal().clickViewCart();

        // Step 6: Verify cart page is displayed
        Assert.assertTrue(cartPage.isCartPageVisible(),
                "FAIL: Cart page should be visible");
        Assert.assertEquals(cartPage.getProductCount(), 1,
                "FAIL: Cart should have 1 product before removal");

        // Step 7: Click 'X' button for the product
        cartPage.removeProductAtRow(1);

        // Step 8: Verify product is removed from cart
        // Wait briefly for DOM update after removal
        cartPage.waitForCartEmpty();
        Assert.assertTrue(cartPage.isCartEmpty(),
                "FAIL: Cart should be empty after removing the product");

        log.info("TC-AE-017 PASS | Product successfully removed from cart");
    }

    // =====================================================================
    // TC-AE-022: Add to Cart from Recommended Items
    // =====================================================================

    @Test(
        description = "TC-AE-022 - Add to Cart from Recommended Items",
        groups = {"regression", "cart", "positive"}
    )
    @Story("Recommended Items")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Scrolls to bottom of home page, verifies 'RECOMMENDED ITEMS' section, " +
        "adds first recommended product to cart, clicks View Cart, " +
        "and verifies the product appears in cart."
    )
    public void addRecommendedItemToCart() {
        log.info("TC-AE-022 START");

        // Step 3: Verify home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Step 3: Scroll to bottom of page
        homePage.goToBottom();

        // Step 4: Verify 'RECOMMENDED ITEMS' are visible
        Assert.assertTrue(homePage.isRecommendedItemsVisible(),
                "FAIL: 'RECOMMENDED ITEMS' section should be visible after scrolling down");

        // Step 5: Click on 'Add To Cart' on Recommended product
        homePage.clickAddFirstRecommendedToCart();

        // Step 6: Click on 'View Cart' button
        CartPage cartPage = homePage.getAddToCartModal().waitForModal().clickViewCart();

        // Step 7: Verify product is displayed in cart page
        Assert.assertTrue(cartPage.isCartPageVisible(),
                "FAIL: Cart page should be visible");
        Assert.assertFalse(cartPage.isCartEmpty(),
                "FAIL: Cart should have the recommended product");

        log.info("TC-AE-022 PASS | Recommended product found in cart");
    }
}
