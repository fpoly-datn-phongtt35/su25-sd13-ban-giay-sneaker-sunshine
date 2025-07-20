package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.PaymentSummary;
import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.dto.response.InvoiceCheckoutResponse;
import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.InvoiceWithVnpayResponse;
import com.example.duantotnghiep.dto.response.InvoiceWithZaloPayResponse;
import com.example.duantotnghiep.dto.response.ProductAttributeResponse;
import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceService {

    @Transactional
    InvoiceResponse createEmptyInvoice();

    List<CustomerResponse> findCustomersByPhonePrefix(String phonePrefix);

    @Transactional
    CustomerResponse createQuickCustomer(String phone, String name, String email);

    PaymentSummary calculatePayment(Long invoiceId, BigDecimal amountGiven);

    @Transactional
    void checkout(Long invoiceId);

    @Transactional
    void clearCart(Long invoiceId);

    InvoiceDisplayResponse getInvoiceWithDetails(Long invoiceId);

    Page<InvoiceDisplayResponse> getInvoiceDisplays(Pageable pageable);

    Page<InvoiceResponse> searchInvoices(String keyword, Integer status,
                                         LocalDate createdDate,
                                         Pageable pageable);

    Invoice findByInvoiceCode(String code);

    List<InvoiceDisplayResponse> getAllInvoicesWithDetails();

    Invoice findById(Long id);

    List<InvoiceDisplayResponse> getInvoicesWithDetailsByIds(List<Long> ids);

    @Transactional
    InvoiceDisplayResponse createInvoice(InvoiceRequest request);


    @Transactional
    InvoiceWithZaloPayResponse createInvoiceAndZaloPay(InvoiceRequest request) throws Exception;

    InvoiceDisplayResponse createInvoiceShipCode(InvoiceRequest request);

    void updateInvoiceStatusByAppTransId(String appTransId, Integer status, Integer statusDetail,Boolean isPaid);

    @Transactional
    InvoiceWithVnpayResponse createInvoiceAndVnpay(InvoiceRequest request) throws Exception;
    void updateStatusIfPaid(String appTransId) throws Exception;

    Page<InvoiceResponse> searchInvoices(Integer status,Integer orderType, LocalDateTime createdFrom, LocalDateTime createdTo, String phone, String code, Pageable pageable);

}
