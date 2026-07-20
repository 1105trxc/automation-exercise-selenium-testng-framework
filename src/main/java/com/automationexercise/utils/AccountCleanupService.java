package com.automationexercise.utils;

import com.automationexercise.config.ConfigManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AccountCleanupService – Deletes test accounts via the automationexercise.com DELETE API.
 *
 * ISOLATION GUARANTEE:
 *   Each test thread has its own account registry via ThreadLocal.
 *   Thread A's cleanup never deletes accounts belonging to Thread B.
 *
 * USAGE PATTERN:
 *   Registration: call registerAccountForCleanup(email, password) BEFORE creating the account
 *   so that even a partially completed test cannot leave a leaked account.
 *
 *   Cleanup: call cleanupCurrentTestAccounts() in @AfterMethod (always runs).
 *
 *   If the test deletes the account via UI successfully: call unregisterAccount(email)
 *   so the API cleanup does not attempt a redundant call.
 *
 * ERROR HANDLING:
 *   - IOException: network failure, logged and counted as a failed cleanup.
 *   - InterruptedException: thread interrupt restored via Thread.currentThread().interrupt().
 *   - Cleanup failures are reported honestly in CleanupResult; they are never silently ignored.
 */
public final class AccountCleanupService {

    private static final Logger log = LoggerFactory.getLogger(AccountCleanupService.class);

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    /**
     * Per-thread account registry: email → password.
     * LinkedHashMap preserves registration order for predictable cleanup sequence.
     */
    private static final ThreadLocal<Map<String, String>> ACCOUNTS =
            ThreadLocal.withInitial(LinkedHashMap::new);

    private AccountCleanupService() {
        throw new UnsupportedOperationException("AccountCleanupService is a utility class.");
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Registers an account for cleanup in the current test thread's registry.
     * Should be called BEFORE clickCreateAccount() to handle partial failures.
     *
     * @param email    the account's email address
     * @param password the account's password
     */
    public static void registerAccountForCleanup(String email, String password) {
        ACCOUNTS.get().put(email, password);
        log.info("Registered account for cleanup: {}", email);
    }

    /**
     * Removes an account from the current test thread's cleanup registry.
     * Call this AFTER successfully deleting the account via UI, to avoid a
     * redundant API call at the end of the test.
     *
     * @param email the email of the account that was already deleted via UI
     */
    public static void unregisterAccount(String email) {
        ACCOUNTS.get().remove(email);
        log.info("Unregistered account from cleanup (deleted via UI): {}", email);
    }

    /**
     * Deletes all accounts registered by the current test thread via the DELETE API.
     * Always clears the ThreadLocal registry, even if some calls fail.
     *
     * @return CleanupResult containing how many were attempted and which emails failed
     */
    public static CleanupResult cleanupCurrentTestAccounts() {
        Map<String, String> accounts = new LinkedHashMap<>(ACCOUNTS.get());

        if (accounts.isEmpty()) {
            return new CleanupResult(0, List.of());
        }

        log.info("Cleanup: attempting to delete {} account(s) via API", accounts.size());
        List<String> failedEmails = new ArrayList<>();

        try {
            for (Map.Entry<String, String> entry : accounts.entrySet()) {
                boolean deleted = deleteAccountViaApi(entry.getKey(), entry.getValue());
                if (!deleted) {
                    failedEmails.add(entry.getKey());
                }
            }
        } finally {
            ACCOUNTS.remove(); // Always clear ThreadLocal to prevent cross-test leaks
        }

        return new CleanupResult(accounts.size(), failedEmails);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private static boolean deleteAccountViaApi(String email, String password) {
        String baseUrl = ConfigManager.get("baseUrl", "https://automationexercise.com");
        String deleteEndpoint = baseUrl + "/api/deleteAccount";

        String formData = "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deleteEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(formData))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString());

            log.debug("Cleanup API response for {}: [{}] {}", email, response.statusCode(), response.body());

            return isSuccessfulDeletion(response);

        } catch (IOException e) {
            log.warn("Cleanup API IOException for {}: {}", email, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            log.warn("Cleanup API interrupted for {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Parses the API response to determine if the account was successfully deleted.
     * The API returns JSON: {"responseCode": 200, "message": "Account deleted!"}
     * A string-contains check on "200" is insufficient and can produce false positives.
     */
    private static boolean isSuccessfulDeletion(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            log.warn("Cleanup API returned HTTP {}", response.statusCode());
            return false;
        }

        try {
            JsonNode json = JSON.readTree(response.body());
            int responseCode = json.path("responseCode").asInt(-1);
            String message = json.path("message").asText("");

            boolean success = responseCode == 200 && message.toLowerCase().contains("deleted");
            if (success) {
                log.info("Account deleted successfully via API.");
            } else {
                log.warn("API returned unexpected response: code={}, message='{}'", responseCode, message);
            }
            return success;

        } catch (IOException e) {
            log.warn("Failed to parse cleanup API response JSON: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // RESULT TYPE
    // =========================================================================

    /**
     * Immutable result of a cleanup operation.
     *
     * @param attempted   total number of accounts for which deletion was attempted
     * @param failedEmails list of emails for which deletion failed (empty means all succeeded)
     */
    public record CleanupResult(int attempted, List<String> failedEmails) {
        public boolean isFullySuccessful() {
            return failedEmails.isEmpty();
        }
    }
}
