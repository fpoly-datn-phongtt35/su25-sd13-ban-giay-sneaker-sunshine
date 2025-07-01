package com.example.duantotnghiep.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiscountCampaignResponse {
    private Long id;
    private String campaignCode;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;
    private List<DiscountCampaignScopeResponse> scopes;
    private List<DiscountCampaignStyleResponse> styles;
    private List<DiscountCampaignBrandResponse> brands;
}
