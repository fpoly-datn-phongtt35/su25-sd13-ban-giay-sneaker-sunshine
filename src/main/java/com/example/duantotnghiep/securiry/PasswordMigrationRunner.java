package com.example.duantotnghiep.securiry;

import com.example.duantotnghiep.dto.response.UserDTO;
import com.example.duantotnghiep.model.User;
import com.example.duantotnghiep.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String password = user.getPassword();

            // Kiểm tra nếu mật khẩu chưa được mã hóa (không bắt đầu bằng $2a$ hoặc $2b$)
            if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode(password));
            }
        }

        userRepository.saveAll(users);
        System.out.println("✅ Đã mã hóa mật khẩu cho các user cũ.");
    }
}



