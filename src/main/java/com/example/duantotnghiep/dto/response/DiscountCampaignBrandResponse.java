package com.example.duantotnghiep.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountCampaignBrandResponse {
    private Long id;
    private Long brandId;
    private String brandName;
    private BigDecimal discountPercentage;
}
