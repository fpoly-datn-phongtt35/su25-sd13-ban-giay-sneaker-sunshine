package com.example.duantotnghiep.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartItemRequest {
    private Long productDetailId;
    private Integer quantity;

    private BigDecimal sellPrice;            // thêm
    private BigDecimal discountedPrice;      // thêm
    private Integer discountPercentage;      // thêm
}

