package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.service.ProductService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // GET ALL
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
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

}
