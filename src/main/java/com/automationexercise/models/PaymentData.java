package com.automationexercise.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PaymentData – POJO cho dữ liệu thanh toán thẻ tín dụng.
 *
 * Đọc từ: src/test/resources/testdata/checkout.json → "paymentData" array
 *
 * Dùng trong: TC-AE-014, TC-AE-015, TC-AE-016, TC-AE-024
 *   (Tất cả các flow đặt hàng cần nhập thông tin thẻ)
 *
 * Lưu ý: Card number 4111111111111111 là Visa test number chuẩn,
 * website automationexercise.com không thực sự charge tiền.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentData {

    private String nameOnCard;
    private String cardNumber;
    private String cvc;
    private String expiryMonth;
    private String expiryYear;
    private String orderComment;

    // Jackson requires no-arg constructor
    public PaymentData() {}

    public String getNameOnCard()   { return nameOnCard; }
    public String getCardNumber()   { return cardNumber; }
    public String getCvc()          { return cvc; }
    public String getExpiryMonth()  { return expiryMonth; }
    public String getExpiryYear()   { return expiryYear; }
    public String getOrderComment() { return orderComment; }

    @Override
    public String toString() {
        return "PaymentData{nameOnCard='" + nameOnCard + "', cardNumber='****', " +
               "expiryMonth='" + expiryMonth + "', expiryYear='" + expiryYear + "'}";
    }
}
