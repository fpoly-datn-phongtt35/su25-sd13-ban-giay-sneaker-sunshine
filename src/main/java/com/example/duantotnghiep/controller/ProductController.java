package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductDetailResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.service.ProductService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // GET ALL
    @GetMapping("/hien-thi")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> productResponses = productService.getAllProducts();
        return ResponseEntity.ok(productResponses);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/product-detail/{idProduct}")
    public ResponseEntity<List<ProductDetailResponse>> getProductDetailById(@PathVariable Long idProduct) {
        return ResponseEntity.ok(productService.getProductDetailById(idProduct));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct( @ModelAttribute ProductRequest request) {
        System.out.println("Tổng số ảnh: " + request.getProductImages().size());
        return ResponseEntity.ok(productService.createProduct(request));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @ModelAttribute ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @GetMapping("/qrcode/{code}")
    public void getQRCode(@PathVariable String code, HttpServletResponse response) throws Exception {
        String qrContent = code; // hoặc gắn URL: "https://sunshine-shop.vn/product/" + code
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(qrContent, BarcodeFormat.QR_CODE, 250, 250);

        response.setContentType("image/png");
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", response.getOutputStream());
    }

    @PostMapping("/search")
    public ResponseEntity<PaginationDTO<ProductSearchResponse>> searchProducts(
            @RequestBody ProductSearchRequest request) {
        System.out.println("from: "+request.getCreatedFrom());
        System.out.println("To: "+request.getCreatedTo());

        int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 5;

        Pageable pageable = PageRequest.of(page, size);
        PaginationDTO<ProductSearchResponse> result = productService.phanTrang(request, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/inactive")
    public ResponseEntity<PaginationDTO<ProductSearchResponse>> getProductRemoved(
            @RequestBody ProductSearchRequest request) {

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 8;

        // Tạo Pageable
        Pageable pageable = PageRequest.of(page, size);

        // Gọi service
        PaginationDTO<ProductSearchResponse> result = productService.getProductRemoved(request, pageable);

        return ResponseEntity.ok(result);
    }

    // Xuất Excel theo danh sách productIds
    @PostMapping("/export-excel/by-ids")
    public void exportExcelByIds(@RequestBody List<Long> productIds, HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=products-by-ids.xlsx");

            productService.exportProductToExcelByIds(productIds, response.getOutputStream());

            response.flushBuffer();
        } catch (IOException e) {
            System.err.println("Xuất Excel theo IDs thất bại: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Xuất Excel theo filter tìm kiếm
    @PostMapping("/export-excel/by-filter")
    public void exportExcelByFilter(@RequestBody ProductSearchRequest filter, HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=products-by-filter.xlsx");

            productService.exportProductToExcel(filter, response.getOutputStream());

            response.flushBuffer();
        } catch (IOException e) {
            System.err.println("Xuất Excel theo filter thất bại: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/online-home")
    public ResponseEntity<List<ProductResponse>> hienThi(){
        List<ProductResponse> productResponseList = productService.findProductWithImage();
        return ResponseEntity.ok(productResponseList);
    }

}
