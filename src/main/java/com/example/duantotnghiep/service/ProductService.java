package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductDetailResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse restoreProduct(Long id);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    Page<ProductDetailResponse> pageProductDetails(int page, int size);

    List<ProductDetailResponse> getProductDetailById(Long productId);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    @Transactional(readOnly = true)
    ProductSearchResponse scanProductToSearchResponse(String rawCode);

    PaginationDTO<ProductSearchResponse> phanTrang(ProductSearchRequest request, Pageable pageable);

    void exportProductToExcel(ProductSearchRequest dto, OutputStream outputStream) throws IOException;
    void exportProductToExcelByIds(List<Long> productIds, OutputStream outputStream) throws IOException;

    PaginationDTO<ProductSearchResponse> getProductRemoved(ProductSearchRequest request, Pageable pageable);

    List<ProductResponse> findProductWithImage();

    List<ProductResponse> findProducts(List<Long> productIds);
}
