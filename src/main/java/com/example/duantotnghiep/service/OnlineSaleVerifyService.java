package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.CartItemRequest;
import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.VerifyPricesResponse;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProduct;
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

    // ======================== Helpers ========================
    private static BigDecimal vnd(BigDecimal x) {
        if (x == null) return BigDecimal.ZERO;
        return x.setScale(0, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }

    private static boolean eqWithinVND(BigDecimal a, BigDecimal b, long tol) {
        if (a == null || b == null) return Objects.equals(a, b);
        return a.subtract(b).abs().compareTo(BigDecimal.valueOf(tol)) <= 0;
    }

    private static int calcPercentInt(BigDecimal sell, BigDecimal disc) {
        if (sell == null || sell.signum() == 0 || disc == null) return 0;
        return sell.subtract(disc)
                .multiply(ONE_HUNDRED)
                .divide(sell, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /** Campaign có chứa trực tiếp SPCT? (bằng bảng discount_campaign_product_detail) */
    private static boolean hasProductDetailInCampaign(DiscountCampaign c, Long pdId) {
        if (c == null || c.getProductDetails() == null) return false;
        return c.getProductDetails().stream()
                .anyMatch(link -> link.getProductDetail() != null
                        && Objects.equals(link.getProductDetail().getId(), pdId));
    }

    /** Campaign có chứa Product? (bằng bảng discount_campaign_product) */
    private static boolean hasProductInCampaign(DiscountCampaign c, Long productId) {
        if (c == null || c.getProducts() == null) return false;
        return c.getProducts().stream()
                .anyMatch(link -> link.getProduct() != null
                        && Objects.equals(link.getProduct().getId(), productId));
    }

    // ======================== Public API ========================
    @Transactional(readOnly = true)
    public VerifyPricesResponse verifyPrices(InvoiceRequest request) {
        VerifyPricesResponse resp = new VerifyPricesResponse();
        resp.setOk(true);

        // Validate input
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            resp.setOk(false);
            resp.setMessage("Không có sản phẩm để xác thực giá.");
            resp.setDiffs(new ArrayList<>());
            return resp;
        }

        // Lấy campaign đang active
        List<DiscountCampaign> activeCampaigns =
                discountCampaignRepository.findActiveCampaigns(LocalDateTime.now());

        List<VerifyPricesResponse.DiffItem> diffs = new ArrayList<>();
        String firstMismatchProductName = null;

        for (CartItemRequest item : request.getItems()) {
            // 1) Load PD & Product
            ProductDetail pd = productDetailRepository.findByIdAndStatus(item.getProductDetailId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy sản phẩm chi tiết ID: " + item.getProductDetailId()));
            Product product = pd.getProduct();

            // 2) Tính giá phía server (chỉ giảm nếu campaign có LINK PD hoặc Product)
            Long feCampId = item.getDiscountCampaignId();
            Pricing serverPricing = computePricingOnlyIfLinked(pd, product, activeCampaigns, feCampId);

            // 3) Giá từ FE (VND nguyên)
            BigDecimal feSell = vnd(nz(item.getSellPrice()));
            BigDecimal feDisc = (item.getDiscountedPrice() == null)
                    ? feSell
                    : vnd(nz(item.getDiscountedPrice()));

            // 4) So sánh GIÁ SAU GIẢM
            BigDecimal svDisc = serverPricing.getDiscountedPrice();
            boolean mismatch = !eqWithinVND(feDisc, svDisc, PRICE_TOLERANCE_VND);

            if (mismatch) {
                resp.setOk(false);

                String productName =
                        (item.getProductName() != null && !item.getProductName().isBlank())
                                ? item.getProductName()
                                : (product != null && product.getProductName() != null
                                ? product.getProductName() : ("SP " + pd.getId()));

                if (firstMismatchProductName == null) {
                    firstMismatchProductName = productName;
                }

                VerifyPricesResponse.DiffItem d = new VerifyPricesResponse.DiffItem();
                d.setProductDetailId(item.getProductDetailId());
                d.setQuantity(item.getQuantity());

                d.setFeSellPrice(feSell);
                d.setFeDiscountedPrice(feDisc);
                d.setFePercent(calcPercentInt(feSell, feDisc));
                d.setFeCampaignId(feCampId);

                d.setSvSellPrice(serverPricing.getSellPrice());
                d.setSvDiscountedPrice(serverPricing.getDiscountedPrice());
                d.setSvPercent(serverPricing.getPercentInt());
                d.setSvCampaignId(serverPricing.getCampaignId());

                d.setReason("DISCOUNTED_PRICE_MISMATCH");

                // (Tuỳ chọn) nếu muốn đẩy cờ này ra FE, thêm field vào DiffItem rồi set:
                // d.setSvHasPdInCampaign(serverPricing.isHasPdInCampaign());
                // d.setSvHasProductInCampaign(serverPricing.isHasProductInCampaign());

                diffs.add(d);
            }
        }

        if (!resp.isOk()) {
            String name = (firstMismatchProductName == null ? "một sản phẩm" : firstMismatchProductName);
            resp.setMessage("Giá sau giảm đã thay đổi cho sản phẩm: " + name + ".");
        }

        resp.setDiffs(diffs);
        return resp;
    }

    // ======================== Core Pricing (liên kết thật) ========================
    /**
     * Rule theo 2 model mapping:
     * - Nếu FE ép campaign: chỉ áp khi campaign có LINK tới PD hoặc Product
     *   (ưu tiên % ở PD > % mặc định campaign khi có link Product).
     * - Nếu không ép: chỉ xét các campaign có LINK PD hoặc Product, chọn % cao nhất,
     *   tie-break ưu tiên campaign có link PD.
     * - Nếu không có bất kỳ LINK nào: không giảm (trả về sellPrice nguyên).
     */
    private Pricing computePricingOnlyIfLinked(ProductDetail pd,
                                               Product product,
                                               List<DiscountCampaign> activeCampaigns,
                                               Long forceCampaignId) {

        BigDecimal sellPrice = Optional.ofNullable(pd.getSellPrice())
                .orElse(Optional.ofNullable(product.getSellPrice()).orElse(BigDecimal.ZERO));
        sellPrice = vnd(sellPrice);

        // ===== 1) FE ép campaign =====
        if (forceCampaignId != null) {
            Optional<DiscountCampaign> chosenOpt = activeCampaigns.stream()
                    .filter(c -> Objects.equals(c.getId(), forceCampaignId))
                    .findFirst();

            if (chosenOpt.isPresent()) {
                DiscountCampaign chosen = chosenOpt.get();

                boolean linkPd  = hasProductDetailInCampaign(chosen, pd.getId());
                boolean linkPrd = hasProductInCampaign(chosen, product.getId());

                if (linkPd || linkPrd) {
                    PctPick pick = pickPctWithLinks(chosen, pd, product, linkPd, linkPrd);

                    BigDecimal discounted = (pick.pct.compareTo(BigDecimal.ZERO) > 0)
                            ? sellPrice.multiply(ONE_HUNDRED.subtract(pick.pct))
                            .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP)
                            : sellPrice;

                    Pricing p = new Pricing();
                    p.setCampaignId(chosen.getId());
                    p.setSellPrice(sellPrice);
                    p.setPercent(pick.pct);
                    p.setPercentInt(pick.pct.setScale(0, RoundingMode.HALF_UP).intValue());
                    p.setDiscountedPrice(vnd(discounted));
                    p.setHasPdInCampaign(linkPd);
                    p.setHasProductInCampaign(linkPrd);
                    return p;
                }
                // nếu FE gửi campaign nhưng không có LINK -> bỏ qua, rơi xuống nhánh auto
            }
        }

        // ===== 2) Không ép: chỉ xét campaign có LINK PD hoặc Product =====
        BigDecimal bestPct = BigDecimal.ZERO;
        Long bestCampaignId = null;
        boolean bestHasPd = false;
        boolean bestHasProduct = false;

        for (DiscountCampaign c : activeCampaigns) {
            boolean linkPd  = hasProductDetailInCampaign(c, pd.getId());
            boolean linkPrd = hasProductInCampaign(c, product.getId());

            if (!linkPd && !linkPrd) {
                // Không có liên kết -> không giảm
                continue;
            }

            PctPick pick = pickPctWithLinks(c, pd, product, linkPd, linkPrd);

            int cmp = pick.pct.compareTo(bestPct);
            if (bestCampaignId == null || cmp > 0) {
                bestPct = pick.pct;
                bestCampaignId = c.getId();
                bestHasPd = linkPd;
                bestHasProduct = linkPrd;
            } else if (cmp == 0) {
                // tie-break: ưu tiên campaign có link PD
                if (linkPd && !bestHasPd) {
                    bestPct = pick.pct;
                    bestCampaignId = c.getId();
                    bestHasPd = true;
                    bestHasProduct = linkPrd;
                }
            }
        }

        BigDecimal discounted = (bestPct.compareTo(BigDecimal.ZERO) > 0)
                ? sellPrice.multiply(ONE_HUNDRED.subtract(bestPct)).divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP)
                : sellPrice;

        Pricing p = new Pricing();
        p.setCampaignId(bestCampaignId);
        p.setSellPrice(sellPrice);
        p.setPercent(bestPct);
        p.setPercentInt(bestPct.setScale(0, RoundingMode.HALF_UP).intValue());
        p.setDiscountedPrice(vnd(discounted));
        p.setHasPdInCampaign(bestHasPd);
        p.setHasProductInCampaign(bestHasProduct);
        return p;
    }

    /**
     * Lấy % giảm khi BIẾT campaign có link PD hoặc/hoặc Product:
     * - Nếu có link PD: dùng DiscountCampaignProductDetail.discountPercentage (nếu null -> xem như 0).
     * - Nếu KHÔNG có link PD nhưng có link Product: dùng % mặc định của campaign.
     */
    private PctPick pickPctWithLinks(DiscountCampaign campaign,
                                     ProductDetail pd,
                                     Product product,
                                     boolean linkPd,
                                     boolean linkPrd) {
        BigDecimal pct = BigDecimal.ZERO;

        if (linkPd) {
            // tìm % riêng ở bảng discount_campaign_product_detail
            if (campaign.getProductDetails() != null) {
                Optional<BigDecimal> pdPctOpt = campaign.getProductDetails().stream()
                        .filter(link -> link.getProductDetail() != null
                                && Objects.equals(link.getProductDetail().getId(), pd.getId()))
                        .map(DiscountCampaignProductDetail::getDiscountPercentage)
                        .filter(Objects::nonNull)
                        .findFirst();
                if (pdPctOpt.isPresent()) {
                    pct = pdPctOpt.get();
                } else {
                    pct = BigDecimal.ZERO; // có link PD nhưng không set % -> xem như 0
                }
            }
        } else if (linkPrd) {
            // link Product: dùng % mặc định ở campaign (vì DiscountCampaignProduct KHÔNG có cột %)
            pct = Optional.ofNullable(campaign.getDiscountPercentage()).orElse(BigDecimal.ZERO);
        } // nếu không link gì thì không vào được hàm này

        if (pct == null) pct = BigDecimal.ZERO;

        PctPick pick = new PctPick();
        pick.pct = pct;
        pick.hasPdInCampaign = linkPd;
        pick.hasProductInCampaign = linkPrd;
        return pick;
    }

    // ======================== Internal DTOs ========================
    @Getter
    @Setter
    private static class Pricing {
        private Long campaignId;
        private BigDecimal sellPrice;         // VND, scale 0
        private BigDecimal percent;           // %
        private Integer percentInt;           // % (int)
        private BigDecimal discountedPrice;   // VND, scale 0
        private boolean hasPdInCampaign;      // campaign có link SPCT?
        private boolean hasProductInCampaign; // campaign có link Product?
    }

    private static class PctPick {
        BigDecimal pct;
        boolean hasPdInCampaign;
        boolean hasProductInCampaign;
    }
}
