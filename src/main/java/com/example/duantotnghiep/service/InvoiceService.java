package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.ProductAttributeResponse;
import com.example.duantotnghiep.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceService {

    @Transactional
    InvoiceResponse createInvoice(Long customerId, Long employeeId);

    List<InvoiceResponse> getAllActiveInvoices();

    List<InvoiceDetailResponse> getInvoiceDetails(Long invoiceId);

    List<ProductAttributeResponse> getProductAttributes(Long productId);

    @Transactional
    void addToCart(Long invoiceId, Long productDetailId, Integer quantity);

    @Transactional
    void removeCartItem(Long invoiceDetailId);

    @Transactional
    void checkout(Long invoiceId, Long paymentMethodId);

    @Transactional
    void cancelInvoice(Long invoiceId);

    InvoiceDisplayResponse getInvoiceWithDetails(Long invoiceId);

    Page<InvoiceDisplayResponse> getInvoiceDisplays(Pageable pageable);

    Page<InvoiceResponse> searchInvoices(String keyword, Integer status,
                                         LocalDate createdDate,
                                         Pageable pageable);

    Invoice findByInvoiceCode(String code);

    List<InvoiceDisplayResponse> getAllInvoicesWithDetails();
}
