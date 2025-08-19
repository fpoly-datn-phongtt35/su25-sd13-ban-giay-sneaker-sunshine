package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.ProductRatingView;
import com.example.duantotnghiep.repository.FavoriteProductRepository;
import com.example.duantotnghiep.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class RatingServiceIpml implements RatingService {
    private final FavoriteProductRepository favoriteProductRepository;


    @Override
    public Map<String, Object> getRatingOfProduct(Long productId) {
        var viewOpt = favoriteProductRepository.findRatingByProductId(productId);

        if (viewOpt.isEmpty()) {
            return Map.of(
                    "productId", productId,
                    "avgRating", 0.0,
                    "totalReviews", 0,
                    "distribution", Map.of(1,0,2,0,3,0,4,0,5,0)
            );
        }

        var v = viewOpt.get();
        Map<Integer, Long> dist = new java.util.LinkedHashMap<>();
        dist.put(1, v.getStar1());
        dist.put(2, v.getStar2());
        dist.put(3, v.getStar3());
        dist.put(4, v.getStar4());
        dist.put(5, v.getStar5());

        return Map.of(
                "productId", v.getProductId(),
                "avgRating", round1(v.getAvgRating()),
                "totalReviews", v.getTotalReviews(),
                "distribution", dist
        );
    }

    @Override
    public List<Map<String, Object>> getRatingsOfProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        var list = favoriteProductRepository.findRatingsByProductIds(productIds);
        Map<Long, ProductRatingView> byId = new java.util.HashMap<>();
        for (var v : list) byId.put(v.getProductId(), v);

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Long pid : productIds) {
            var v = byId.get(pid);
            if (v == null) {
                result.add(Map.of(
                        "productId", pid,
                        "avgRating", 0.0,
                        "totalReviews", 0,
                        "distribution", Map.of(1,0,2,0,3,0,4,0,5,0)
                ));
            } else {
                result.add(Map.of(
                        "productId", v.getProductId(),
                        "avgRating", round1(v.getAvgRating()),
                        "totalReviews", v.getTotalReviews(),
                        "distribution", Map.of(
                                1, v.getStar1(),
                                2, v.getStar2(),
                                3, v.getStar3(),
                                4, v.getStar4(),
                                5, v.getStar5()
                        )
                ));
            }
        }
        return result;
    }

    private double round1(Double d) {
        if (d == null) return 0.0;
        return Math.round(d * 10.0) / 10.0; // làm tròn 1 chữ số thập phân (vd 4.3)
    }
}
