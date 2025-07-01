package com.example.duantotnghiep.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountCampaignStyleResponse {
    private Long id;
    private Long styleId;
    private String styleName;
    private BigDecimal discountPercentage;
}
