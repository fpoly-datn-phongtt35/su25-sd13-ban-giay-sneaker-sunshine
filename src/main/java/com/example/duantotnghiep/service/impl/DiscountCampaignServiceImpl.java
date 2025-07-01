package com.example.duantotnghiep.service.impl;


import com.example.duantotnghiep.dto.request.DiscountCampaignBrandRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignScopeRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignStyleRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.mapper.DiscountCampaignMapper;
import com.example.duantotnghiep.model.Brand;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignBrand;
import com.example.duantotnghiep.model.DiscountCampaignScope;
import com.example.duantotnghiep.model.DiscountCampaignStyle;
import com.example.duantotnghiep.model.Style;
import com.example.duantotnghiep.repository.BrandRepository;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import com.example.duantotnghiep.repository.StyleRepository;
import com.example.duantotnghiep.service.DiscountCampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountCampaignServiceImpl implements DiscountCampaignService {

    private final DiscountCampaignRepository repository;
    private final DiscountCampaignMapper discountCampaignMapper;
    private final DiscountCampaignRepository discountCampaignRepository;
    private final BrandRepository brandRepository;
    private final StyleRepository styleRepository;

    @Override
    public List<DiscountCampaignResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(discountCampaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DiscountCampaignResponse createDiscountCampaign(DiscountCampaignRequest request) {
        // Map trường cơ bản từ request → entity (không bao gồm brands/styles/scopes)
        DiscountCampaign campaign = discountCampaignMapper.toEntity(request);

        // Lưu campaign trước để có ID
        campaign = discountCampaignRepository.save(campaign);

        // Xử lý scopes
        if (request.getScopes() != null) {
            for (DiscountCampaignScopeRequest scopeReq : request.getScopes()) {
                DiscountCampaignScope scope = new DiscountCampaignScope();
                scope.setScopeType(scopeReq.getScopeType());
                scope.setDiscountPercentage(scopeReq.getDiscountPercentage());
                scope.setCampaign(campaign); // set quan hệ cha

                // Gán style hoặc brand nếu có
                if ("STYLE".equalsIgnoreCase(scopeReq.getScopeType()) && scopeReq.getStyleId() != null) {
                    Style style = styleRepository.findById(scopeReq.getStyleId())
                            .orElseThrow(() -> new RuntimeException("Style not found with id: " + scopeReq.getStyleId()));
                    scope.setStyle(style);
                }

                if ("BRAND".equalsIgnoreCase(scopeReq.getScopeType()) && scopeReq.getBrandId() != null) {
                    Brand brand = brandRepository.findById(scopeReq.getBrandId())
                            .orElseThrow(() -> new RuntimeException("Brand not found with id: " + scopeReq.getBrandId()));
                    scope.setBrand(brand);
                }

                // Thêm vào list
                campaign.getScopes().add(scope);
            }
        }

        // Xử lý brands
        if (request.getBrands() != null) {
            for (DiscountCampaignBrandRequest brandReq : request.getBrands()) {
                Brand brand = brandRepository.findById(brandReq.getBrandId())
                        .orElseThrow(() -> new RuntimeException("Brand not found with id: " + brandReq.getBrandId()));
                DiscountCampaignBrand dcb = new DiscountCampaignBrand();
                dcb.setBrand(brand);
                dcb.setDiscountPercentage(brandReq.getDiscountPercentage());
                dcb.setCampaign(campaign); // set quan hệ cha
                campaign.getBrands().add(dcb);
            }
        }

        // Xử lý styles
        if (request.getStyles() != null) {
            for (DiscountCampaignStyleRequest styleReq : request.getStyles()) {
                Style style = styleRepository.findById(styleReq.getStyleId())
                        .orElseThrow(() -> new RuntimeException("Style not found with id: " + styleReq.getStyleId()));
                DiscountCampaignStyle dcs = new DiscountCampaignStyle();
                dcs.setStyle(style);
                dcs.setDiscountPercentage(styleReq.getDiscountPercentage());
                dcs.setCampaign(campaign); // set quan hệ cha
                campaign.getStyles().add(dcs);
            }
        }

        // Lưu lại để cascade lưu các quan hệ con (nếu entity đã setup CascadeType.PERSIST hoặc ALL)
        campaign = discountCampaignRepository.save(campaign);

        // Trả về response
        return discountCampaignMapper.toResponse(campaign);
    }

}
