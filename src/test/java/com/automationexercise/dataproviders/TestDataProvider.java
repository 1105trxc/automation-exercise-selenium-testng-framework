package com.automationexercise.dataproviders;

import com.automationexercise.models.LoginData;
import com.automationexercise.utils.JsonDataReader;
import org.testng.annotations.DataProvider;

import java.util.List;

/**
 * TestDataProvider – Cung cấp test data cho các @Test methods dùng @DataProvider.
 *
 * WHY @DataProvider?
 * Thay vì viết cùng một test 5 lần với 5 bộ dữ liệu khác nhau,
 * @DataProvider cho phép viết test 1 lần và chạy N lần với N bộ data.
 *
 * EXAMPLE OUTPUT cho "invalidLoginData":
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ TC-AE-003 Run #1: email=nonexistent1@fake.xyz, pass=WrongPass1 │
 * │ TC-AE-003 Run #2: email=another.wrong@nowhere.com, pass=Bad456 │
 * └─────────────────────────────────────────────────────────────────┘
 * → Cả hai đều verify cùng error message, nhưng với input khác nhau.
 *
 * HOW TO USE IN TEST:
 * @Test(dataProvider = "invalidLoginData", dataProviderClass = TestDataProvider.class)
 * public void loginShouldFail(LoginData data) {
 *     // data.getEmail(), data.getPassword(), data.getExpectedError()
 * }
 *
 * NAMING CONVENTION:
 * - DataProvider name = camelCase string matching what @Test references
 * - Method must return Object[][]
 */
public final class TestDataProvider {

    private TestDataProvider() {
        // Static utility class – no instantiation
    }

    /**
     * Provides invalid login credentials for TC-AE-003 negative login tests.
     * Data source: src/test/resources/testdata/users.json → "invalidLoginUsers" array
     *
     * @return Object[][] where each row is {LoginData}
     */
    @DataProvider(name = "invalidLoginData")
    public static Object[][] invalidLoginData() {
        List<LoginData> list = JsonDataReader.readList("users.json", "invalidLoginUsers", LoginData.class);

        // Convert List<LoginData> → Object[][] (format required by TestNG)
        // Each row = one test run
        // Each column = one parameter passed to @Test method
        return list.stream()
                   .map(data -> new Object[]{data})
                   .toArray(Object[][]::new);
    }
}
