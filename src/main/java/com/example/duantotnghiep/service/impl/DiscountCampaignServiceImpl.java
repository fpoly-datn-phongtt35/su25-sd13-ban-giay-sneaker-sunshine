package com.example.duantotnghiep.service.impl;


import com.example.duantotnghiep.dto.request.DiscountCampaignProductRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignStatisticsResponse;
import com.example.duantotnghiep.mapper.DiscountCampaignMapper;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProduct;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.repository.DiscountCampaignProductRepository;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import com.example.duantotnghiep.repository.InvoiceRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountCampaignServiceImpl implements DiscountCampaignService {

    private final DiscountCampaignRepository repository;
    private final DiscountCampaignMapper discountCampaignMapper;
    private final DiscountCampaignRepository discountCampaignRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InvoiceRepository invoiceRepository;
    private final DiscountCampaignProductRepository discountCampaignProductRepository;

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

    @Transactional
    public DiscountCampaignResponse updateDiscountCampaign(Long id, DiscountCampaignRequest request) {
        DiscountCampaign c = discountCampaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));

        // ===== Cập nhật scalar fields =====
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            c.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            c.setDescription(request.getDescription());
        }
        if (request.getDiscountPercentage() != null) {
            c.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getCampaignCode() != null && !request.getCampaignCode().trim().isEmpty()) {
            c.setCampaignCode(request.getCampaignCode().trim());
        }
        if (request.getStartDate() != null) {
            c.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            c.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            c.setStatus(request.getStatus());
        }

        // Validate thời gian
        if (c.getStartDate() != null && c.getEndDate() != null && c.getEndDate().isBefore(c.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu.");
        }

        // ===== Cập nhật products theo kiểu diff (orphanRemoval=true) =====
        if (request.getProducts() != null) {
            // set hiện có
            java.util.Set<Long> existing = c.getProducts().stream()
                    .map(l -> l.getProduct().getId())
                    .collect(java.util.stream.Collectors.toSet());

            // set mới (lọc null/trùng)
            java.util.Set<Long> incoming = request.getProducts().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(DiscountCampaignProductRequest::getProductId)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

            // remove những productId không còn
            c.getProducts().removeIf(link -> !incoming.contains(link.getProduct().getId()));

            // add những productId mới
            for (Long pid : incoming) {
                if (!existing.contains(pid)) {
                    Product p = productRepository.findById(pid)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + pid));
                    DiscountCampaignProduct link = new DiscountCampaignProduct();
                    link.setCampaign(c);   // rất quan trọng
                    link.setProduct(p);
                    c.getProducts().add(link);
                }
            }
        }

        c.setUpdatedDate(java.time.LocalDateTime.now());
        DiscountCampaign saved = discountCampaignRepository.save(c);
        return discountCampaignMapper.toResponse(saved); // map từ entity
    }



    @Override
    public DiscountCampaignStatisticsResponse getStatistics(Long campaignId) {
        return invoiceRepository.getStatisticsByCampaignId(campaignId);
    }

}
