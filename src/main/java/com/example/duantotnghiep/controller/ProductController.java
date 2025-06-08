package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.service.ProductService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.http.HttpServletResponse;

import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.service.ProductService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // GET ALL
    @GetMapping("")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productResponses = productService.getAllProducts(pageable);
        return ResponseEntity.ok(productResponses);
    }


    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct( @ModelAttribute ProductRequest request) {

        return ResponseEntity.ok(productService.createProduct(request));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @ModelAttribute ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
       try {
           productService.deleteProduct(id);
           return ResponseEntity.ok("Xóa thành công");
       }catch (Exception e){
           throw new RuntimeException("Lỗi xóa");
       }
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

        int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 5;

        Pageable pageable = PageRequest.of(page, size);
        PaginationDTO<ProductSearchResponse> result = productService.phanTrang(request, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping ("/export")
    public void exportExcel(@RequestBody ProductSearchRequest dto, HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=categories.xlsx");
            productService.exportProductToExcel(dto, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            System.out.println("Xuất Excel thất bại: {}");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // status 500
        }
    }

}
