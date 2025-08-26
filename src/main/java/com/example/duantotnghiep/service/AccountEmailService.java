package com.example.duantotnghiep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEmailService {

    private final EmailService emailService;

    /**
     * Gửi email tài khoản mới ở chế độ bất đồng bộ để không chặn HTTP request.
     */
    @Async
    public void sendAccountCreatedEmail(String recipientEmail, String customerName,
                                        String username, String rawPassword) {
        try {
            String subject = "🎉 Tài khoản của bạn đã được tạo thành công!";

            String content = "<h3>Xin chào " + customerName + ",</h3>"
                    + "<p>🎉 Chúc mừng! Tài khoản của bạn tại hệ thống Sneaker Sunshine đã được tạo thành công.</p>"
                    + "<p>Dưới đây là thông tin đăng nhập của bạn:</p>"
                    + "<ul>"
                    + "<li><strong>Tên đăng nhập:</strong> " + username + "</li>"
                    + "<li><strong>Mật khẩu:</strong> " + rawPassword + "</li>"
                    + "</ul>"
                    + "<p>Bạn vui lòng đăng nhập và đổi mật khẩu để đảm bảo an toàn.</p>"
                    + "<br><p>Trân trọng,<br>Đội ngũ hỗ trợ</p>";

            emailService.sendEmail(recipientEmail, subject, content);
            log.info("[AccountEmailService] Sent account email to {}", recipientEmail);
        } catch (Exception ex) {
            // Không để exception làm fail request chính
            log.error("[AccountEmailService] Failed to send account email to {}: {}", recipientEmail, ex.getMessage(), ex);
        }
    }
}
