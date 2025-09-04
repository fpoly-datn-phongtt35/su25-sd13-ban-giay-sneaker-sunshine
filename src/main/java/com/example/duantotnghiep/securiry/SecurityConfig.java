package com.example.duantotnghiep.securiry;

import com.example.duantotnghiep.JWT.JwtFilter;
import com.example.duantotnghiep.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

        @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập login và register
                        .requestMatchers("/api/auth/**").permitAll() // gồm /login-user

                        .requestMatchers("/api/online-sale/**").permitAll()

                        // ADMIN và STAFF có thể truy cập counter-sales
                        .requestMatchers("/api/admin/counter-sales/**").hasAnyRole("ADMIN", "STAFF")

                        // CHỈ ADMIN mới được phép truy cập thống kê
                        .requestMatchers("/api/admin/statistics/**").hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers("/api/admin/customers/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/employees/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/products/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/campaigns/**").hasRole("ADMIN")

                        // CHỈ ADMIN được phép vào các API admin khác
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "STAFF")

                        // Các request khác yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider());

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()  // Mở tất cả các đường dẫn
//                )
//                .sessionManagement(sess -> sess
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authenticationProvider(authenticationProvider());
//
//        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // ✅ Dùng cho mật khẩu plain text
    }
}




