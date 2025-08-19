package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.FavoriteRequest;
import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.request.UpdateAddress;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.StatusCountDTO;
import com.example.duantotnghiep.mapper.InvoiceMapper;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.PromotionSuggestion;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.ProductService;
import com.example.duantotnghiep.service.impl.InvoiceEmailService;
import com.example.duantotnghiep.service.impl.OnlineSaleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/online-sale")
@RequiredArgsConstructor
public class SaleOnlineController {

    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final InvoiceEmailService invoiceEmailService;
    private final InvoiceRepository invoiceRepository;
    private final OnlineSaleServiceImpl onlineSaleService;

    @GetMapping("/online-home")
    public ResponseEntity<List<ProductResponse>> hienThi(){
        List<ProductResponse> productResponseList = productService.findProductWithImage();
        return ResponseEntity.ok(productResponseList);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody InvoiceRequest request) {
        try {
            InvoiceDisplayResponse response = invoiceService.createInvoiceShipCode(request);

            Long invoiceId = response.getInvoice().getId();
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn để gửi email"));

            if (invoice.getCustomer().getEmail() != null && !invoice.getCustomer().getEmail().isEmpty()) {
                invoiceEmailService.sendInvoiceEmail(invoice);
            }

            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", ex.getStatusCode().value());
            error.put("message", ex.getReason());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        } catch (Exception e) {
            //  Lỗi hệ thống khác
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            error.put("message", "Lỗi thanh toán: " + e.getMessage());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping()
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.getAllProducts(pageable);
        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/suggest-promotion/{customerId}")
    public ResponseEntity<PromotionSuggestion> suggestPromotion(@PathVariable Long customerId) {
        PromotionSuggestion suggestion = invoiceService.getSuggestedPromotion(customerId);
        return ResponseEntity.ok(suggestion);
    }

    @PostMapping("/{invoiceId}/pay")
    public ResponseEntity<String> payInvoice(@PathVariable Long invoiceId) {
        invoiceService.processInvoicePayment(invoiceId);
        return ResponseEntity.ok("Thanh toán hóa đơn thành công và xét duyệt voucher.");
    }

    @PutMapping("/huy-don-va-hoan-tien")
    public ResponseEntity<?> huyDonVaHoanTien(
            @RequestParam Long invoiceId,
            @RequestParam String statusDetail,
            @RequestParam String note,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam Boolean isPaid
    ) {
        try {
            onlineSaleService.huyDonVaHoanTienClient(invoiceId, statusDetail, note, paymentMethod,isPaid);
            return ResponseEntity.ok("Hủy đơn và hoàn tiền thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi máy chủ");
        }
    }

    @PutMapping("/cap-nhat-dia-chi")
    public ResponseEntity<String> capNhatDiaChi(@RequestBody UpdateAddress request) {
        onlineSaleService.updateAddressShipping(request);
        return ResponseEntity.ok("Cập nhật địa chỉ giao hàng thành công.");
    }

    @PostMapping("/favorites")
    public ResponseEntity<List<ProductResponse>> getFavoriteProducts(@RequestBody FavoriteRequest request) {
        List<Long> productIds = request.getProductIds();

        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<ProductResponse> products = productService.findProducts(productIds);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/count-by-status")
    public ResponseEntity<List<StatusCountDTO>> getCountByStatus() {
        return ResponseEntity.ok(onlineSaleService.getCountByStatus());
    }



}
