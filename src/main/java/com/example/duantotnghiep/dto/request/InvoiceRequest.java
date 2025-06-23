package com.example.duantotnghiep.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceRequest {
    private CustomerInfo customerInfo;
    private List<CartItemRequest> items;
    private Long employeeId;
    private BigDecimal discountAmount;
    private String description;
    private Integer orderType;
    private Integer status;
    private Long voucherId;
    private BigDecimal shippingFee; // ➕ Thêm dòng này
}

