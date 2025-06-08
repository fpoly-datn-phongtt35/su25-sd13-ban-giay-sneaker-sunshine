package com.example.duantotnghiep.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VoucherResponse {
    Long id;
    @NotNull
    @Size(max = 150)
    String voucherCode;
    @Size(max = 250)
    String voucherName;
    BigDecimal discountPercentage;
    @NotNull
    Integer discountAmount;
    @NotNull
    BigDecimal minOrderValue;
    BigDecimal maxDiscountValue;
    @NotNull
    LocalDateTime startDate;
    @NotNull
    LocalDateTime endDate;
    Integer status;
    @Size(max = 250)
    String description;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    @Size(max = 50)
    String createdBy;
    @Size(max = 50)
    String updatedBy;
    Integer orderType;
}