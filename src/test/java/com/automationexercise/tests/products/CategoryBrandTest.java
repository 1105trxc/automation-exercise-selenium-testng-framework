package com.automationexercise.tests.products;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.ProductsPage;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * CategoryBrandTest – Automated tests for TC-AE-018 và TC-AE-019.
 *
 * TC-AE-018: View Category Products
 *   Home page → verify categories visible → click Women → click first subcategory (Tops/Dress)
 *   → verify category page heading → click Men → click first subcategory → verify heading.
 *
 * TC-AE-019: View & Cart Brand Products
 *   Products page → verify brands sidebar → click first brand → verify brand page
 *   → click second brand → verify brand page.
 *
 * NOTE VỀ TC-018 HEADING:
 * Test case gốc ghi "WOMEN - TOPS PRODUCTS" nhưng step 5 click "Dress".
 * Actual website: click Women → click first subcategory (Dress) → heading là
 * "WOMEN - DRESS PRODUCTS". Chúng ta verify heading is NOT empty (dynamic check)
 * thay vì hardcode expected text, để tránh breaking nếu site thay đổi categories.
 */
@Listeners(TestListener.class)
@Epic("Products")
@Feature("Category and Brand Navigation")
public class CategoryBrandTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CategoryBrandTest.class);

    // =====================================================================
    // TC-AE-018: View Category Products
    // =====================================================================

    @Test(
        description = "TC-AE-018 - View Category Products",
        groups = {"regression", "products", "positive"}
    )
    @Story("Category Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Verifies categories are visible on home page left sidebar. " +
        "Navigates through Women subcategory and Men subcategory. " +
        "Verifies respective category product pages are displayed."
    )
    public void viewCategoryProducts() {
        log.info("TC-AE-018 START");

        // Step 3: Verify categories are visible on left side bar
        // Categories sidebar is visible on home page
        HomePage homePage = new HomePage(driver());
        Assert.assertTrue(homePage.isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Navigate to products page where category sidebar is clearly visible
        ProductsPage productsPage = homePage.getHeader().clickProducts();

        Assert.assertTrue(productsPage.isCategoryVisible(),
                "FAIL: Category sidebar should be visible");

        // Step 4-5: Click on 'Women' category → click subcategory link (Dress/Tops)
        productsPage.clickWomenCategory();
        productsPage.clickFirstWomenSubcategory();

        // Step 6: Verify category page is displayed with heading
        String womenCategoryHeading = productsPage.getCategoryPageHeading();
        log.info("TC-AE-018 | Women subcategory heading: '{}'", womenCategoryHeading);
        Assert.assertEquals(womenCategoryHeading.toUpperCase().replaceAll("\\s+", " "), "WOMEN - DRESS PRODUCTS",
                "FAIL: Heading should be exactly 'WOMEN - DRESS PRODUCTS' but was: " + womenCategoryHeading);

        // Step 7: On left side bar, click on any sub-category link of 'Men' category
        productsPage.clickMenCategory();
        productsPage.clickFirstMenSubcategory();
        // Step 8: Verify that user is navigated to that category page
        String menCategoryHeading = productsPage.getCategoryPageHeading();
        log.info("TC-AE-018 | Men subcategory heading: '{}'", menCategoryHeading);
        Assert.assertEquals(menCategoryHeading.toUpperCase().replaceAll("\\s+", " "), "MEN - TSHIRTS PRODUCTS",
                "FAIL: Heading should be exactly 'MEN - TSHIRTS PRODUCTS' but was: " + menCategoryHeading);

        log.info("TC-AE-018 PASS | Successfully navigated through Categories");
    }

    // =====================================================================
    // TC-AE-019: View & Cart Brand Products
    // =====================================================================

    @Test(
        description = "TC-AE-019 - View & Cart Brand Products",
        groups = {"regression", "products", "positive"}
    )
    @Story("Brand Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Navigates to Products page, verifies Brands are visible on left sidebar. " +
        "Clicks first brand and verifies brand page is displayed. " +
        "Clicks second brand and verifies navigation to second brand page."
    )
    public void viewBrandProducts() {
        log.info("TC-AE-019 START");

        // Step 3: Click on 'Products' button
        HomePage homePage = new HomePage(driver());
        ProductsPage productsPage = homePage.getHeader().clickProducts();

        // Step 4: Verify Brands are visible on left side bar
        Assert.assertTrue(productsPage.isBrandSectionVisible(),
                "FAIL: Brands sidebar should be visible on Products page");

        // Step 5: Click on any brand name (first brand)
        productsPage.clickFirstBrand();

        // Step 5: Verify that user is navigated to brand page and brand products are displayed
        String firstBrandHeading = productsPage.getCategoryPageHeading();
        log.info("TC-AE-019 | First brand heading: '{}'", firstBrandHeading);
        Assert.assertEquals(firstBrandHeading.toUpperCase().replaceAll("\\s+", " "), "BRAND - POLO PRODUCTS",
                "FAIL: Heading should be exactly 'BRAND - POLO PRODUCTS' but was: " + firstBrandHeading);
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: Products should be visible for Polo brand");

        // Step 6: On left side bar, click on any other brand link
        productsPage.clickSecondBrand();
        String secondBrandHeading = productsPage.getCategoryPageHeading();
        log.info("TC-AE-019 | Second brand heading: '{}'", secondBrandHeading);
        Assert.assertFalse(secondBrandHeading.isEmpty(),
                "FAIL: Second brand page heading should not be empty");
        Assert.assertTrue(productsPage.hasProducts(),
                "FAIL: Second brand page should display products");

        log.info("TC-AE-019 PASS | Brand1: '{}' | Brand2: '{}'",
                firstBrandHeading, secondBrandHeading);
    }
}
