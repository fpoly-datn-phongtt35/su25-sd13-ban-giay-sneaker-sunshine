package com.example.duantotnghiep.dto.response;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerResponse {
    Long id;
    @Size(max = 50)
    String customerCode;
    @Size(max = 255)
    String customerName;
    @Size(max = 255)
    String email;
    @Size(max = 250)
    String passWord;
    @Size(max = 50)
    String phone;
    Integer gender;
    LocalDateTime dateOfBirth;
    @Size(max = 100)
    String country;
    @Size(max = 100)
    String province;
    @Size(max = 100)
    String district;
    @Size(max = 100)
    String ward;
    @Size(max = 250)
    String houseName;
    Integer status;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    @Size(max = 50)
    String createdBy;
    @Size(max = 50)
    String updatedBy;
}