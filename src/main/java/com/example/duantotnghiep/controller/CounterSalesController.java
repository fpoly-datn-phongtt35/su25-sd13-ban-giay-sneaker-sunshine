package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.PaymentSummary;
import com.example.duantotnghiep.dto.request.CreateInvoiceRequest;
import com.example.duantotnghiep.dto.response.CustomerResponse;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/counter-sales")
@RequiredArgsConstructor
@Validated
public class CounterSalesController {

    private final InvoiceServiceImpl invoiceService;

    /**
     * Tạo hóa đơn rỗng (bán tại quầy)
     */
    @PostMapping("/create-empty")
    public ResponseEntity<InvoiceResponse> createEmptyInvoice(@RequestParam Long employeeId) {
        InvoiceResponse response = invoiceService.createEmptyInvoice(employeeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Thêm sản phẩm vào hóa đơn
     */
    @PostMapping("/{invoiceId}/add-product")
    public ResponseEntity<InvoiceDetailResponse> addProductToInvoice(
            @PathVariable Long invoiceId,
            @RequestParam Long productDetailId,
            @RequestParam int quantity) {
        InvoiceDetailResponse response = invoiceService.addProductToInvoice(invoiceId, productDetailId, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm hoặc tạo mới nhanh khách hàng theo số điện thoại
     */
    @PostMapping("/assign-customer-to-invoice")
    public ResponseEntity<CustomerResponse> assignCustomerToInvoice(
            @RequestParam Long invoiceId,
            @RequestParam String phone,
            @RequestParam(required = false) String name) {

        CustomerResponse response = invoiceService.findOrCreateQuickCustomer(invoiceId, phone, name != null ? name : "Khách lẻ");
        return ResponseEntity.ok(response);
    }

    /**
     * Tính tiền cần trả và tiền thừa
     */
    @GetMapping("/{invoiceId}/calculate-payment")
    public ResponseEntity<PaymentSummary> calculatePayment(
            @PathVariable Long invoiceId,
            @RequestParam BigDecimal amountGiven) {
        PaymentSummary summary = invoiceService.calculatePayment(invoiceId, amountGiven);
        return ResponseEntity.ok(summary);
    }

    /**
     * Thanh toán hóa đơn
     */
    @PostMapping("/{invoiceId}/checkout")
    public ResponseEntity<?> checkoutInvoice(
            @PathVariable Long invoiceId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") BigDecimal discountAmount) {
        invoiceService.checkout(invoiceId, customerId, discountAmount);
        return ResponseEntity.ok("Thanh toán thành công");
    }

    /**
     * Xóa giỏ hàng (hóa đơn) theo ID
     */
    @DeleteMapping("/{invoiceId}/clear-cart")
    public ResponseEntity<?> clearCart(@PathVariable Long invoiceId) {
        invoiceService.clearCart(invoiceId);
        return ResponseEntity.ok("Giỏ hàng đã được xóa");
    }
}

