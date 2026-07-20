package com.automationexercise.tests.products;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.ProductDetailPage;
import com.automationexercise.pages.ProductsPage;
import com.automationexercise.pages.TestCasesPage;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * ProductsTest – Automated tests for TC-AE-007 and TC-AE-008.
 *
 * TC-AE-007: Verify Test Cases Page
 *   Very simple test – just verify navigation to /test_cases works.
 *
 * TC-AE-008: Verify All Products and product detail page
 *   1. Click Products in nav
 *   2. Verify "ALL PRODUCTS" page is visible
 *   3. Click "View Product" for first product
 *   4. Verify product detail page has: Name, Category, Price, Availability, Condition, Brand
 */
@Listeners(TestListener.class)
@Epic("Products")
@Feature("Products Browsing")
public class ProductsTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ProductsTest.class);

    // TC-AE-007: Verify Test Cases Page

    @Test(
        description = "TC-AE-007 - Verify Test Cases Page",
        groups = {"smoke", "regression", "products", "positive"}
    )
    @Story("Navigation")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Clicks the 'Test Cases' link in the nav bar. " +
        "Verifies user is navigated to the test_cases page successfully."
    )
    public void navigateToTestCasesPageSuccessfully() {
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible at start of test");
        log.info("TC-AE-007 START");

        TestCasesPage testCasesPage = homePage.getHeader().clickTestCases();
        Assert.assertTrue(testCasesPage.isTestCasesPageVisible(),
                "FAIL: Test Cases heading should be visible after navigation");

        log.info("TC-AE-007 PASS | Navigated to Test Cases page: {}", driver().getCurrentUrl());
    }

    // TC-AE-008: Verify All Products and product detail page

    @Test(
        description = "TC-AE-008 - Verify All Products and product detail page",
        groups = {"smoke", "regression", "products", "positive"}
    )
    @Story("Products")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Navigates to the Products page and verifies ALL PRODUCTS heading. " +
        "Clicks 'View Product' for the first product. " +
        "Verifies product detail page shows: Name, Category, Price, Availability, Condition, Brand."
    )
    public void verifyAllProductsAndProductDetailPage() {
        log.info("TC-AE-008 START");

        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        ProductsPage productsPage = homePage.getHeader().clickProducts();

        Assert.assertTrue(productsPage.isAllProductsVisible(),
                "FAIL: 'ALL PRODUCTS' heading should be visible on Products page");
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: At least one product with 'View Product' link should exist");
        log.info("TC-AE-008 | All Products page verified");

        ProductDetailPage detailPage = productsPage.clickFirstProductViewProduct();

        log.info("TC-AE-008 | Verifying product detail page elements");

        Assert.assertTrue(detailPage.isProductDetailVisible(),
                "FAIL: Product name should be visible on the detail page");

        Assert.assertTrue(detailPage.isCategoryVisible(),
                "FAIL: Product Category should be visible on the detail page");

        Assert.assertTrue(detailPage.isPriceVisible(),
                "FAIL: Product Price should be visible on the detail page");

        Assert.assertTrue(detailPage.isAvailabilityVisible(),
                "FAIL: Product Availability should be visible on the detail page");

        Assert.assertTrue(detailPage.isConditionVisible(),
                "FAIL: Product Condition should be visible on the detail page");

        Assert.assertTrue(detailPage.isBrandVisible(),
                "FAIL: Product Brand should be visible on the detail page");

        // Log what we found for traceability
        log.info("TC-AE-008 PASS | Product detail verified:");
        log.info("   Name:         {}", detailPage.getProductName());
        log.info("   Category:     {}", detailPage.getCategory());
        log.info("   Price:        {}", detailPage.getPrice());
        log.info("   Availability: {}", detailPage.getAvailability());
        log.info("   Condition:    {}", detailPage.getCondition());
        log.info("   Brand:        {}", detailPage.getBrand());
    }

}
