package com.example.duantotnghiep.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DiscountCampaignScopeResponse {
    private Long id;
    private String scopeType;
    private BigDecimal discountPercentage;
    private Long brandId;
    private String brandName;
    private Long styleId;
    private String styleName;
}

