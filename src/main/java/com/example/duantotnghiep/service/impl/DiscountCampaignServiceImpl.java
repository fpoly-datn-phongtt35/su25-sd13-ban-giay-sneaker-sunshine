package com.example.duantotnghiep.service.impl;


import com.example.duantotnghiep.dto.request.DiscountCampaignProductRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignProductDetailRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.mapper.DiscountCampaignMapper;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProduct;
import com.example.duantotnghiep.model.DiscountCampaignProductDetail;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import com.example.duantotnghiep.repository.ProductRepository;
import com.example.duantotnghiep.service.DiscountCampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountCampaignServiceImpl implements DiscountCampaignService {

    private final DiscountCampaignRepository repository;
    private final DiscountCampaignMapper discountCampaignMapper;
    private final DiscountCampaignRepository discountCampaignRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    public List<DiscountCampaignResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(discountCampaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountCampaignResponse getDetail(Long id) {
        DiscountCampaign campaign = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));

        return discountCampaignMapper.toResponse(campaign);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        DiscountCampaign campaign = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));

        // Chuyển trạng thái từ 1 (hoạt động) sang 2 (đã xoá hoặc không hoạt động)
        campaign.setStatus(2);
        campaign.setUpdatedDate(new Date());

        repository.save(campaign);
    }

    @Transactional
    @Override
    public DiscountCampaignResponse createDiscountCampaign(DiscountCampaignRequest request) {
        // 0) Gen campaignCode nếu chưa có
        String campaignCode = (request.getCampaignCode() == null || request.getCampaignCode().trim().isEmpty())
                ? "CAMPAIGN_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "_" + ((int)(Math.random() * 9000) + 1000)
                : request.getCampaignCode();

        // 1) Map trường cơ bản
        DiscountCampaign campaign = discountCampaignMapper.toEntity(request);
        campaign.setCampaignCode(campaignCode);
        campaign.setStatus(1); // Active
        campaign.setDiscountPercentage(request.getDiscountPercentage());
        campaign.setDescription(request.getDescription());

        // 2) Xử lý products
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            List<DiscountCampaignProduct> productList = new ArrayList<>();
            for (DiscountCampaignProductRequest productReq : request.getProducts()) {
                Product product = productRepository.findById(productReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productReq.getProductId()));
                DiscountCampaignProduct dcp = new DiscountCampaignProduct();
                dcp.setProduct(product);
                dcp.setCampaign(campaign);
                productList.add(dcp);
            }
            campaign.setProducts(productList);
        }

        // 4) Lưu campaign (cascade sẽ tự lưu quan hệ con)
        DiscountCampaign savedCampaign = discountCampaignRepository.save(campaign);

        // 5) Map sang response
        return discountCampaignMapper.toResponse(savedCampaign);
    }



}
