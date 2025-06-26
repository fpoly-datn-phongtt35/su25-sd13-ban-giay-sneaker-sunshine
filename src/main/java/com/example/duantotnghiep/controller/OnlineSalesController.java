package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/admin/online-sales")
@RequiredArgsConstructor
public class OnlineSalesController {
    private final InvoiceService invoiceService;

    @GetMapping("/search")
    public ResponseEntity<Page<InvoiceResponse>> searchInvoices(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime from = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime to = createdTo != null ? createdTo.atTime(LocalTime.MAX) : null;
        Pageable pageable = PageRequest.of(page,size);

        Page<InvoiceResponse> result = invoiceService.searchInvoices(status,orderType, from, to, phone, code, pageable);
        return ResponseEntity.ok(result);
    }
}
