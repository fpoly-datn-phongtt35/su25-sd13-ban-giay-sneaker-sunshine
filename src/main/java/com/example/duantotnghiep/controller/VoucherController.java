package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.response.VoucherResponse;
import com.example.duantotnghiep.service.VoucherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/valid")
    public ResponseEntity<List<VoucherResponse>> getValidVouchers() {
        List<VoucherResponse> vouchers = voucherService.getValidVouchers();
        return ResponseEntity.ok(vouchers);
    }

//    @GetMapping("/by-customer/{customerId}")
//    public ResponseEntity<List<VoucherResponse>> getVouchersByCustomerId(@PathVariable Long customerId) {
//        List<VoucherResponse> vouchers = voucherService.getVoucherResponsesByCustomerId(customerId);
//        return ResponseEntity.ok(vouchers);
//    }

//    @GetMapping("/invoice/{invoiceId}")
//    public ResponseEntity<VoucherResponse> getVoucherByInvoiceId(@PathVariable Long invoiceId) {
//        VoucherResponse response = voucherService.getVoucherByInvoiceId(invoiceId);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<?> getVouchersByInvoiceId(@PathVariable Long invoiceId) {
        try {
            List<VoucherResponse> responseList = voucherService.getVouchersByCustomerInInvoice(invoiceId);
            return ResponseEntity.ok(responseList);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }


}

