package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.ZaloPayResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.repository.CustomerRepository;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.ProductService;
import com.example.duantotnghiep.service.impl.ZaloPayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/online-sale")
@RequiredArgsConstructor
public class SaleOnlineController {
    private final ProductService productService;
    private final InvoiceService invoiceService;


    @GetMapping("/online-home")
    public ResponseEntity<List<ProductResponse>> hienThi(){
        List<ProductResponse> productResponseList = productService.findProductWithImage();
        return ResponseEntity.ok(productResponseList);
    }

//    @PostMapping("/checkout")
//    public ResponseEntity<?> checkout(@RequestBody InvoiceRequest request) {
//        try {
//            InvoiceDisplayResponse response = invoiceService.createInvoice(request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("❌ Lỗi thanh toán: " + e.getMessage());
//        }
//    }


}
