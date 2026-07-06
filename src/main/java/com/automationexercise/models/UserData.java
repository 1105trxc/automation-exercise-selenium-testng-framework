package com.automationexercise.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * UserData – POJO (Plain Old Java Object) đại diện cho dữ liệu người dùng.
 *
 * WHY A POJO INSTEAD OF Map<String,Object>?
 * - Type-safe: compiler báo lỗi nếu gọi sai field
 * - IDE auto-complete hỗ trợ đầy đủ
 * - Dễ đọc: user.getEmail() rõ hơn map.get("email")
 * - Jackson serialize/deserialize tự động khi field name khớp JSON key
 *
 * @JsonIgnoreProperties(ignoreUnknown = true)
 * → Nếu JSON có thêm field mà POJO không có → bỏ qua thay vì throw exception
 * → Framework sẽ không bị break khi team thêm field vào JSON test data
 *
 * NOTE: email KHÔNG nằm trong JSON template.
 * Email được generate động bằng RandomDataUtils.generateUniqueEmail()
 * để đảm bảo tính độc lập của mỗi test run.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData {

    // --- Account section (trên trang /signup) ---
    private String title;           // "Mr" hoặc "Mrs"
    private String name;            // Full name displayed in nav
    private String password;
    private String dayOfBirth;      // "10"
    private String monthOfBirth;    // "3" (March)
    private String yearOfBirth;     // "1995"

    // --- Personal info ---
    private String firstName;
    private String lastName;
    private String company;

    // --- Address ---
    private String address1;
    private String address2;
    private String country;         // Must match dropdown option exactly: "United States"
    private String state;
    private String city;
    private String zipcode;
    private String mobile;

    // ---- Getters ----

    public String getTitle()        { return title; }
    public String getName()         { return name; }
    public String getPassword()     { return password; }
    public String getDayOfBirth()   { return dayOfBirth; }
    public String getMonthOfBirth() { return monthOfBirth; }
    public String getYearOfBirth()  { return yearOfBirth; }
    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
    public String getCompany()      { return company; }
    public String getAddress1()     { return address1; }
    public String getAddress2()     { return address2; }
    public String getCountry()      { return country; }
    public String getState()        { return state; }
    public String getCity()         { return city; }
    public String getZipcode()      { return zipcode; }
    public String getMobile()       { return mobile; }

    // ---- Setters ----

    public void setTitle(String title)              { this.title = title; }
    public void setName(String name)                { this.name = name; }
    public void setPassword(String password)        { this.password = password; }
    public void setDayOfBirth(String dayOfBirth)    { this.dayOfBirth = dayOfBirth; }
    public void setMonthOfBirth(String monthOfBirth){ this.monthOfBirth = monthOfBirth; }
    public void setYearOfBirth(String yearOfBirth)  { this.yearOfBirth = yearOfBirth; }
    public void setFirstName(String firstName)      { this.firstName = firstName; }
    public void setLastName(String lastName)        { this.lastName = lastName; }
    public void setCompany(String company)          { this.company = company; }
    public void setAddress1(String address1)        { this.address1 = address1; }
    public void setAddress2(String address2)        { this.address2 = address2; }
    public void setCountry(String country)          { this.country = country; }
    public void setState(String state)              { this.state = state; }
    public void setCity(String city)                { this.city = city; }
    public void setZipcode(String zipcode)          { this.zipcode = zipcode; }
    public void setMobile(String mobile)            { this.mobile = mobile; }

    @Override
    public String toString() {
        return "UserData{name='" + name + "', country='" + country + "'}";
    }
}
