package com.automationexercise.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager – Single source of truth for all configuration.
 *
 * CONFIGURATION PRIORITY (highest to lowest):
 *   1. System Property    → -Dbrowser=firefox (CLI override)
 *   2. Properties File    → local.properties / staging.properties
 *   3. Default Value      → hardcoded fallback in getXxx() methods
 *
 * WHICH FILE IS LOADED?
 * Determined by the -Denv system property:
 *   mvn test              → loads config/local.properties
 *   mvn test -Denv=staging → loads config/staging.properties
 *
 * USAGE:
 *   ConfigManager.get("browser", "chrome")
 *   ConfigManager.getBoolean("headless", false)
 *   ConfigManager.getInt("explicitWait", 15)
 */
public final class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();

    // Static initializer: loads the properties file once when class is loaded
    static {
        // Read -Denv system property; default to "local"
        String env = System.getProperty("env", "local");
        String configFile = "config/" + env + ".properties";

        try (InputStream inputStream = ConfigManager.class
                .getClassLoader()
                .getResourceAsStream(configFile)) {

            if (inputStream == null) {
                log.warn("Config file not found: '{}'. All values will use defaults.", configFile);
            } else {
                properties.load(inputStream);
                log.info("Configuration loaded from: {}", configFile);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + configFile, e);
        }
    }

    // Utility class – no instantiation
    private ConfigManager() {
        throw new UnsupportedOperationException("ConfigManager is a utility class.");
    }

    /**
     * Gets a config value. System property overrides the properties file.
     *
     * @param key Config key (e.g., "browser")
     * @return Value or null if not found anywhere
     */
    public static String get(String key) {
        // System property has highest priority (CLI -Dkey=value)
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return properties.getProperty(key);
    }

    /**
     * Gets a config value with a fallback default.
     *
     * @param key          Config key
     * @param defaultValue Fallback if key not found
     * @return Config value or default
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    /**
     * Gets a boolean config value.
     *
     * @param key          Config key (e.g., "headless")
     * @param defaultValue Fallback boolean
     * @return Boolean value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Gets an integer config value.
     *
     * @param key          Config key (e.g., "explicitWait")
     * @param defaultValue Fallback integer
     * @return Integer value
     */
    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value != null && !value.isBlank()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid integer for key '{}': '{}'. Using default: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
}
