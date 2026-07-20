package com.automationexercise.tests.checkout;

import com.automationexercise.base.BaseTest;
import com.automationexercise.flows.CartFlow;
import com.automationexercise.flows.CheckoutFlow;
import com.automationexercise.flows.UserFlow;
import com.automationexercise.models.PaymentData;
import com.automationexercise.models.UserData;
import com.automationexercise.pages.AccountCreatedPage;
import com.automationexercise.pages.AccountDeletedPage;
import com.automationexercise.pages.CartPage;
import com.automationexercise.pages.CheckoutPage;
import com.automationexercise.pages.HomePage;
import com.automationexercise.pages.PaymentSuccessPage;
import com.automationexercise.utils.AccountCleanupService;
import com.automationexercise.utils.JsonDataReader;
import com.automationexercise.utils.RandomDataUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckoutTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CheckoutTest.class);

    private boolean isOnCheckoutPage() {
        return driver().getCurrentUrl().contains("/checkout");
    }

    @Test(
        description = "TC-AE-014 - Place Order: Register while Checkout",
        groups = {"smoke", "regression", "checkout", "positive"}
    )
    @Story("Register while Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Guest user adds product to cart, proceeds to checkout. Registers via modal, pays, verifies order placed.")
    public void placeOrderRegisterWhileCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();

        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(), "FAIL: Home page should be visible");

        CartPage cartPage = new CartFlow(driver()).addFirstProductAndGoToCart();
        Assert.assertTrue(cartPage.isCartPageVisible(), "FAIL: Cart page should be visible");

        cartPage.clickProceedToCheckoutGuest();
        cartPage.clickRegisterLoginInModal();

        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        AccountCreatedPage accountCreatedPage =
                new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user);

        HomePage homePage = accountCreatedPage.clickContinue();
        cartPage = homePage.getHeader().clickCart();
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckoutLoggedIn();
        Assert.assertTrue(isOnCheckoutPage(), "FAIL: Should be on checkout page");

        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        PaymentSuccessPage successPage = new CheckoutFlow().completePayment(checkoutPage, payment);
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(), "FAIL: Order not placed");

        homePage = successPage.clickContinue();
        AccountDeletedPage deletedPage = homePage.getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(), "FAIL: Account should be deleted");
        AccountCleanupService.unregisterAccount(uniqueEmail);
    }

    @Test(
        description = "TC-AE-015 - Place Order: Register before Checkout",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Register before Checkout")
    @Severity(SeverityLevel.CRITICAL)
    public void placeOrderRegisterBeforeCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);

        new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user).clickContinue();

        CartPage cartPage = new CartFlow(driver()).addFirstProductAndGoToCart();
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckoutLoggedIn();

        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        PaymentSuccessPage successPage = new CheckoutFlow().completePayment(checkoutPage, payment);
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(), "FAIL: Order not placed");

        AccountDeletedPage deletedPage = successPage.clickContinue().getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(), "FAIL: Account should be deleted");
        AccountCleanupService.unregisterAccount(uniqueEmail);
    }

    @Test(
        description = "TC-AE-016 - Place Order: Login before Checkout",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Login before Checkout")
    public void placeOrderLoginBeforeCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);

        new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user)
                .clickContinue()
                .getHeader()
                .clickLogout();

        new UserFlow(driver()).loginSuccessfully(uniqueEmail, user.getPassword());

        CartPage cartPage = new CartFlow(driver()).addFirstProductAndGoToCart();
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckoutLoggedIn();

        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        PaymentSuccessPage successPage = new CheckoutFlow().completePayment(checkoutPage, payment);
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(), "FAIL: Order not placed");

        AccountDeletedPage deletedPage = successPage.clickContinue().getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(), "FAIL: Account should be deleted");
        AccountCleanupService.unregisterAccount(uniqueEmail);
    }

    @Test(
        description = "TC-AE-023 - Verify address details in checkout page",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Address Verification")
    public void verifyAddressDetailsInCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);

        new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user).clickContinue();

        CartPage cartPage = new CartFlow(driver()).addFirstProductAndGoToCart();
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckoutLoggedIn();

        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible());
        Assert.assertTrue(checkoutPage.isBillingAddressVisible());

        AccountDeletedPage deletedPage = new HomePage(driver()).getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(), "FAIL: Account should be deleted");
        AccountCleanupService.unregisterAccount(uniqueEmail);
    }

    @Test(
        description = "TC-AE-024 - Download Invoice after purchase order",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Download Invoice")
    public void downloadInvoiceAfterOrder() {
        com.automationexercise.utils.DownloadManager.cleanDownloadDirectory();
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();

        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(), "FAIL: Home page should be visible");

        CartPage cartPage = new CartFlow(driver()).addFirstProductAndGoToCart();
        cartPage.clickProceedToCheckoutGuest();
        cartPage.clickRegisterLoginInModal();

        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        AccountCreatedPage accountCreatedPage =
                new UserFlow(driver()).registerNewUser(uniqueName, uniqueEmail, user);

        HomePage homePage = accountCreatedPage.clickContinue();
        cartPage = homePage.getHeader().clickCart();
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckoutLoggedIn();

        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        PaymentSuccessPage successPage = new CheckoutFlow().completePayment(checkoutPage, payment);

        Assert.assertTrue(successPage.isDownloadInvoiceVisible(), "FAIL: 'Download Invoice' button should be visible");
        successPage.clickDownloadInvoice();

        java.nio.file.Path invoiceFile = com.automationexercise.utils.DownloadManager.waitForDownload(
                "invoice.txt", java.time.Duration.ofSeconds(15));
        Assert.assertTrue(java.nio.file.Files.exists(invoiceFile),
                "FAIL: invoice.txt should exist in download directory");
        assertDownloadedFileIsNotEmpty(invoiceFile);

        String invoiceContent = com.automationexercise.utils.DownloadManager.readFileContent(invoiceFile);
        Assert.assertTrue(invoiceContent.contains("Hi " + user.getFirstName() + " " + user.getLastName()),
                "FAIL: Invoice should contain the user's name");

        homePage = successPage.clickContinue();
        AccountDeletedPage deletedPage = homePage.getHeader().clickDeleteAccount();
        Assert.assertTrue(deletedPage.isAccountDeletedVisible(), "FAIL: Account should be deleted");
        AccountCleanupService.unregisterAccount(uniqueEmail);
    }

    private void assertDownloadedFileIsNotEmpty(java.nio.file.Path invoiceFile) {
        try {
            Assert.assertTrue(java.nio.file.Files.size(invoiceFile) > 0,
                    "FAIL: invoice.txt should not be empty");
        } catch (java.io.IOException e) {
            throw new AssertionError("FAIL: Could not read invoice file size: " + invoiceFile, e);
        }
    }
}
