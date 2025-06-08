package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.InvoiceQRCodeRequest;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.repository.InvoiceDetailRepository;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.impl.ExcelExportService;
import com.example.duantotnghiep.service.impl.InvoiceExportService;
import com.example.duantotnghiep.service.impl.InvoiceQRService;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceExportService invoiceExportService;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ExcelExportService excelExportService;
    private final InvoiceQRService invoiceQRService;

    @GetMapping
    public ResponseEntity<Page<InvoiceDisplayResponse>> getInvoiceDisplays(
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<InvoiceDisplayResponse> page = invoiceService.getInvoiceDisplays(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}/detail")
    public InvoiceDisplayResponse getInvoiceWithDetails(@PathVariable Long id) {
        return invoiceService.getInvoiceWithDetails(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InvoiceResponse>> searchInvoices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String createdDate, // nhận String
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDate parsedDate = null;

        try {
            if (createdDate != null && !createdDate.trim().isEmpty()) {
                parsedDate = LocalDate.parse(createdDate);
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(null);
        }

        Page<InvoiceResponse> invoices = invoiceService.searchInvoices(keyword, status, parsedDate, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceCode}/export")
    public void exportInvoice(
            @PathVariable String invoiceCode,
            HttpServletResponse response) throws IOException, DocumentException {

        Invoice invoice = invoiceService.findByInvoiceCode(invoiceCode);
        if (invoice == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invoice not found");
            return;
        }

        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());
        invoiceExportService.exportInvoice(response, invoice, details);
    }

    @GetMapping("/export-all-excel")
    public void exportAllInvoicesToExcel(HttpServletResponse response) throws IOException {
        List<InvoiceDisplayResponse> allInvoices = invoiceService.getAllInvoicesWithDetails();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"danh_sach_hoa_don.xlsx\"");

        ByteArrayInputStream excelStream = excelExportService.exportInvoicesToExcel(allInvoices);
        IOUtils.copy(excelStream, response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/qr-scan")
    public ResponseEntity<Object> scanQRCode(@RequestBody InvoiceQRCodeRequest request) {
        String invoiceCode = request.getInvoiceCode();
        if (invoiceCode == null || invoiceCode.isBlank()) {
            return ResponseEntity.badRequest().body("Mã hóa đơn (invoiceCode) không được để trống");
        }

        return invoiceQRService.getInvoiceByInvoiceCode(invoiceCode)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Không tìm thấy hóa đơn từ mã QR"));
    }

}
