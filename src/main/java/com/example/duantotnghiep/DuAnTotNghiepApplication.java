package com.example.duantotnghiep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
//@EnableScheduling
//@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class DuAnTotNghiepApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuAnTotNghiepApplication.class, args);
    }

}
