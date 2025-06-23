package com.example.duantotnghiep.dto.response;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceResponse {
    Long id;
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    @Size(max = 250)
    String description;
    Integer orderType;
    Integer status;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    @Size(max = 50)
    String createdBy;
    @Size(max = 50)
    String updatedBy;

    private String customerName;

    private String employeeName;

    private String phone;

    private String invoiceCode;

    private Long customerId;

    private VoucherResponse voucher;

    private BigDecimal shippingFee; // 👈 Thêm trường này

    private List<InvoiceDetailResponse> invoiceDetails;


}