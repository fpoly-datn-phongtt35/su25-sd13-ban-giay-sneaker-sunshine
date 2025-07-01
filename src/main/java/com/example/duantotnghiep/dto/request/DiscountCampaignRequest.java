package com.example.duantotnghiep.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DiscountCampaignRequest {
    private String campaignCode;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;

    private List<DiscountCampaignBrandRequest> brands;
    private List<DiscountCampaignStyleRequest> styles;
    private List<DiscountCampaignScopeRequest> scopes;
}

