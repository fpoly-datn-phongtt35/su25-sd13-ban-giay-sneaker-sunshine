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
    public void run(String... args) {
        // KHÔNG thực hiện gì cả
        System.out.println("⚠️ Đã tắt mã hóa mật khẩu do đang sử dụng NoOpPasswordEncoder.");
    }
}




