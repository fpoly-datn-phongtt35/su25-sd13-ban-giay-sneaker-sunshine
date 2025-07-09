package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.ProductService;
import com.example.duantotnghiep.service.impl.InvoiceEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/online-sale")
@RequiredArgsConstructor
public class SaleOnlineController {
    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final InvoiceEmailService invoiceEmailService;
    private final InvoiceRepository invoiceRepository;


    @GetMapping("/online-home")
    public ResponseEntity<List<ProductResponse>> hienThi(){
        List<ProductResponse> productResponseList = productService.findProductWithImage();
        return ResponseEntity.ok(productResponseList);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody InvoiceRequest request) {
        try {
            // 1. Tạo hóa đơn
            InvoiceDisplayResponse response = invoiceService.createInvoice(request);

            // 2. Lấy lại Invoice thật từ DB
            Long invoiceId = response.getInvoice().getId();
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn để gửi email"));

            // 3. Gửi email nếu có địa chỉ email
            if (invoice.getCustomer().getEmail() != null && !invoice.getCustomer().getEmail().isEmpty()) {
                invoiceEmailService.sendInvoiceEmail(invoice);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Lỗi thanh toán: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/hien-thi")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> productResponses = productService.getAllProducts();
        return ResponseEntity.ok(productResponses);
    }


}
