package com.example.duantotnghiep.service.impl;


import com.example.duantotnghiep.dto.request.DiscountCampaignProductDetailRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignProductRequest;
import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignStatisticsResponse;
import com.example.duantotnghiep.mapper.DiscountCampaignMapper;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProduct;
import com.example.duantotnghiep.model.DiscountCampaignProductDetail;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductDetail;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
                + "_" + ((int) (Math.random() * 9000) + 1000)
                : request.getCampaignCode();

        // 1) Map cơ bản
        DiscountCampaign campaign = new DiscountCampaign();
        campaign.setCampaignCode(campaignCode);
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());

        // status mặc định 1 nếu null
        campaign.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        // % giảm giá mặc định ở campaign (BigDecimal 0..100, scale 2)
        campaign.setDiscountPercentage(normalizePercentageOrNull(request.getDiscountPercentage()));

        LocalDateTime now = LocalDateTime.now();
        campaign.setCreatedDate(now);
        campaign.setUpdatedDate(now);
        // createdBy/updatedBy nếu bạn có context user thì set thêm

        // 2) Mức PRODUCT (tuỳ bạn còn dùng không)
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            List<DiscountCampaignProduct> productLinks = new ArrayList<>();
            for (DiscountCampaignProductRequest pReq : request.getProducts()) {
                if (pReq == null || pReq.getProductId() == null) continue;

                Product product = productRepository.findById(pReq.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + pReq.getProductId()));

                DiscountCampaignProduct link = new DiscountCampaignProduct();
                link.setCampaign(campaign);
                link.setProduct(product);
                link.setCreatedDate(now);
                link.setUpdatedDate(now);
                productLinks.add(link);
            }
            campaign.setProducts(productLinks);
        }

        // 3) Mức PRODUCT DETAIL (SPCT) – chỉ insert những SPCT được gửi lên
        if (request.getProductDetails() != null && !request.getProductDetails().isEmpty()) {
            // Lọc ID hợp lệ + distinct
            List<Long> pdIds = request.getProductDetails().stream()
                    .filter(Objects::nonNull)
                    .map(DiscountCampaignProductDetailRequest::getProductDetailId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (!pdIds.isEmpty()) {
                // Load batch để kiểm tra tồn tại
                Map<Long, ProductDetail> pdMap = productDetailRepository.findAllById(pdIds)
                        .stream().collect(Collectors.toMap(ProductDetail::getId, it -> it));

                for (Long id : pdIds) {
                    if (!pdMap.containsKey(id)) {
                        throw new IllegalArgumentException("Không tìm thấy SPCT ID: " + id);
                    }
                }

                List<DiscountCampaignProductDetail> items = new ArrayList<>();
                for (DiscountCampaignProductDetailRequest dReq : request.getProductDetails()) {
                    if (dReq == null || dReq.getProductDetailId() == null) continue;

                    ProductDetail pd = pdMap.get(dReq.getProductDetailId());
                    if (pd == null) {
                        throw new IllegalArgumentException("Không tìm thấy SPCT ID: " + dReq.getProductDetailId());
                    }

                    DiscountCampaignProductDetail item = new DiscountCampaignProductDetail();
                    item.setCampaign(campaign);
                    item.setProductDetail(pd);

                    // % riêng trên SPCT (nullable => dùng % campaign khi tính)
                    BigDecimal perItem = normalizePercentageOrNull(dReq.getDiscountPercentage());
                    item.setDiscountPercentage(perItem);

                    item.setCreatedDate(java.sql.Timestamp.valueOf(now));
                    item.setUpdatedDate(java.sql.Timestamp.valueOf(now));

                    items.add(item);
                }
                campaign.setProductDetails(items);
            }
        }

        // 4) Lưu
        DiscountCampaign saved = discountCampaignRepository.save(campaign);

        // 5) Trả response
        // Nếu bạn đã có mapper:
        return discountCampaignMapper.toResponse(saved);
    }

    /**
     * Chuẩn hoá % BigDecimal về scale(2), kiểm tra 0..100. Null => null
     */
    private BigDecimal normalizePercentageOrNull(BigDecimal percent) {
        if (percent == null) return null;
        BigDecimal p = percent.setScale(2, RoundingMode.HALF_UP);
        if (p.compareTo(BigDecimal.ZERO) < 0 || p.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("discountPercentage phải nằm trong [0, 100]");
        }
        return p;
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
