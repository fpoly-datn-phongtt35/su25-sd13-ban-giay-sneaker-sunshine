package com.example.duantotnghiep.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class VoucherEmailService {

    private final EmailService emailService;

    public void sendVoucherNotificationEmail(String recipientEmail, String customerName,
                                             BigDecimal totalAmount, int discountAmount,
                                             String voucherCode, LocalDate endDate) {
        String subject = "🎉 Chúc mừng bạn nhận được voucher!";

        // Định dạng tiền tệ Việt Nam
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedTotal = currencyFormatter.format(totalAmount);
        String formattedDiscount = currencyFormatter.format(discountAmount);

        String content = "<h3>Xin chào " + customerName + ",</h3>"
                + "<p>🎉 Cảm ơn bạn đã mua hàng tại hệ thống của chúng tôi!</p>"
                + "<p>Với đơn hàng trị giá " + formattedTotal + ", bạn đã nhận được một "
                + "<strong>Voucher trị giá " + formattedDiscount + "</strong>.</p>"
                + "<p><strong>Mã voucher:</strong> " + voucherCode + "</p>"
                + "<p>Hạn sử dụng: đến ngày " + endDate + "</p>"
                + "<br><p>Trân trọng,<br>Đội ngũ hỗ trợ</p>";

        emailService.sendEmail(recipientEmail, subject, content);
    }
}

