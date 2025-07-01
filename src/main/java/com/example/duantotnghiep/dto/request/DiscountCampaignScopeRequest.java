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
public class DiscountCampaignScopeRequest {
    private Long id;
    private String scopeType;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private Long brandId;
    private String brandName;
    private Long styleId;
    private String styleName;
}
