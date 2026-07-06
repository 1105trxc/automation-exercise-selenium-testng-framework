package com.automationexercise.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ContactFormData – Dữ liệu cho Contact Us form (TC-AE-006).
 *
 * JSON structure expected:
 * {
 *   "contactFormData": [
 *     { "name": "...", "email": "...", "subject": "...", "message": "..." }
 *   ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactFormData {

    private String name;
    private String email;
    private String subject;
    private String message;

    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }

    public void setName(String name)       { this.name = name; }
    public void setEmail(String email)     { this.email = email; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "ContactFormData{name='" + name + "', subject='" + subject + "'}";
    }
}
