package com.automationexercise.tests.checkout;

import com.automationexercise.base.BaseTest;
import com.automationexercise.listeners.TestListener;
import com.automationexercise.models.PaymentData;
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
 * CheckoutTest – Automated tests for TC-AE-014, 015, 016, 023, 024.
 *
 * TC-AE-014: Place Order: Register while Checkout
 *   Guest adds product → Cart → Checkout modal → Register → return to Cart → Checkout → Pay → Delete
 *
 * TC-AE-015: Place Order: Register before Checkout
 *   Register → add product → Cart → Checkout → Pay → Delete
 *
 * TC-AE-016: Place Order: Login before Checkout
 *   Register + Login → add product → Cart → Checkout → Pay → Delete
 *
 * TC-AE-023: Verify address details in checkout page
 *   Register (save address data) → add product → Cart → Checkout → verify address → Delete
 *
 * TC-AE-024: Download Invoice after purchase order
 *   Same as TC-014 flow → verify success → click Download Invoice → continue → Delete
 *
 * ARCHITECTURE NOTE on TC-014 / TC-024:
 * When guest clicks "Proceed To Checkout", website shows a modal with "Register/Login" link.
 * CartPage.clickProceedToCheckout() returns CheckoutPage – but if NOT logged in, the
 * modal overlay appears instead. We detect this by checking the current URL:
 *   - URL contains "/checkout" → went straight to checkout (logged in)
 *   - URL still "/view_cart" → modal appeared (guest user)
 * Then we call CartPage.clickRegisterLoginInModal() explicitly.
 */
