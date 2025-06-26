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
    String description;
    Integer orderType;
    Integer status;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    String createdBy;
    String updatedBy;

    private String customerName;

    private String employeeName;

    private String phone;

    private String invoiceCode;

    private Long customerId;

    private VoucherResponse voucher;
    private BigDecimal shippingFee;

    private List<InvoiceDetailResponse> invoiceDetails;

    public InvoiceResponse(Long id, String invoiceCode, Integer status, Integer orderType,
                           LocalDateTime createdDate, String customerName, String phone, BigDecimal totalAmount) {
        this.id = id;
        this.invoiceCode = invoiceCode;
        this.status = status;
        this.orderType = orderType;
        this.createdDate = createdDate;
        this.customerName = customerName;
        this.phone = phone;
        this.totalAmount = totalAmount;
    }


}