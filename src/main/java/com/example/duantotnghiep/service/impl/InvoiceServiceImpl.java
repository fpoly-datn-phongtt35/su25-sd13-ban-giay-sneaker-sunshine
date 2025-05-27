package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.PaymentSummary;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.mapper.InvoiceMapper;
import com.example.duantotnghiep.model.*;
import com.example.duantotnghiep.repository.*;
import com.example.duantotnghiep.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InvoiceMapper invoiceMapper;

    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_CUSTOMER_NAME = "Khách lẻ";

    @Transactional
    @Override
    public InvoiceResponse createEmptyInvoice(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setEmployee(employee);
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(0);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoice.setCreatedBy(DEFAULT_USER);
        invoice.setDescription("Hóa đơn bán tại quầy");
        invoice.setOrderType(0); // 0: tại quầy

        invoiceRepository.save(invoice);
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    private String generateInvoiceCode() {
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%04d", count);
    }

    @Transactional
    @Override
    public InvoiceDetailResponse addProductToInvoice(Long invoiceId, Long productDetailId, int quantity) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại"));

        if (productDetail.getQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng tồn kho");
        }

        InvoiceDetail invoiceDetail = invoiceDetailRepository.findByInvoiceIdAndProductDetailId(invoiceId, productDetailId)
                .orElseGet(() -> {
                    InvoiceDetail newInvoiceDetail = new InvoiceDetail();
                    newInvoiceDetail.setInvoice(invoice);
                    newInvoiceDetail.setProductDetail(productDetail);
                    newInvoiceDetail.setQuantity(0);

                    // Tự động sinh mã dựa trên số lượng bản ghi hiện tại + 1
                    long count = invoiceDetailRepository.count() + 1;
                    String invoiceDetailCode = String.format("INV-D-%04d", count);
                    newInvoiceDetail.setInvoiceCodeDetail(invoiceDetailCode);

                    return newInvoiceDetail;
                });

        invoiceDetail.setQuantity(invoiceDetail.getQuantity() + quantity);
        invoiceDetail.setCreatedBy(DEFAULT_USER);
        invoiceDetail.setStatus(1);
        invoiceDetail.setCreatedDate(LocalDateTime.now());
        invoiceDetailRepository.save(invoiceDetail);

        productDetail.setQuantity(productDetail.getQuantity() - quantity);
        productDetailRepository.save(productDetail);

        updateInvoiceTotal(invoice);
        return invoiceMapper.toInvoiceDetailResponse(invoiceDetail);
    }


    private void updateInvoiceTotal(Invoice invoice) {
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());
        BigDecimal total = details.stream()
                .map(d -> d.getProductDetail().getSellPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(total);
        invoice.setFinalAmount(total); // mặc định chưa giảm giá
        invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public CustomerResponse findOrCreateQuickCustomer(Long invoiceId, String phone, String name) {
        // Tìm hoặc tạo khách hàng
        Customer customer = customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomerName(name);
                    newCustomer.setPhone(phone);

                    long count = customerRepository.count() + 1;
                    String customerCode = String.format("CUS%02d", count);
                    newCustomer.setCustomerCode(customerCode);

                    Role role = new Role();
                    role.setId(2L);
                    newCustomer.setRole(role);

                    newCustomer.setCountry("Việt Nam");
                    newCustomer.setStatus(1);
                    newCustomer.setCreatedDate(LocalDateTime.now());
                    newCustomer.setCreatedBy(DEFAULT_USER);

                    return customerRepository.save(newCustomer);
                });

        // Gán khách hàng vào hóa đơn
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        invoice.setCustomer(customer);
        invoice.setUpdatedBy(DEFAULT_USER);
        invoice.setUpdatedDate(LocalDateTime.now());
        invoiceRepository.save(invoice);

        return invoiceMapper.toCustomerResponse(customer);
    }


    @Override
    public PaymentSummary calculatePayment(Long invoiceId, BigDecimal amountGiven) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        BigDecimal finalAmount = invoice.getFinalAmount();
        if (amountGiven.compareTo(finalAmount) < 0) {
            throw new RuntimeException("Số tiền đưa không đủ");
        }

        BigDecimal change = amountGiven.subtract(finalAmount);
        return new PaymentSummary(finalAmount, amountGiven, change);
    }

    @Transactional
    @Override
    public void checkout(Long invoiceId, Long customerId, BigDecimal discountAmount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
            invoice.setCustomer(customer);
        }

        invoice.setDiscountAmount(discountAmount);
        BigDecimal finalAmount = invoice.getTotalAmount().subtract(discountAmount);
        invoice.setFinalAmount(finalAmount);
        invoice.setStatus(1);
        invoice.setOrderType(1);
        invoice.setUpdatedBy(DEFAULT_USER);
        invoice.setCreatedDate(LocalDateTime.now());

        invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public void clearCart(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Trả lại tồn kho cho từng sản phẩm trong giỏ
        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            ProductDetail productDetail = detail.getProductDetail();
            productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
            productDetailRepository.save(productDetail);
        }

        // Xóa tất cả chi tiết hóa đơn
        invoiceDetailRepository.deleteAll(invoice.getInvoiceDetails());

        // Làm trống danh sách chi tiết
        invoice.getInvoiceDetails().clear();

        // Reset tổng tiền về 0
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoiceRepository.save(invoice);
    }

    /**
     * Tạo hóa đơn(bán tại quầy)
     */

    @Override
    public InvoiceDisplayResponse getInvoiceWithDetails(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoiceId);

        return invoiceMapper.toInvoiceDisplayResponse(invoice, details);
    }

    @Override
    public Page<InvoiceDisplayResponse> getInvoiceDisplays(Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findAll(pageable);

        List<InvoiceDisplayResponse> displayResponses = invoices.stream()
                .map(invoice -> {
                    List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());

                    InvoiceResponse invoiceResponse = convertToResponse(invoice);

                    List<InvoiceDetailResponse> detailResponses = details.stream()
                            .map(this::convertToDetailResponse)
                            .collect(Collectors.toList());

                    return new InvoiceDisplayResponse(invoiceResponse, detailResponses);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(displayResponses, pageable, invoices.getTotalElements());
    }

    private InvoiceResponse convertToResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceCode(invoice.getInvoiceCode());
        if (invoice.getCustomer() != null) {
            response.setPhone(invoice.getCustomer().getPhone());
            response.setCustomerName(invoice.getCustomer().getCustomerName());
        }
        response.setCreatedDate(invoice.getCreatedDate());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setDiscountAmount(invoice.getDiscountAmount());
        response.setFinalAmount(invoice.getFinalAmount());
        response.setDescription(invoice.getDescription());
        response.setOrderType(invoice.getOrderType());
        response.setStatus(invoice.getStatus());
        response.setCreatedBy(invoice.getCreatedBy());
        response.setUpdatedBy(invoice.getUpdatedBy());
        response.setUpdatedDate(invoice.getUpdatedDate());

        if (invoice.getEmployee() != null) {
            response.setEmployeeName(invoice.getEmployee().getEmployeeName());
        }

        return response;
    }

    private InvoiceDetailResponse convertToDetailResponse(InvoiceDetail detail) {
        InvoiceDetailResponse response = new InvoiceDetailResponse();
        response.setId(detail.getId());
        response.setProductName(detail.getProductDetail().getProduct().getProductName());
        response.setQuantity(detail.getQuantity());
        response.setPrice(detail.getInvoice().getTotalAmount());
        return response;
    }

    @Override
    public Page<InvoiceResponse> searchInvoices(String keyword, Integer status,
                                                LocalDate createdDate,
                                                Pageable pageable) {
        // Nếu keyword là chuỗi rỗng, gán null để tránh lọc
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // Chuyển createdDate thành startOfDay và startOfNextDay nếu có
        LocalDateTime startOfDay = null;
        LocalDateTime startOfNextDay = null;
        if (createdDate != null) {
            startOfDay = createdDate.atStartOfDay(); // 00:00
            startOfNextDay = createdDate.plusDays(1).atStartOfDay(); // ngày tiếp theo 00:00
        }

        // Gọi repository để tìm kiếm
        Page<Invoice> page = invoiceRepository.searchByKeywordStatusAndCreatedDate(
                keyword, status, startOfDay, startOfNextDay, pageable
        );

        // Chuyển sang DTO
        List<InvoiceResponse> dtos = invoiceMapper.toInvoiceResponseList(page.getContent());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public Invoice findByInvoiceCode(String code) {
        return invoiceRepository.findByInvoiceCode(code);
    }

    @Override
    public List<InvoiceDisplayResponse> getAllInvoicesWithDetails() {
        List<Invoice> invoices = invoiceRepository.findAll();

        return invoices.stream()
                .map(invoice -> {
                    List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());
                    return invoiceMapper.toInvoiceDisplayResponse(invoice, details);
                })
                .collect(Collectors.toList());
    }

}