@Listeners(TestListener.class)
@Epic("Checkout")
@Feature("Order Placement")
public class CheckoutTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CheckoutTest.class);

    // -----------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------

    /**
     * Registers a new user and returns to home page as logged-in user.
     * Uses UserData from JSON (profile) + runtime-generated unique email.
     */
    private UserData registerNewUser(String uniqueEmail, String uniqueName) {
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);

        SignupPage signupPage = new HomePage(driver()).clickLoginSignup()
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();

        signupPage.fillRegistrationForm(user);
        signupPage.clickCreateAccount();
        Assert.assertTrue(signupPage.isAccountCreatedMessageVisible(),
                "HELPER FAIL: Account should be created");
        signupPage.clickContinue();

        log.info("HELPER | User registered: {}", uniqueEmail);
        return user;
    }

    /**
     * Adds the first product to cart via ProductsPage hover, returns to CartPage.
     */
    private CartPage addProductAndGoToCart() {
        ProductsPage productsPage = new HomePage(driver()).clickProducts();
        productsPage.hoverAndAddFirstProductToCart();
        CartPage cartPage = productsPage.clickViewCartFromModal();
        Assert.assertTrue(cartPage.isCartPageVisible(), "HELPER FAIL: Cart page should be visible");
        Assert.assertFalse(cartPage.isCartEmpty(), "HELPER FAIL: Cart should have product");
        return cartPage;
    }

    /**
     * Completes payment using PaymentData from checkout.json.
     * @param checkoutPage the CheckoutPage already navigated to
     * @return PaymentSuccessPage after payment confirmed
     */
    private PaymentSuccessPage completePayment(CheckoutPage checkoutPage) {
        PaymentData payment = JsonDataReader.readFirst("checkout.json", "paymentData", PaymentData.class);
        return checkoutPage
                .enterOrderComment(payment.getOrderComment())
                .clickPlaceOrder()
                .fillAndConfirm(
                        payment.getNameOnCard(),
                        payment.getCardNumber(),
                        payment.getCvc(),
                        payment.getExpiryMonth(),
                        payment.getExpiryYear()
                );
    }

    /**
     * Checks if current page is the checkout page.
     * Used in TC-014/024 to detect if modal appeared (guest) vs direct checkout (logged in).
     */
    private boolean isOnCheckoutPage() {
        return driver().getCurrentUrl().contains("/checkout");
    }

    // =====================================================================
    // TC-AE-014: Place Order: Register while Checkout
    // =====================================================================

    @Test(
        description = "TC-AE-014 - Place Order: Register while Checkout",
        groups = {"smoke", "regression", "checkout", "positive"}
    )
    @Story("Register while Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Guest user adds product to cart, proceeds to checkout. " +
        "Website shows Register/Login modal. User registers via modal, " +
        "returns to cart, proceeds to checkout, pays, verifies order placed, " +
        "then deletes account."
    )
    public void placeOrderRegisterWhileCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-014 START | email: {}", uniqueEmail);

        // Step 3: Verify home page
        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Steps 4-6: Add product → Cart → Verify cart
        CartPage cartPage = addProductAndGoToCart();

        // Step 7: Click Proceed To Checkout
        cartPage.clickProceedToCheckout();

        // Step 8: If modal appeared (guest user) → click 'Register / Login'
        if (!isOnCheckoutPage()) {
            new CartPage(driver()).clickRegisterLoginInModal();
        }

        // Step 9: Fill all details in Signup and create account
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        SignupPage signupPage = new LoginPage(driver())
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();
        signupPage.fillRegistrationForm(user);
        signupPage.clickCreateAccount();

        // Step 10: Verify 'ACCOUNT CREATED!' and click 'Continue'
        Assert.assertTrue(signupPage.isAccountCreatedMessageVisible(),
                "FAIL: 'ACCOUNT CREATED!' should be visible");
        HomePage homePage = signupPage.clickContinue();

        // Step 11: Verify 'Logged in as username'
        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in after registration");

        // Step 12: Click 'Cart' button
        cartPage = homePage.clickCart();
        Assert.assertTrue(cartPage.isCartPageVisible(),
                "FAIL: Cart page should be visible");

        // Step 13: Click 'Proceed To Checkout' button
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckout();
        Assert.assertTrue(isOnCheckoutPage(),
                "FAIL: Should be on checkout page after login");

        // Step 14: Verify Address Details and Review Your Order
        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible(),
                "FAIL: Delivery address should be visible");
        Assert.assertTrue(checkoutPage.isBillingAddressVisible(),
                "FAIL: Billing address should be visible");

        // Steps 15-17: Comment → Place Order → Pay
        PaymentSuccessPage successPage = completePayment(checkoutPage);

        // Step 18: Verify success message
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(),
                "FAIL: 'Order placed successfully' message should be visible");

        // Steps 19-20: Delete Account + verify
        homePage = successPage.clickContinue();
        homePage.clickDeleteAccount();
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: 'ACCOUNT DELETED!' should be visible");

        log.info("TC-AE-014 PASS");
    }

    // =====================================================================
    // TC-AE-015: Place Order: Register before Checkout
    // =====================================================================

    @Test(
        description = "TC-AE-015 - Place Order: Register before Checkout",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Register before Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Registers a new account first, then adds product to cart, " +
        "proceeds to checkout, completes payment, " +
        "verifies order placed, then deletes account."
    )
    public void placeOrderRegisterBeforeCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-015 START | email: {}", uniqueEmail);

        // Step 3: Verify home page
        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(),
                "FAIL: Home page should be visible");

        // Steps 4-6: Register user
        registerNewUser(uniqueEmail, uniqueName);

        // Step 7: Verify 'Logged in as username'
        Assert.assertTrue(new HomePage(driver()).isUserLoggedIn(),
                "FAIL: User should be logged in after registration");

        // Step 8: Add products to cart
        // Steps 9-10: Cart → verify
        CartPage cartPage = addProductAndGoToCart();

        // Step 11: Click Proceed To Checkout
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckout();

        // Step 12: Verify Address Details and Review Your Order
        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible(),
                "FAIL: Delivery address should be visible");

        // Steps 13-15: Comment → Place Order → Pay
        PaymentSuccessPage successPage = completePayment(checkoutPage);

        // Step 16: Verify success message
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(),
                "FAIL: Order placed message should be visible");

        // Steps 17-18: Delete account
        HomePage homePage = successPage.clickContinue();
        homePage.clickDeleteAccount();
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: 'ACCOUNT DELETED!' should be visible");

        log.info("TC-AE-015 PASS");
    }

    // =====================================================================
    // TC-AE-016: Place Order: Login before Checkout
    // =====================================================================

    @Test(
        description = "TC-AE-016 - Place Order: Login before Checkout",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Login before Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Logs in to an existing account, adds product to cart, " +
        "proceeds to checkout, completes payment, " +
        "verifies order placed, then deletes account."
    )
    public void placeOrderLoginBeforeCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        final String TEST_PASSWORD = "Automation@2026";
        log.info("TC-AE-016 START | email: {}", uniqueEmail);

        // Pre-condition: Create account to login with
        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(),
                "FAIL: Home page should be visible");

        registerNewUser(uniqueEmail, uniqueName);

        // Logout to be in guest state
        LoginPage loginPage = new HomePage(driver()).clickLogout();

        // Steps 4-5: Signup/Login → Fill credentials → Login button
        HomePage homePage = loginPage
                .enterLoginEmail(uniqueEmail)
                .enterLoginPassword(TEST_PASSWORD)
                .clickLoginButton();

        // Step 6: Verify 'Logged in as username'
        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in");

        // Step 7: Add products to cart
        // Steps 8-9: Cart → verify
        CartPage cartPage = addProductAndGoToCart();

        // Step 10: Click Proceed To Checkout
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckout();

        // Step 11: Verify Address Details
        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible(),
                "FAIL: Delivery address should be visible");

        // Steps 12-14: Comment → Place Order → Pay
        PaymentSuccessPage successPage = completePayment(checkoutPage);

        // Step 15: Verify success message
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(),
                "FAIL: Order placed message should be visible");

        // Steps 16-17: Delete account
        homePage = successPage.clickContinue();
        homePage.clickDeleteAccount();
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: 'ACCOUNT DELETED!' should be visible");

        log.info("TC-AE-016 PASS");
    }

    // =====================================================================
    // TC-AE-023: Verify address details in checkout page
    // =====================================================================

    @Test(
        description = "TC-AE-023 - Verify address details in checkout page",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Address Verification")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Registers a new account (saves address data), adds product to cart, " +
        "proceeds to checkout, and verifies that the delivery and billing addresses " +
        "match the address filled during registration."
    )
    public void verifyAddressDetailsInCheckout() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-023 START | email: {}", uniqueEmail);

        // Steps 3-6: Register user, save UserData for address comparison
        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(),
                "FAIL: Home page should be visible");

        UserData user = registerNewUser(uniqueEmail, uniqueName);

        // Step 7: Verify 'Logged in as username'
        Assert.assertTrue(new HomePage(driver()).isUserLoggedIn(),
                "FAIL: User should be logged in");

        // Step 8: Add product to cart
        // Steps 9-10: Cart → verify
        CartPage cartPage = addProductAndGoToCart();

        // Step 11: Proceed To Checkout
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckout();

        // Step 12: Verify delivery address matches registration data
        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible(),
                "FAIL: Delivery address section should be visible");

        String deliveryName    = checkoutPage.getDeliveryFullName();
        String deliveryStreet  = checkoutPage.getDeliveryStreetAddress();
        String deliveryCountry = checkoutPage.getDeliveryCountry();

        log.info("TC-AE-023 | Delivery: name='{}', street='{}', country='{}'",
                deliveryName, deliveryStreet, deliveryCountry);

        Assert.assertFalse(deliveryName.isEmpty(),
                "FAIL: Delivery full name should not be empty");
        Assert.assertTrue(deliveryStreet.contains(user.getAddress1()),
                "FAIL: Delivery street '" + deliveryStreet
                        + "' should contain: '" + user.getAddress1() + "'");
        Assert.assertTrue(deliveryCountry.equalsIgnoreCase(user.getCountry()),
                "FAIL: Delivery country should be '" + user.getCountry()
                        + "' but was '" + deliveryCountry + "'");

        // Step 13: Verify billing address matches registration data
        Assert.assertTrue(checkoutPage.isBillingAddressVisible(),
                "FAIL: Billing address section should be visible");

        String billingName = checkoutPage.getBillingFullName();
        Assert.assertFalse(billingName.isEmpty(),
                "FAIL: Billing full name should not be empty");

        log.info("TC-AE-023 PASS | Addresses verified");

        // Steps 14-15: Delete account
        new HomePage(driver()).clickDeleteAccount();
        Assert.assertTrue(new HomePage(driver()).isAccountDeletedMessageVisible(),
                "FAIL: Account should be deleted");

        log.info("TC-AE-023 CLEANUP: Account deleted");
    }

    // =====================================================================
    // TC-AE-024: Download Invoice after purchase order
    // =====================================================================

    @Test(
        description = "TC-AE-024 - Download Invoice after purchase order",
        groups = {"regression", "checkout", "positive"}
    )
    @Story("Download Invoice")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Completes full order flow (add to cart → register while checkout → pay). " +
        "Verifies order placed, clicks 'Download Invoice' button, " +
        "continues and verifies still logged in, then deletes account."
    )
    public void downloadInvoiceAfterOrder() {
        String uniqueEmail = RandomDataUtils.generateUniqueEmail();
        String uniqueName  = RandomDataUtils.generateName();
        log.info("TC-AE-024 START | email: {}", uniqueEmail);

        // Steps 3-6: Home page + add product + cart
        Assert.assertTrue(new HomePage(driver()).isHomePageVisible(),
                "FAIL: Home page should be visible");

        CartPage cartPage = addProductAndGoToCart();

        // Step 7: Proceed To Checkout (will show modal for guest)
        cartPage.clickProceedToCheckout();

        // Step 8: Click Register/Login in modal
        if (!isOnCheckoutPage()) {
            new CartPage(driver()).clickRegisterLoginInModal();
        }

        // Step 9: Register new account
        UserData user = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
        SignupPage signupPage = new LoginPage(driver())
                .enterSignupName(uniqueName)
                .enterSignupEmail(uniqueEmail)
                .clickSignupButton();
        signupPage.fillRegistrationForm(user);
        signupPage.clickCreateAccount();

        // Step 10: Verify Account Created + Continue
        Assert.assertTrue(signupPage.isAccountCreatedMessageVisible(),
                "FAIL: 'ACCOUNT CREATED!' should be visible");
        HomePage homePage = signupPage.clickContinue();

        // Step 11: Verify Logged in
        Assert.assertTrue(homePage.isUserLoggedIn(),
                "FAIL: User should be logged in");

        // Steps 12-13: Cart → Proceed To Checkout
        cartPage = homePage.clickCart();
        Assert.assertTrue(cartPage.isCartPageVisible(),
                "FAIL: Cart page should be visible");
        CheckoutPage checkoutPage = cartPage.clickProceedToCheckout();

        // Step 14: Verify Address Details
        Assert.assertTrue(checkoutPage.isDeliveryAddressVisible(),
                "FAIL: Delivery address should be visible");

        // Steps 15-17: Comment → Place Order → Pay
        PaymentSuccessPage successPage = completePayment(checkoutPage);

        // Step 18: Verify success
        Assert.assertTrue(successPage.isOrderPlacedSuccessfully(),
                "FAIL: Order placed message should be visible");

        // Step 19: Click 'Download Invoice' button
        Assert.assertTrue(successPage.isDownloadInvoiceVisible(),
                "FAIL: 'Download Invoice' button should be visible");
        successPage.clickDownloadInvoice();
        log.info("TC-AE-024 | Download Invoice clicked successfully");

        // Step 20: Click Continue
        homePage = successPage.clickContinue();

        // Steps 21-22: Delete account + verify
        homePage.clickDeleteAccount();
        Assert.assertTrue(homePage.isAccountDeletedMessageVisible(),
                "FAIL: 'ACCOUNT DELETED!' should be visible");

        log.info("TC-AE-024 PASS");
    }
}
