package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface DiscountCampaignService {
    List<DiscountCampaignResponse> getAll();

    @Transactional
    DiscountCampaignResponse createDiscountCampaign(DiscountCampaignRequest request);
}
