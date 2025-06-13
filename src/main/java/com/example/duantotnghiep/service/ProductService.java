package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductDetailResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.model.Product;
import org.springframework.data.domain.Page;
import com.example.duantotnghiep.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse restoreProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    List<ProductDetailResponse> getProductDetailById(Long productId);


    List<ProductResponse> getAllProducts();

    PaginationDTO<ProductSearchResponse> phanTrang(ProductSearchRequest request, Pageable pageable);

    void exportProductToExcel(ProductSearchRequest dto, OutputStream outputStream) throws IOException;
    void exportProductToExcelByIds(List<Long> productIds, OutputStream outputStream) throws IOException;

    PaginationDTO<ProductSearchResponse> getProductRemoved(ProductSearchRequest request, Pageable pageable);
}
