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
public class DiscountCampaignStyleRequest {
    private Long id;
    private Long styleId;
    private String styleName;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
}
