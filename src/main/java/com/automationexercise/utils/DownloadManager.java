package com.automationexercise.utils;

import com.automationexercise.config.ConfigManager;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * DownloadManager – Manages file downloads for automated tests.
 *
 * DESIGN PRINCIPLES:
 *   - Download directory is configured via config, not hard-coded.
 *   - All waits use condition polling (FluentWait), not Thread.sleep.
 *   - File deletion failures throw an exception rather than being silently ignored.
 *   - File content is read with explicit UTF-8 encoding.
 *
 * BROWSER SUPPORT NOTE:
 *   Download directory preference is currently configured for Chrome only.
 *   TC-AE-024 (Download Invoice) is therefore Chrome-specific.
 *   Running this test on Firefox or Edge will require additional browser configuration.
 */
public final class DownloadManager {

    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);

    private DownloadManager() {
        throw new UnsupportedOperationException("DownloadManager is a utility class.");
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Returns the configured download directory as an absolute Path.
     *
     * Config key: downloadDir (default: target/downloads)
     * If the configured path is relative, it is resolved against the working directory.
     * This is the single source of truth — DriverFactory also calls this method.
     *
     * @return absolute Path to the download directory
     */
    public static Path getDownloadDirectory() {
        String configured = ConfigManager.get("downloadDir", "target/downloads");
        Path path = Paths.get(configured);

        return path.isAbsolute()
                ? path
                : Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
    }

    /**
     * Deletes all files in the download directory and recreates it.
     * Call this before tests that download files to prevent reading stale files.
     *
     * @throws IllegalStateException if the directory cannot be cleaned
     */
    public static void cleanDownloadDirectory() {
        Path dir = getDownloadDirectory();

        try {
            Files.createDirectories(dir);

            try (Stream<Path> paths = Files.list(dir)) {
                for (Path file : paths.toList()) {
                    Files.deleteIfExists(file);
                    log.debug("Deleted stale download file: {}", file.getFileName());
                }
            }

            log.info("Download directory cleaned: {}", dir);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not clean download directory: " + dir, e);
        }
    }

    /**
     * Waits for a file to appear in the download directory, be non-empty,
     * and have a stable size (no .crdownload partial file).
     *
     * HOW STABILITY IS DETECTED:
     *   - File must exist and have size > 0.
     *   - No corresponding .crdownload file present.
     *   - File size must be unchanged for 2 consecutive poll intervals.
     *
     * No Thread.sleep — uses FluentWait with 250ms polling.
     *
     * @param fileName the exact filename to wait for (e.g., "invoice.txt")
     * @param timeout  maximum time to wait
     * @return the Path to the fully downloaded file
     * @throws org.openqa.selenium.TimeoutException if download does not complete within timeout
     */
    public static Path waitForDownload(String fileName, Duration timeout) {
        Path directory = getDownloadDirectory();
        Path target = directory.resolve(fileName);
        Path partial = directory.resolve(fileName + ".crdownload");

        AtomicLong previousSize = new AtomicLong(-1);
        AtomicInteger stablePolls = new AtomicInteger(0);

        log.info("Waiting up to {} for download: {}", timeout, target);

        FluentWait<Path> waiter = new FluentWait<>(target)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(IOException.class);

        return waiter.until(path -> {
            if (!Files.exists(path) || Files.exists(partial)) {
                return null; // Not ready yet
            }

            long size;
            try {
                size = Files.size(path);
            } catch (IOException e) {
                return null;
            }

            if (size <= 0) {
                return null; // Empty file
            }

            if (size == previousSize.getAndSet(size)) {
                if (stablePolls.incrementAndGet() >= 2) {
                    log.info("File downloaded: {} ({} bytes)", path.getFileName(), size);
                    return path; // Size stable for 2 polls — download complete
                }
            } else {
                stablePolls.set(0); // Size changed — still writing
            }

            return null;
        });
    }

    /**
     * Reads the entire content of a file as a UTF-8 string.
     *
     * @param filePath the path to the file to read
     * @return the file content
     * @throws IllegalStateException if the file cannot be read
     */
    public static String readFileContent(Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read downloaded file: " + filePath, e);
        }
    }
}
