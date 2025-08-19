package com.example.duantotnghiep.service;

import java.util.List;
import java.util.Map;

public interface RatingService {
    Map<String, Object> getRatingOfProduct(Long productId);

    List<Map<String, Object>> getRatingsOfProducts(List<Long> productIds);
}
