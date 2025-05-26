package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.CreateInvoiceRequest;
import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.ProductAttributeResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.service.impl.InvoiceServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/counter-sales")
@RequiredArgsConstructor
@Validated
public class CounterSalesController {

    private final InvoiceServiceImpl invoiceService;

    // Tạo hóa đơn mới
    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody @Valid CreateInvoiceRequest request) {
        InvoiceResponse invoiceResponse = invoiceService.createInvoice(request.getCustomerId(), request.getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceResponse);
    }

    // Lấy danh sách hóa đơn active (status = 1)
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getActiveInvoices() {
        List<InvoiceResponse> invoices = invoiceService.getAllActiveInvoices();
        return ResponseEntity.ok(invoices);
    }

    // Lấy chi tiết hóa đơn theo invoiceId
    @GetMapping("/invoices/{invoiceId}/details")
    public ResponseEntity<List<InvoiceDetailResponse>> getInvoiceDetails(@PathVariable @Positive Long invoiceId) {
        List<InvoiceDetailResponse> details = invoiceService.getInvoiceDetails(invoiceId);
        return ResponseEntity.ok(details);
    }

    // Lấy thuộc tính sản phẩm theo productId
    @GetMapping("/products/{productId}/attributes")
    public ResponseEntity<List<ProductAttributeResponse>> getProductAttributes(@PathVariable @Positive Long productId) {
        List<ProductAttributeResponse> attributes = invoiceService.getProductAttributes(productId);
        return ResponseEntity.ok(attributes);
    }


    // Thêm sản phẩm vào giỏ hàng (invoice)
    @PostMapping("/invoices/{invoiceId}/cart")
    public ResponseEntity<Void> addToCart(
            @PathVariable @Positive Long invoiceId,
            @RequestParam @Positive Long productDetailId,
            @RequestParam @Positive Integer quantity) {
        invoiceService.addToCart(invoiceId, productDetailId, quantity);
        return ResponseEntity.ok().build();
    }

    // Xóa sản phẩm trong giỏ hàng theo invoiceDetailId
    @DeleteMapping("/cart/{invoiceDetailId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable @Positive Long invoiceDetailId) {
        invoiceService.removeCartItem(invoiceDetailId);
        return ResponseEntity.noContent().build();
    }

    // Thanh toán hóa đơn
    @PostMapping("/invoices/{invoiceId}/checkout")
    public ResponseEntity<Void> checkout(
            @PathVariable @Positive Long invoiceId,
            @RequestParam @Positive Long paymentMethodId) {
        invoiceService.checkout(invoiceId, paymentMethodId);
        return ResponseEntity.ok().build();
    }

    // Hủy hóa đơn
    @PostMapping("/invoices/{invoiceId}/cancel")
    public ResponseEntity<Void> cancelInvoice(@PathVariable @Positive Long invoiceId) {
        invoiceService.cancelInvoice(invoiceId);
        return ResponseEntity.ok().build();
    }
}

