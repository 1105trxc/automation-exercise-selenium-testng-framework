package com.automationexercise.constants;

/**
 * RouteConstants – URL paths for each page of automationexercise.com.
 *
 * WHY THIS CLASS EXISTS:
 * Centralizes all URL paths. If a URL changes, update it here once.
 * Tests use these to navigate or verify the current URL.
 *
 * USAGE:
 *   driver.get(ConfigManager.get("baseUrl") + RouteConstants.LOGIN);
 *   waitForUrlContains(RouteConstants.CART);
 */
public final class RouteConstants {

    public static final String HOME              = "/";
    public static final String LOGIN             = "/login";
    public static final String SIGNUP            = "/signup";
    public static final String PRODUCTS          = "/products";
    public static final String CART              = "/view_cart";
    public static final String CHECKOUT          = "/checkout";
    public static final String PAYMENT           = "/payment";
    public static final String PAYMENT_DONE      = "/payment_done";
    public static final String CONTACT_US        = "/contact_us";
    public static final String TEST_CASES        = "/test_cases";
    public static final String DELETE_ACCOUNT    = "/delete_account";
    public static final String ACCOUNT_CREATED   = "/account_created";
    public static final String ACCOUNT_DELETED   = "/delete_account";

    private RouteConstants() {
        throw new UnsupportedOperationException("RouteConstants is a utility class.");
    }
}
