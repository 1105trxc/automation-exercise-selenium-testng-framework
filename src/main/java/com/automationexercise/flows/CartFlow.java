package com.automationexercise.flows;

import com.automationexercise.components.AddToCartModal;
import com.automationexercise.components.HeaderComponent;
import com.automationexercise.pages.CartPage;
import com.automationexercise.pages.ProductsPage;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CartFlow – Orchestrates multi-step workflows for adding products to cart.
 */
public class CartFlow {

    private static final Logger log = LoggerFactory.getLogger(CartFlow.class);

    private final WebDriver driver;

    public CartFlow(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Navigates to the Products page, adds the first product to the cart,
     * and navigates to the Cart page.
     *
     * @return CartPage for assertions.
     */
    public CartPage addFirstProductAndGoToCart() {
        ProductsPage productsPage = new HeaderComponent(driver).clickProducts();
        return productsPage
                .hoverAndAddFirstProductToCart()
                .getAddToCartModal()
                .waitForModal()
                .clickViewCart();
    }

    /**
     * Adds all currently visible searched products to cart one by one,
     * then navigates to the Cart page.
     *
     * @param productsPage the ProductsPage instance already showing search results
     * @return CartPage after all products have been added
     * @throws IllegalStateException if no searched products are visible
     */
    public CartPage addAllVisibleSearchedProducts(ProductsPage productsPage) {
        int productCount = productsPage.getSearchedProductCount();

        if (productCount == 0) {
            throw new IllegalStateException("No searched products available to add to cart.");
        }

        log.info("CartFlow: adding {} searched product(s) to cart", productCount);

        for (int index = 1; index <= productCount; index++) {
            AddToCartModal modal = productsPage.addSearchedProductAt(index);

            if (index < productCount) {
                // Not the last product: close modal and stay on products page
                modal.clickContinueShopping();
                log.info("CartFlow: product {}/{} added, continuing shopping", index, productCount);
            } else {
                // Last product: navigate to cart
                log.info("CartFlow: product {}/{} added, navigating to cart", index, productCount);
                return modal.clickViewCart();
            }
        }

        // Unreachable in normal flow; present for compiler satisfaction
        throw new IllegalStateException("CartFlow terminated unexpectedly without reaching CartPage.");
    }
}
