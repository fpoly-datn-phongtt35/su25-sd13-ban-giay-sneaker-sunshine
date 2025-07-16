package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DiscountCampaignService {
    Page<DiscountCampaignResponse> getAll(int page, int size);

    DiscountCampaignResponse getDetail(Long id);

    @Transactional
    void delete(Long id);

    @Transactional
    DiscountCampaignResponse createDiscountCampaign(DiscountCampaignRequest request);
}
