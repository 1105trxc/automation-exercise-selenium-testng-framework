package com.automationexercise.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RandomDataUtils – Generates unique test data to ensure test independence.
 *
 * WHY UNIQUE DATA?
 * automationexercise.com stores registered accounts.
 * If two test runs use the same email → "Email already exists" error.
 * Solution: embed a timestamp in every generated email.
 *
 * EXAMPLE:
 *   generateUniqueEmail() → "ae.test.20260705183045@example.com"
 *   generateName()        → "TestUser183045"
 *
 * The timestamp makes each value unique AND traceable in logs.
 * You can look at a screenshot timestamp and find the exact test run.
 */
public final class RandomDataUtils {

    /** Pattern: yyyyMMddHHmmss (e.g., 20260705183045) */
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** Shorter pattern for names: HHmmss (e.g., 183045) */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HHmmss");

    // Utility class – no instantiation
    private RandomDataUtils() {
        throw new UnsupportedOperationException("RandomDataUtils is a utility class.");
    }

    /**
     * Generates a unique email address with a timestamp suffix.
     * Format: ae.test.{timestamp}@example.com
     *
     * @return Unique email string
     */
    public static String generateUniqueEmail() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 6);
        return "ae.test." + timestamp + "." + uuid + "@example.com";
    }

    /**
     * Generates a readable test user name with a time suffix.
     * Format: TestUser{HHmmss}
     *
     * @return Unique name string
     */
    public static String generateName() {
        String timeSuffix = LocalDateTime.now().format(TIME_FORMAT);
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 4);
        return "TestUser" + timeSuffix + uuid;
    }

    /**
     * Returns the current timestamp string.
     * Useful for screenshot file naming.
     *
     * @return Timestamp in yyyyMMddHHmmss format
     */
    public static String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
}
