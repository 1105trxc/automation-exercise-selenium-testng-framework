package com.automationexercise.driver;

/**
 * BrowserType – Enum of supported browsers.
 *
 * WHY AN ENUM?
 * Instead of passing raw strings like "chrome" everywhere (typo-prone),
 * we use an enum. The compiler catches invalid browser names at build time.
 *
 * USAGE:
 *   BrowserType browser = BrowserType.fromString("chrome"); // → CHROME
 *   DriverFactory.initDriver(BrowserType.CHROME);
 */
public enum BrowserType {

    CHROME,
    FIREFOX,
    EDGE;

    /**
     * Converts a string (from config/CLI) to the corresponding BrowserType.
     * Case-insensitive: "Chrome", "CHROME", "chrome" all work.
     *
     * @param browser Browser name string
     * @return Matching BrowserType
     * @throws IllegalArgumentException if browser name is not recognized
     */
    public static BrowserType fromString(String browser) {
        return switch (browser.toLowerCase().trim()) {
            case "chrome"  -> CHROME;
            case "firefox" -> FIREFOX;
            case "edge"    -> EDGE;
            default -> throw new IllegalArgumentException(
                "Unsupported browser: '" + browser + "'. Valid options: chrome, firefox, edge"
            );
        };
    }
}
