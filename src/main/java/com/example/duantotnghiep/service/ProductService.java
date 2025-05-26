package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

}
