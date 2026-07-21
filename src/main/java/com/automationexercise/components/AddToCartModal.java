package com.automationexercise.components;

import com.automationexercise.pages.AEBasePage;
import com.automationexercise.pages.CartPage;
import com.automationexercise.pages.ProductsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AddToCartModal – Encapsulates the modal that appears after clicking "Add to cart".
 *
 * Owns all state management for the modal:
 *  - waitForModal(): wait until visible
 *  - clickContinueShopping(): close modal and wait until invisible
 *  - clickViewCart(): navigate to CartPage
 */
public class AddToCartModal extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(AddToCartModal.class);

    // -----------------------------------------------------------------
    // Locators
    // -----------------------------------------------------------------
    private static final By MODAL           = By.cssSelector("div.modal-content");
    private static final By CONTINUE_BTN    = By.cssSelector("button.close-modal");
    private static final By VIEW_CART_LINK  = By.cssSelector(".modal-body a[href='/view_cart']");

    public AddToCartModal(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the modal to become visible after clicking "Add to cart".
     *
     * @return this – fluent API for chaining clickContinueShopping() or clickViewCart()
     */
    public AddToCartModal waitForModal() {
        log.info("Waiting for Add to Cart modal to appear");
        waitUntilVisible(MODAL);
        return this;
    }

    /**
     * Clicks "Continue Shopping" and waits for the modal to fully disappear.
     *
     * WHY WAIT FOR INVISIBLE:
     * Without this wait, calling addSearchedProductAt() immediately after returns the
     * same product wrapper, and hover may fail while Bootstrap modal is still fading out.
     * Using ExpectedConditions.invisibilityOfElementLocated avoids Thread.sleep.
     *
     * @return ProductsPage – the page we remain on after closing the modal
     */
    public ProductsPage clickContinueShopping() {
        log.info("Clicking Continue Shopping in Add to Cart modal");
        click(CONTINUE_BTN);
        // Wait for modal to fully disappear before allowing next product interaction
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL));
        return new ProductsPage(driver);
    }

    /**
     * Clicks "View Cart" link. Navigates to Cart page.
     *
     * @return CartPage
     */
    public CartPage clickViewCart() {
        log.info("Clicking View Cart in Add to Cart modal");
        clickSideEffectFreeNavigationLink(VIEW_CART_LINK, "View Cart");
        return new CartPage(driver).waitUntilLoaded();
    }

}
