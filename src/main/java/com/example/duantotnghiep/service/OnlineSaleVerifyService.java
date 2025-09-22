package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.CartItemRequest;
import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.VerifyPricesResponse;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProductDetail;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnlineSaleVerifyService {

    private final ProductDetailRepository productDetailRepository;
    private final DiscountCampaignRepository discountCampaignRepository;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final long PRICE_TOLERANCE_VND = 1L; // ±1₫

    // ===== Helpers =====
    private static BigDecimal vnd(BigDecimal x) {
        if (x == null) return BigDecimal.ZERO;
        return x.setScale(0, RoundingMode.HALF_UP);
    }
    private static BigDecimal nz(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }
    private static boolean eqWithinVND(BigDecimal a, BigDecimal b, long tol) {
        if (a == null || b == null) return Objects.equals(a, b);
        return a.subtract(b).abs().compareTo(BigDecimal.valueOf(tol)) <= 0;
    }

    @Transactional(readOnly = true)
    public VerifyPricesResponse verifyPrices(InvoiceRequest request) {
        VerifyPricesResponse resp = new VerifyPricesResponse();
        resp.setOk(true);

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            resp.setOk(false);
            resp.setMessage("Không có sản phẩm để xác thực giá.");
            resp.setDiffs(new ArrayList<>());
            return resp;
        }

        List<DiscountCampaign> activeCampaigns =
                discountCampaignRepository.findActiveCampaigns(LocalDateTime.now());

        List<VerifyPricesResponse.DiffItem> diffs = new ArrayList<>();
        String firstMismatchProductName = null;

        for (CartItemRequest item : request.getItems()) {
            // 1) Lấy PD & Product
            ProductDetail pd = productDetailRepository.findByIdAndStatus(item.getProductDetailId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy sản phẩm chi tiết ID: " + item.getProductDetailId()));
            Product product = pd.getProduct();

            // 2) Tính giá phía server (y hệt createInvoiceShipCode):
            //    - Không gửi campaignId -> chọn BEST trong active
            //    - Gửi campaignId & áp dụng được -> ép dùng campaign đó
            Long feCampId = item.getDiscountCampaignId();
            Pricing server = computePricingLikeCreate(pd, product, activeCampaigns, feCampId);

            // 3) Snapshot FE (VND nguyên)
            BigDecimal feSell = vnd(nz(item.getSellPrice()));
            BigDecimal feDisc = (item.getDiscountedPrice() == null) ? feSell : vnd(nz(item.getDiscountedPrice()));

            // 4) Chỉ so sánh GIÁ ĐÃ GIẢM (FE vs SV)
            BigDecimal svDisc = server.getDiscountedPrice();
            boolean mismatch = !eqWithinVND(feDisc, svDisc, PRICE_TOLERANCE_VND);

            if (mismatch) {
                resp.setOk(false);

                // Lấy tên sản phẩm ưu tiên từ FE, fallback về tên trong DB, cuối cùng là mã SPCT
                String productName =
                        (item.getProductName() != null && !item.getProductName().isBlank()) ? item.getProductName()
                                : (product != null && product.getProductName() != null ? product.getProductName()
                                : ("SP " + pd.getId()));

                if (firstMismatchProductName == null) {
                    firstMismatchProductName = productName;
                }

                VerifyPricesResponse.DiffItem d = new VerifyPricesResponse.DiffItem();
                d.setProductDetailId(item.getProductDetailId());
                d.setQuantity(item.getQuantity());
                d.setFeSellPrice(feSell);
                d.setFeDiscountedPrice(feDisc);
                d.setFePercent(calcPercent(feSell, feDisc));
                d.setFeCampaignId(feCampId);
                d.setSvSellPrice(server.getSellPrice());
                d.setSvDiscountedPrice(svDisc);
                d.setSvPercent(server.getPercentInt());
                d.setSvCampaignId(server.getCampaignId());
                // Không trả reason dài dòng nữa
                d.setReason("DISCOUNTED_PRICE_MISMATCH");
                diffs.add(d);
            }
        }

        if (!resp.isOk()) {
            // Chỉ bắn tên sản phẩm đầu tiên bị lệch
            String name = (firstMismatchProductName == null ? "một sản phẩm" : firstMismatchProductName);
            resp.setMessage("Giá sau giảm đã thay đổi cho sản phẩm: " + name + ".");
        }

        resp.setDiffs(diffs);
        return resp;
    }

    private static int calcPercent(BigDecimal sell, BigDecimal disc) {
        if (sell == null || sell.signum() == 0 || disc == null) return 0;
        return sell.subtract(disc).multiply(ONE_HUNDRED)
                .divide(sell, 0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * TÍNH GIÁ giống createInvoiceShipCode:
     * - Nếu forceCampaignId == null: CHỌN BEST CAMPAIGN trong active
     *   (ưu tiên % ở SPCT > % theo Product (nếu có) > % campaign).
     * - Nếu forceCampaignId != null và campaign áp dụng được: ÉP dùng campaign đó
     *   (vẫn ưu tiên % SPCT > % Product > % campaign).
     */
    private Pricing computePricingLikeCreate(ProductDetail pd,
                                             Product product,
                                             List<DiscountCampaign> activeCampaigns,
                                             Long forceCampaignId) {

        BigDecimal sellPrice = Optional.ofNullable(pd.getSellPrice())
                .orElse(Optional.ofNullable(product.getSellPrice()).orElse(BigDecimal.ZERO));
        sellPrice = vnd(sellPrice); // chuẩn VND nguyên

        // Ưu tiên campaign FE gửi (nếu có) và áp dụng được
        if (forceCampaignId != null) {
            Optional<DiscountCampaign> chosenOpt = activeCampaigns.stream()
                    .filter(c -> c.getId().equals(forceCampaignId))
                    .findFirst();
            if (chosenOpt.isPresent()) {
                DiscountCampaign chosen = chosenOpt.get();

                boolean applicable =
                        (chosen.getProductDetails() != null && chosen.getProductDetails().stream()
                                .anyMatch(link -> link.getProductDetail() != null && link.getProductDetail().getId().equals(pd.getId())))
                                ||
                                (chosen.getProducts() != null && chosen.getProducts().stream()
                                        .anyMatch(link -> link.getProduct() != null && link.getProduct().getId().equals(product.getId())));

                if (applicable) {
                    BigDecimal pctPd = null;
                    if (chosen.getProductDetails() != null) {
                        pctPd = chosen.getProductDetails().stream()
                                .filter(link -> link.getProductDetail() != null && link.getProductDetail().getId().equals(pd.getId()))
                                .map(DiscountCampaignProductDetail::getDiscountPercentage)
                                .filter(Objects::nonNull)
                                .findFirst().orElse(null);
                    }

                    BigDecimal pctProduct = null;
                    if (chosen.getProducts() != null && chosen.getProducts().stream()
                            .anyMatch(link -> link.getProduct() != null && link.getProduct().getId().equals(product.getId()))) {
                        // Nếu có % riêng ở link Product, thay bằng link.getDiscountPercentage()
                        pctProduct = chosen.getDiscountPercentage();
                    }

                    BigDecimal bestPct = pctPd != null ? pctPd
                            : (pctProduct != null ? pctProduct
                            : Optional.ofNullable(chosen.getDiscountPercentage()).orElse(BigDecimal.ZERO));
                    if (bestPct == null) bestPct = BigDecimal.ZERO;

                    BigDecimal discounted = bestPct.compareTo(BigDecimal.ZERO) > 0
                            ? sellPrice.multiply(ONE_HUNDRED.subtract(bestPct)).divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP)
                            : sellPrice;

                    Pricing p = new Pricing();
                    p.setCampaignId(chosen.getId());
                    p.setSellPrice(sellPrice);
                    p.setPercent(bestPct);
                    p.setPercentInt(bestPct.setScale(0, RoundingMode.HALF_UP).intValue());
                    p.setDiscountedPrice(vnd(discounted));
                    return p;
                }
            }
            // nếu FE gửi campaign nhưng không áp dụng → rơi xuống chọn BEST
        }

        // Chọn BEST trong active (ưu tiên SPCT > Product > % campaign)
        AbstractMap.SimpleEntry<Long, BigDecimal> best = null;

        for (DiscountCampaign c : activeCampaigns) {

            BigDecimal pctPd = null;
            if (c.getProductDetails() != null) {
                pctPd = c.getProductDetails().stream()
                        .filter(link -> link.getProductDetail() != null
                                && link.getProductDetail().getId().equals(pd.getId()))
                        .map(DiscountCampaignProductDetail::getDiscountPercentage)
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);
            }

            BigDecimal pctProduct = null;
            if (c.getProducts() != null) {
                boolean matched = c.getProducts().stream()
                        .anyMatch(link -> link.getProduct() != null
                                && link.getProduct().getId().equals(product.getId()));
                if (matched) {
                    // Nếu link Product có % riêng: thay bằng link.getDiscountPercentage()
                    pctProduct = c.getDiscountPercentage();
                }
            }

            BigDecimal chosen = pctPd != null ? pctPd
                    : (pctProduct != null ? pctProduct
                    : Optional.ofNullable(c.getDiscountPercentage()).orElse(BigDecimal.ZERO));
            if (chosen == null) chosen = BigDecimal.ZERO;

            if (best == null || chosen.compareTo(best.getValue()) > 0) {
                best = new AbstractMap.SimpleEntry<>(c.getId(), chosen);
            }
        }

        Long bestCampaignId = best != null ? best.getKey()   : null;
        BigDecimal bestPct  = best != null ? best.getValue() : BigDecimal.ZERO;
        if (bestPct == null) bestPct = BigDecimal.ZERO;

        BigDecimal discounted = bestPct.compareTo(BigDecimal.ZERO) > 0
                ? sellPrice.multiply(ONE_HUNDRED.subtract(bestPct)).divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP)
                : sellPrice;

        Pricing p = new Pricing();
        p.setCampaignId(bestCampaignId);
        p.setSellPrice(sellPrice);
        p.setPercent(bestPct);
        p.setPercentInt(bestPct.setScale(0, RoundingMode.HALF_UP).intValue());
        p.setDiscountedPrice(vnd(discounted));
        return p;
    }

    @Getter @Setter
    private static class Pricing {
        private Long campaignId;
        private BigDecimal sellPrice;       // VND, scale 0
        private BigDecimal percent;         // %
        private Integer    percentInt;      // %
        private BigDecimal discountedPrice; // VND, scale 0
    }
}
