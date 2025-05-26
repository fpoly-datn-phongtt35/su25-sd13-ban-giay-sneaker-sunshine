package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.ProductAttributeResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public InvoiceResponse createInvoice(Long customerId, Long employeeId) {
        Invoice invoice = new Invoice();

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            invoice.setCustomer(customer);
        } else {
            invoice.setCustomer(null); // Khách lẻ
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        invoice.setEmployee(employee);

        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(0); // Active
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoice.setCreatedBy(DEFAULT_USER);
        invoice.setInvoiceDetails(new ArrayList<>());
        invoice.setDescription("Hóa đơn mua tại cửa hàng");
        invoice.setOrderType(1); // 0: cửa hàng, 1: online

        // ✅ Tạo mã hóa đơn tự động
        long count = invoiceRepository.count() + 1;
        String invoiceCode = String.format("INV-%04d", count);
        invoice.setInvoiceCode(invoiceCode);

        invoiceRepository.save(invoice);
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Override
    public List<InvoiceResponse> getAllActiveInvoices() {
        List<Invoice> invoices = invoiceRepository.findByStatus(0);
        return invoiceMapper.toInvoiceResponseList(invoices);
    }

    @Override
    public List<InvoiceDetailResponse> getInvoiceDetails(Long invoiceId) {
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoiceId);
        return invoiceMapper.toInvoiceDetailResponseList(details);
    }

    @Override
    public List<ProductAttributeResponse> getProductAttributes(Long productId) {
        List<ProductDetail> details = productDetailRepository.findByProductId(productId);
        return invoiceMapper.toProductAttributeResponseList(details);
    }

    @Transactional
    @Override
    public void addToCart(Long invoiceId, Long productDetailId, Integer quantity) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("Product detail not found"));

        if (productDetail.getQuantity() < quantity) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        // Load invoice details trực tiếp từ repository để chắc chắn dữ liệu đồng bộ
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoiceId(invoiceId);

        Optional<InvoiceDetail> optionalDetail = invoiceDetails.stream()
                .filter(d -> d.getProductDetail().getId().equals(productDetailId))
                .findFirst();

        InvoiceDetail invoiceDetail;
        if (optionalDetail.isPresent()) {
            invoiceDetail = optionalDetail.get();
            invoiceDetail.setQuantity(invoiceDetail.getQuantity() + quantity);
            invoice.setCreatedDate(LocalDateTime.now());
            invoiceDetail.setUpdatedBy(DEFAULT_USER);
        } else {
            invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductDetail(productDetail);
            invoiceDetail.setQuantity(quantity);
            invoice.setCreatedDate(LocalDateTime.now());
            invoiceDetail.setCreatedBy(DEFAULT_USER);
            invoiceDetail.setStatus(1);  // <-- Thêm dòng này để set trạng thái mặc định
        }

        invoiceDetailRepository.save(invoiceDetail);

        productDetail.setQuantity(productDetail.getQuantity() - quantity);
        productDetailRepository.save(productDetail);

        updateInvoiceTotalPrice(invoice);
    }

    @Transactional
    @Override
    public void removeCartItem(Long invoiceDetailId) {
        InvoiceDetail detail = invoiceDetailRepository.findById(invoiceDetailId)
                .orElseThrow(() -> new RuntimeException("Invoice detail not found"));

        ProductDetail productDetail = detail.getProductDetail();
        productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
        productDetailRepository.save(productDetail);

        Invoice invoice = detail.getInvoice();
        invoice.getInvoiceDetails().remove(detail);

        invoiceDetailRepository.delete(detail);

        updateInvoiceTotalPrice(invoice);
    }

    @Transactional
    @Override
    public void checkout(Long invoiceId, Long paymentMethodId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // TODO: xử lý paymentMethod nếu có

        BigDecimal discount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;

        invoice.setFinalAmount(total.subtract(discount));
        invoice.setStatus(1); // trạng thái đã thanh toán
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setUpdatedBy(DEFAULT_USER);

        invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public void cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Trả lại tồn kho cho các sản phẩm trong hóa đơn
        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            ProductDetail productDetail = detail.getProductDetail();
            productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
            productDetailRepository.save(productDetail);
        }

        invoice.setStatus(2); // trạng thái đã hủy
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setUpdatedBy("admin");

        invoiceRepository.save(invoice);
    }

    private void updateInvoiceTotalPrice(Invoice invoice) {
        BigDecimal total = invoice.getInvoiceDetails().stream()
                .map(d -> d.getProductDetail().getSellPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(total);

        if (invoice.getDiscountAmount() == null) {
            invoice.setDiscountAmount(BigDecimal.ZERO);
        }

        invoice.setFinalAmount(invoice.getTotalAmount().subtract(invoice.getDiscountAmount()));

        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setUpdatedBy(DEFAULT_USER);

        invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceDisplayResponse getInvoiceWithDetails(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoiceId);

        return invoiceMapper.toInvoiceDisplayResponse(invoice, details);
    }

    public List<InvoiceResponse> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAllByOrderByCreatedDateDesc();

        // Giả sử bạn có phương thức chuyển Invoice -> InvoiceResponse
        List<InvoiceResponse> responses = invoices.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return responses;
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

