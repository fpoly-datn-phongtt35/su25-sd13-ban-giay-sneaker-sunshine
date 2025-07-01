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
public class DiscountCampaignBrandRequest {
    private Long id;
    private Long brandId;
    private String brandName;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
}
