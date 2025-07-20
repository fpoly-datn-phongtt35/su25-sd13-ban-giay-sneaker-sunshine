package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.OrderRequest;
import com.example.duantotnghiep.dto.response.InvoiceOnlineResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.OrderStatusHistoryResponse;
import com.example.duantotnghiep.dto.response.StatusCountResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.impl.OnlineSaleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/online-sales")
@RequiredArgsConstructor
public class OnlineSalesController {
    private final InvoiceService invoiceService;
    private final OnlineSaleServiceImpl onlineSaleService;

    @GetMapping("/search")
    public ResponseEntity<Page<InvoiceResponse>> searchInvoices(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        System.out.println("status: "+status);
        LocalDateTime from = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime to = createdTo != null ? createdTo.atTime(LocalTime.MAX) : null;
        Pageable pageable = PageRequest.of(page,size);

        Page<InvoiceResponse> result = invoiceService.searchInvoices(status,1, from, to, phone, code, pageable);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/chuyen-trang-thai")
    public ResponseEntity<String> chuyenTrangThai(@RequestParam("invoiceId") Long invoiceId,@RequestParam("statusDetail") String nextKey) {
        try {
            onlineSaleService.chuyenTrangThai(invoiceId,nextKey);
            return ResponseEntity.ok("Chuyển trạng thái đơn hàng thành công.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Lỗi: " + ex.getMessage());
        }
    }

    @GetMapping("/get-order")
    public ResponseEntity<InvoiceOnlineResponse> getOrderOnline(@RequestParam("invoiceId") Long invoiceId){
        InvoiceOnlineResponse response = onlineSaleService.getOrder(invoiceId);
        return ResponseEntity.ok(response);
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
            onlineSaleService.huyDonVaHoanTien(invoiceId, statusDetail, note, paymentMethod,isPaid);
            return ResponseEntity.ok("Hủy đơn và hoàn tiền thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi máy chủ");
        }
    }

    @PutMapping("/failed-shipping")
    public ResponseEntity<?> giaoHangThatBaiVaHoanTien(
            @RequestParam Long invoiceId,
            @RequestParam String statusDetail,
            @RequestParam String note,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam Boolean isPaid
    ) {
        try {
            onlineSaleService.giaoHangThatBaiVaHoanTien(invoiceId, statusDetail, note, paymentMethod,isPaid);
            return ResponseEntity.ok("Giao hàng thất bại và hoàn tiền thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi máy chủ");
        }
    }

    @GetMapping("/count-by-status")
    public ResponseEntity<List<StatusCountResponse>> getCountByStatus() {
        return ResponseEntity.ok(onlineSaleService.getCountByStatusDetail());
    }

    @GetMapping("/get-order-customer")
    public ResponseEntity<List<InvoiceOnlineResponse>> getOrderCustomerOnline2(@RequestParam("statusDetail") Integer statusDetail){
        System.out.println("status: "+statusDetail);
        List<InvoiceOnlineResponse> response = onlineSaleService.getOrderByCustomer2(statusDetail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-order-customer-detail")
    public ResponseEntity<InvoiceOnlineResponse> getOrderCustomerOnline(@RequestParam("invoiceId") Long invoiceId){
        InvoiceOnlineResponse response = onlineSaleService.getOrderByCustomer(invoiceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-order-history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getOrderHistory(@Param("invoiceId") Long invoiceId){
        List<OrderStatusHistoryResponse> list = onlineSaleService.getOrderStatusHistory(invoiceId);
        return ResponseEntity.ok(list);
    }

}
