package com.automationexercise.constants;

/**
 * FrameworkConstants – Centralized constants for the automation framework.
 *
 * WHY THIS CLASS EXISTS:
 * Avoid magic numbers scattered in code. If timeout needs to change,
 * update it here once instead of hunting through every file.
 *
 * RULE: This class must NEVER be instantiated (private constructor).
 */
public final class FrameworkConstants {

    // -------------------------------------------------
    // Timeouts (seconds)
    // -------------------------------------------------


    /** Maximum time to wait for a page to load */
    public static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;

    /** Short wait for quick visibility checks */
    public static final int SHORT_WAIT_SECONDS = 5;

    // -------------------------------------------------
    // File Paths
    // -------------------------------------------------

    /** Directory where screenshots are saved on test failure */
    public static final String SCREENSHOT_DIR = "reports/screenshots/";

    /** Directory for log files */
    public static final String LOG_DIR = "reports/logs/";

    /** Directory for Allure raw results */
    public static final String ALLURE_RESULTS_DIR = "target/allure-results/";

    /** Base directory for test data JSON files */
    public static final String TEST_DATA_DIR = "src/test/resources/testdata/";

    // -------------------------------------------------
    // Private constructor – prevent instantiation
    // -------------------------------------------------
    private FrameworkConstants() {
        throw new UnsupportedOperationException("FrameworkConstants is a utility class.");
    }
}
