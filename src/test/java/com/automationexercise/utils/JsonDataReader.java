package com.automationexercise.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * JsonDataReader – Utility để đọc file JSON và ánh xạ thành Java objects.
 *
 * HOW IT WORKS:
 * 1. Nhận tên file JSON (không cần path, tự tìm trong /testdata/)
 * 2. Nhận key của array hoặc object trong JSON
 * 3. Dùng Jackson ObjectMapper để deserialize thành kiểu T
 *
 * EXAMPLE:
 * Given users.json:
 * {
 *   "invalidLoginUsers": [
 *     {"email": "a@b.com", "password": "wrong", "expectedError": "..."}
 *   ]
 * }
 *
 * Usage:
 *   List<LoginData> list = JsonDataReader.readList("users.json", "invalidLoginUsers", LoginData.class);
 *   LoginData first = JsonDataReader.readFirst("users.json", "validUsers", UserData.class);
 *
 * WHY IN src/test/java?
 * JsonDataReader phụ thuộc vào test resources (testdata/*.json).
 * Test resources chỉ available trên test classpath, không phải main classpath.
 * → Đặt class này ở test scope là đúng.
 */
public final class JsonDataReader {

    private static final Logger log = LoggerFactory.getLogger(JsonDataReader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TEST_DATA_PATH = "testdata/";

    private JsonDataReader() {
        throw new UnsupportedOperationException("JsonDataReader is a utility class.");
    }

    /**
     * Reads a JSON array under the given key and deserializes it into a List<T>.
     *
     * @param jsonFileName  File name (e.g., "users.json")
     * @param arrayKey      Key of the JSON array (e.g., "invalidLoginUsers")
     * @param clazz         Target class (e.g., LoginData.class)
     * @param <T>           Generic type
     * @return List of deserialized objects
     */
    public static <T> List<T> readList(String jsonFileName, String arrayKey, Class<T> clazz) {
        try (InputStream is = openStream(jsonFileName)) {
            JsonNode root = MAPPER.readTree(is);
            JsonNode arrayNode = root.get(arrayKey);

            if (arrayNode == null || !arrayNode.isArray()) {
                throw new RuntimeException("Key '" + arrayKey + "' not found or not an array in " + jsonFileName);
            }

            List<T> result = MAPPER.readValue(
                arrayNode.toString(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, clazz)
            );

            log.debug("Loaded {} items from {}.{}", result.size(), jsonFileName, arrayKey);
            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON list from: " + jsonFileName + " [" + arrayKey + "]", e);
        }
    }

    /**
     * Reads the FIRST object from a JSON array under the given key.
     * Use this when you need a single "template" object from the data file.
     *
     * @param jsonFileName  File name (e.g., "users.json")
     * @param arrayKey      Key of the JSON array (e.g., "validUsers")
     * @param clazz         Target class (e.g., UserData.class)
     * @param <T>           Generic type
     * @return First object in the array
     */
    public static <T> T readFirst(String jsonFileName, String arrayKey, Class<T> clazz) {
        List<T> list = readList(jsonFileName, arrayKey, clazz);
        if (list.isEmpty()) {
            throw new RuntimeException("Array '" + arrayKey + "' in " + jsonFileName + " is empty.");
        }
        return list.get(0);
    }

    /**
     * Reads a single JSON object (not in an array) under the given key.
     *
     * @param jsonFileName  File name
     * @param objectKey     Key of the JSON object
     * @param clazz         Target class
     * @param <T>           Generic type
     * @return Deserialized object
     */
    public static <T> T readObject(String jsonFileName, String objectKey, Class<T> clazz) {
        try (InputStream is = openStream(jsonFileName)) {
            JsonNode root = MAPPER.readTree(is);
            JsonNode objectNode = root.get(objectKey);

            if (objectNode == null) {
                throw new RuntimeException("Key '" + objectKey + "' not found in " + jsonFileName);
            }

            return MAPPER.treeToValue(objectNode, clazz);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON object from: " + jsonFileName + " [" + objectKey + "]", e);
        }
    }

    // Private helpers

    private static InputStream openStream(String jsonFileName) {
        String fullPath = TEST_DATA_PATH + jsonFileName;
        InputStream is = JsonDataReader.class.getClassLoader().getResourceAsStream(fullPath);

        if (is == null) {
            throw new RuntimeException(
                "Test data file not found on classpath: '" + fullPath + "'\n" +
                "Make sure the file exists in src/test/resources/testdata/"
            );
        }

        log.debug("Loading test data from: {}", fullPath);
        return is;
    }
}
