package com.automationexercise.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * LoginData – Dữ liệu cho negative login test cases.
 *
 * Được dùng với @DataProvider để chạy cùng một test với nhiều bộ dữ liệu.
 *
 * JSON structure expected:
 * {
 *   "invalidLoginUsers": [
 *     { "email": "...", "password": "...", "expectedError": "..." },
 *     { "email": "...", "password": "...", "expectedError": "..." }
 *   ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginData {

    private String email;
    private String password;
    private String expectedError;

    public String getEmail()         { return email; }
    public String getPassword()      { return password; }
    public String getExpectedError() { return expectedError; }

    public void setEmail(String email)                  { this.email = email; }
    public void setPassword(String password)            { this.password = password; }
    public void setExpectedError(String expectedError)  { this.expectedError = expectedError; }

    @Override
    public String toString() {
        return "LoginData{email='" + email.split("@")[0] + "@***'}";
    }
}
