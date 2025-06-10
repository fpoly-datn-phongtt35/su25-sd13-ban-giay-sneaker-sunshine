package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.request.VoucherRequest;
import com.example.duantotnghiep.dto.request.VoucherSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.dto.response.VoucherResponse;
import com.example.duantotnghiep.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/search")
    public ResponseEntity<PaginationDTO<VoucherResponse>> searchVouchers(
            @RequestBody VoucherSearchRequest request) {

        System.out.println("status: "+request.getStatus());

        int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 5;

        Pageable pageable = PageRequest.of(page, size);
        PaginationDTO<VoucherResponse> result = voucherService.phanTrangHienThi(request, pageable);

        return ResponseEntity.ok(result);
    }

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

    @PostMapping("/create")
    public ResponseEntity<VoucherResponse> create(@RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.themMoi(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<VoucherResponse> update(@PathVariable Long id, @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.capNhat(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable Long id) {
        try {
            voucherService.deteleVoucherById(id);
            return ResponseEntity.ok("Xóa voucher thành công (status = 0)");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa voucher");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVoucherById(@PathVariable Long id) {
        Optional<VoucherResponse> voucherOpt = voucherService.getOne(id);
        if (voucherOpt.isPresent()) {
            return ResponseEntity.ok(voucherOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy voucher với ID: " + id);
        }
    }

}

