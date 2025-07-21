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
    private BigDecimal sellPrice;
    private BigDecimal discountedPrice;
    private Integer discountPercentage;
}

