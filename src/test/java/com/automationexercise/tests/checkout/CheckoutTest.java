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
    private static final String EXPECTED_ORDER_PLACED_HEADING = "ORDER PLACED!";
    private static final String EXPECTED_ORDER_SUCCESS_MESSAGE =
            "Congratulations! Your order has been confirmed!";

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
        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible(),
                "FAIL: Delivery address should be visible on checkout page");

        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        PaymentSuccessPage successPage = new CheckoutFlow().completePayment(checkoutPage, payment);
        assertOrderPlaced(successPage);

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
        assertOrderPlaced(successPage);

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
        assertOrderPlaced(successPage);

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

        CheckoutPage.AddressSnapshot expectedAddress = expectedAddress(user);
        Assert.assertEquals(checkoutPage.getDeliveryAddress(), expectedAddress,
                "FAIL: Delivery address should exactly match registration data");
        Assert.assertEquals(checkoutPage.getBillingAddress(), expectedAddress,
                "FAIL: Billing address should exactly match registration data");

        AccountDeletedPage deletedPage = checkoutPage.getHeader().clickDeleteAccount();
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
        String expectedInvoiceTotal = cartPage.getCartItems().getFirst().total();
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckoutLoggedIn();

        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        PaymentSuccessPage successPage = new CheckoutFlow().completePayment(checkoutPage, payment);

        assertOrderPlaced(successPage);
        Assert.assertTrue(successPage.isDownloadInvoiceVisible(), "FAIL: 'Download Invoice' button should be visible");
        successPage.clickDownloadInvoice();

        java.nio.file.Path invoiceFile = com.automationexercise.utils.DownloadManager.waitForDownload(
                "invoice.txt", java.time.Duration.ofSeconds(15));
        Assert.assertTrue(java.nio.file.Files.exists(invoiceFile),
                "FAIL: invoice.txt should exist in download directory");
        assertDownloadedFileIsNotEmpty(invoiceFile);

        String invoiceContent = com.automationexercise.utils.DownloadManager.readFileContent(invoiceFile);
        String expectedInvoiceContent = "Hi %s %s, Your total purchase amount is %s. Thank you".formatted(
                user.getFirstName(),
                user.getLastName(),
                expectedInvoiceTotal.replace("Rs.", "").trim()
        );
        Assert.assertEquals(invoiceContent.strip(), expectedInvoiceContent,
                "FAIL: Invoice should exactly identify the customer and purchased amount");

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

    private void assertOrderPlaced(PaymentSuccessPage successPage) {
        Assert.assertEquals(successPage.getOrderPlacedHeading(), EXPECTED_ORDER_PLACED_HEADING,
                "FAIL: Order confirmation heading should identify the completed order state");
        Assert.assertEquals(successPage.getOrderSuccessMessage(), EXPECTED_ORDER_SUCCESS_MESSAGE,
                "FAIL: Order success message should match the required business outcome");
    }

    private CheckoutPage.AddressSnapshot expectedAddress(UserData user) {
        return new CheckoutPage.AddressSnapshot(
                user.getTitle() + ". " + user.getFirstName() + " " + user.getLastName(),
                user.getCompany(),
                user.getAddress1(),
                user.getAddress2(),
                user.getCity() + " " + user.getState() + " " + user.getZipcode(),
                user.getCountry(),
                user.getMobile()
        );
    }
}
