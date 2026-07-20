package com.automationexercise.tests.products;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.ProductDetailPage;
import com.automationexercise.pages.ProductsPage;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * ReviewTest – Automated test for TC-AE-021.
 *
 * TC-AE-021: Add Review on Product
 *   Navigate to Products page → click View Product → verify "Write Your Review"
 *   → enter name, email, review text → Submit → verify success message.
 */
@Listeners(TestListener.class)
@Epic("Products")
@Feature("Product Review")
public class ReviewTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ReviewTest.class);

    @Test(
        description = "TC-AE-021 - Add Review on Product",
        groups = {"regression", "products", "positive"}
    )
    @Story("Product Review")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Navigates to the first product's detail page, " +
        "verifies the 'Write Your Review' section is visible, " +
        "submits a review with name/email/text, " +
        "and verifies the success message 'Thank you for your review.'"
    )
    public void addReviewOnProduct() {
        log.info("TC-AE-021 START");

        HomePage homePage = new HomePage(driver());
        ProductsPage productsPage = homePage.getHeader().clickProducts();

        Assert.assertTrue(productsPage.isAllProductsVisible(),
                "FAIL: All Products page should be visible");

        ProductDetailPage detailPage = productsPage.clickFirstProductViewProduct();

        Assert.assertTrue(detailPage.isWriteReviewVisible(),
                "FAIL: 'Write Your Review' section should be visible on product detail page");

        String reviewerName  = "Automation Tester";
        String reviewerEmail = RandomDataUtils.generateUniqueEmail();
        String reviewText    = "This is an automated test review. Product quality is excellent.";

        detailPage
                .enterReviewName(reviewerName)
                .enterReviewEmail(reviewerEmail)
                .enterReviewText(reviewText);

        detailPage.clickSubmitReview();

        Assert.assertTrue(detailPage.isReviewSuccessVisible(),
                "FAIL: Success message 'Thank you for your review.' should be visible after submission");

        log.info("TC-AE-021 PASS | Review submitted by '{}'", reviewerName);
    }
}
