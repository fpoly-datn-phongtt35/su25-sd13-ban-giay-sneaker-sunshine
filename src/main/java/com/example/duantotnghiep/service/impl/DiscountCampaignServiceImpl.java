package com.example.duantotnghiep.service.impl;


import com.example.duantotnghiep.dto.request.DiscountCampaignProductRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.mapper.DiscountCampaignMapper;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProduct;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import com.example.duantotnghiep.repository.ProductRepository;
import com.example.duantotnghiep.service.DiscountCampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountCampaignServiceImpl implements DiscountCampaignService {

    private final DiscountCampaignRepository repository;
    private final DiscountCampaignMapper discountCampaignMapper;
    private final DiscountCampaignRepository discountCampaignRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    public Page<DiscountCampaignResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return repository.findAll(pageable)
                .map(discountCampaignMapper::toResponse);
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

        // Nếu đã bị hủy rồi thì không cần cập nhật lại
        if (campaign.getStatus() != null && campaign.getStatus() == 2) {
            throw new IllegalStateException("Đợt giảm giá này đã bị vô hiệu hóa trước đó.");
        }

        // Chuyển trạng thái sang đã bị hủy
        campaign.setStatus(2);
        campaign.setUpdatedDate(LocalDateTime.now());

        repository.save(campaign);
    }


    @Transactional
    @Override
    public DiscountCampaignResponse createDiscountCampaign(DiscountCampaignRequest request) {
        // 0) Tạo campaignCode nếu chưa có
        String campaignCode = (request.getCampaignCode() == null || request.getCampaignCode().trim().isEmpty())
                ? "CAMPAIGN_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "_" + ((int)(Math.random() * 9000) + 1000)
                : request.getCampaignCode();

        // 1) Map từ request sang entity (bỏ qua products)
        DiscountCampaign campaign = discountCampaignMapper.toEntity(request);
        campaign.setCampaignCode(campaignCode);
        campaign.setStatus(1); // Đang hoạt động
        campaign.setDiscountPercentage(request.getDiscountPercentage());
        campaign.setDescription(request.getDescription());

        // ✅ Gán ngày bắt đầu / kết thúc nếu có
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());

        // ✅ Gán ngày tạo / cập nhật
        LocalDateTime now = LocalDateTime.now();
        campaign.setCreatedDate(now);
        campaign.setUpdatedDate(now);

        // 2) Gán danh sách sản phẩm nếu có
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            List<DiscountCampaignProduct> productList = new ArrayList<>();
            for (DiscountCampaignProductRequest productReq : request.getProducts()) {
                Product product = productRepository.findById(productReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productReq.getProductId()));

                DiscountCampaignProduct dcp = new DiscountCampaignProduct();
                dcp.setProduct(product);
                dcp.setCampaign(campaign);
                productList.add(dcp);
            }
            campaign.setProducts(productList);
        }

        // 3) Lưu chiến dịch giảm giá
        DiscountCampaign savedCampaign = discountCampaignRepository.save(campaign);

        // 4) Trả về response
        return discountCampaignMapper.toResponse(savedCampaign);
    }

}
