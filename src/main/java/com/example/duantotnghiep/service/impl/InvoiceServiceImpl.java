package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.PaymentSummary;
import com.example.duantotnghiep.dto.response.CustomerResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;

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

    private void updateInvoiceTotal(Invoice invoice) {
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());
        BigDecimal total = details.stream()
                .map(d -> d.getProductDetail().getSellPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(total);
        invoice.setFinalAmount(total); // mặc định chưa giảm giá
        invoiceRepository.save(invoice);
    }

    @Override
    public List<CustomerResponse> findCustomersByPhonePrefix(String phonePrefix) {
        List<Customer> customers = customerRepository.findByPhoneStartingWith(phonePrefix);
        if (customers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy khách hàng với số điện thoại bắt đầu bằng: " + phonePrefix);
        }
        return customers.stream()
                .map(invoiceMapper::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CustomerResponse createQuickCustomer(String phone, String name) {
        // Tạo khách hàng mới
        Customer newCustomer = new Customer();
        newCustomer.setCustomerName(name != null ? name : "Khách lẻ");
        newCustomer.setPhone(phone);

        long count = customerRepository.count() + 1;
        newCustomer.setCustomerCode(String.format("CUS%02d", count));
        newCustomer.setStatus(1);
        newCustomer.setCreatedDate(LocalDateTime.now());
        newCustomer.setCreatedBy(DEFAULT_USER);

        Customer customer = customerRepository.save(newCustomer);

        // Bỏ phần gán khách hàng vào hóa đơn

        return invoiceMapper.toCustomerResponse(customer);
    }

    @Transactional
    public void assignCustomer(Long invoiceId, Long customerId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với id: " + invoiceId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + customerId));

        // Kiểm tra nếu khách hàng đã được gán vào hóa đơn khác có status = 0
        boolean isCustomerAssigned = invoiceRepository.existsByCustomer_IdAndStatus(customerId, 0);

        if (isCustomerAssigned) {
            throw new RuntimeException("Khách hàng đã được gán vào một hóa đơn đang xử lý (status = 0)");
        }

        // Gán khách hàng vào hóa đơn
        invoice.setCustomer(customer);
        invoiceRepository.save(invoice);
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
    public void cancelInvoice(Long invoiceId) {
        // 1. Lấy hóa đơn theo ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // 2. Lấy danh sách chi tiết hóa đơn của hóa đơn đó
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoice(invoice);

        // 3. Trả lại số lượng tồn kho cho từng biến thể sản phẩm trong chi tiết hóa đơn
        for (InvoiceDetail detail : invoiceDetails) {
            ProductDetail productDetail = detail.getProductDetail();

            // Cộng lại số lượng tồn kho (trả lại hàng về kho)
            productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
            productDetailRepository.save(productDetail);
        }

        // 4. Xóa hết chi tiết hóa đơn liên quan
        invoiceDetailRepository.deleteAll(invoiceDetails);

        // 5. Cập nhật trạng thái hóa đơn là đã hủy (giả sử status=2 là hủy)
        invoice.setStatus(2);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setUpdatedDate(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public void checkout(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        invoice.setStatus(1);  // Đặt trạng thái thành 1 - đã thanh toán
        invoice.setOrderType(1);
        invoice.setUpdatedDate(LocalDateTime.now());
        invoice.setUpdatedBy(DEFAULT_USER);

        invoiceRepository.save(invoice);  // Lưu lại thay đổi
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

    @Transactional
    public void deleteCartItemById(Long invoiceDetailId) {
        InvoiceDetail detail = invoiceDetailRepository.findById(invoiceDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        ProductDetail productDetail = detail.getProductDetail();
        if (productDetail == null) {
            throw new RuntimeException("ProductDetail không tồn tại");
        }

        // Trả lại tồn kho
        int newQuantity = productDetail.getQuantity() + detail.getQuantity();
        productDetail.setQuantity(newQuantity);
        productDetailRepository.save(productDetail);

        // Tính tổng tiền của dòng sản phẩm
        BigDecimal sellPrice = productDetail.getSellPrice();
        if (sellPrice == null) {
            throw new RuntimeException("Giá bán của sản phẩm không tồn tại");
        }
        BigDecimal lineTotal = sellPrice.multiply(BigDecimal.valueOf(detail.getQuantity()));

        Invoice invoice = detail.getInvoice();
        if (invoice == null) {
            throw new RuntimeException("Hóa đơn không tồn tại");
        }

        BigDecimal totalAmount = invoice.getTotalAmount() == null ? BigDecimal.ZERO : invoice.getTotalAmount();
        BigDecimal discountAmount = invoice.getDiscountAmount() == null ? BigDecimal.ZERO : invoice.getDiscountAmount();

        invoice.setTotalAmount(totalAmount.subtract(lineTotal));
        invoice.setFinalAmount(invoice.getTotalAmount().subtract(discountAmount));

        // Xóa dòng sản phẩm khỏi chi tiết hóa đơn
        invoiceDetailRepository.delete(detail);

        invoiceRepository.save(invoice);
    }

    /**
     * Thêm hoặc cập nhật chi tiết hóa đơn (InvoiceDetail) với số lượng mới.
     */
    @Transactional
    public void addInvoiceDetails(Long invoiceId, Long productDetailId, Integer quantity) {
        // 1. Lấy hóa đơn theo ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // 2. Lấy biến thể sản phẩm theo ID
        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại"));

        // 3. Kiểm tra tồn kho đủ để thêm
        if (productDetail.getQuantity() < quantity) {
            throw new RuntimeException("Số lượng trong kho không đủ");
        }

        // 4. Tìm chi tiết hóa đơn hiện có với biến thể sản phẩm này
        Optional<InvoiceDetail> existingDetailOpt = invoiceDetailRepository.findByInvoiceAndProductDetail(invoice, productDetail);

        InvoiceDetail invoiceDetail;
        LocalDateTime now = LocalDateTime.now();

        if (existingDetailOpt.isPresent()) {
            // 5a. Nếu có, cộng dồn số lượng và cập nhật thời gian sửa
            invoiceDetail = existingDetailOpt.get();
            invoiceDetail.setQuantity(invoiceDetail.getQuantity() + quantity);
            invoiceDetail.setUpdatedDate(now);
        } else {
            // 5b. Nếu chưa có, tạo mới chi tiết hóa đơn
            invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductDetail(productDetail);
            invoiceDetail.setQuantity(quantity);
            invoiceDetail.setCreatedDate(now);
            invoiceDetail.setCreatedBy(DEFAULT_USER);
            invoiceDetail.setStatus(1);

            // Tạo mã chi tiết hóa đơn tự động
            long count = invoiceDetailRepository.count() + 1;
            String invoiceDetailCode = String.format("INV-D-%04d", count);
            invoiceDetail.setInvoiceCodeDetail(invoiceDetailCode);
        }

        // 6. Cập nhật tồn kho của biến thể sản phẩm
        productDetail.setQuantity(productDetail.getQuantity() - quantity);

        // 7. Lưu chi tiết hóa đơn và cập nhật tồn kho
        invoiceDetailRepository.save(invoiceDetail);
        productDetailRepository.save(productDetail);

        // 8. Cập nhật tổng tiền cho hóa đơn
        updateInvoiceTotal(invoice);

        // 9. Lấy lại danh sách tất cả chi tiết hóa đơn để trả về
        List<InvoiceDetail> allDetails = invoiceDetailRepository.findByInvoice(invoice);

        // 10. Trả về kết quả đã map sang response DTO
        invoiceMapper.toInvoiceDisplayResponse(invoice, allDetails);
    }

    public Invoice applyVoucherToInvoice(Long invoiceId, String voucherCode) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        if (voucher.getStatus() != 1) {
            throw new RuntimeException("Voucher không khả dụng");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher đã hết hạn");
        }

        if (invoice.getTotalAmount().compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Không đạt giá trị tối thiểu để áp dụng voucher");
        }

        if (voucher.getCustomer() != null && invoice.getCustomer() != null &&
                voucher.getCustomer().getId() != null &&
                !voucher.getCustomer().getId().equals(invoice.getCustomer().getId())) {
            throw new RuntimeException("Voucher không áp dụng cho khách hàng này");
        }

        // Bỏ kiểm tra orderType vì invoice không có trường này

        // Tính tiền giảm
        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountPercentage() != null && voucher.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discount = invoice.getTotalAmount()
                    .multiply(voucher.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100));

            if (voucher.getMaxDiscountValue() != null && discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                discount = voucher.getMaxDiscountValue();
            }
        } else if (voucher.getDiscountAmount() != null && voucher.getDiscountAmount() > 0) {
            discount = BigDecimal.valueOf(voucher.getDiscountAmount());
        }

        invoice.setVoucher(voucher);
        invoice.setDiscountAmount(discount);
        invoice.setFinalAmount(invoice.getTotalAmount().subtract(discount));
        invoice.setUpdatedDate(now);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        VoucherHistory history = new VoucherHistory();
        history.setVoucher(voucher);
        history.setInvoice(invoice);
        history.setCustomer(invoice.getCustomer());
        history.setUsedAt(now);
        history.setDiscountValueApplied(discount);
        history.setStatus(1);
        history.setCreatedDate(now);
        history.setCreatedBy("admin");
        history.setUpdatedBy("admin");

        voucherHistoryRepository.save(history);

        return updatedInvoice;
    }

    /**
     * Tạo hóa đơn(bán tại quầy)
     */

    public List<ProductAttributeResponse> getProductAttributesByProductId(Long productId) {
        List<ProductDetail> details = productDetailRepository.findByProductId(productId);
        return invoiceMapper.toProductAttributeResponseList(details);
    }

    public Page<InvoiceResponse> getInvoicesByStatus(int status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Invoice> invoicePage = invoiceRepository.findByStatus(status, pageable);
        return invoicePage.map(invoiceMapper::toInvoiceResponse);
    }

    @Override
    public InvoiceDisplayResponse getInvoiceWithDetails(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoiceId);

        // ✅ Log ID khách hàng
        if (invoice.getCustomer() != null) {
            System.out.println("ID khách hàng gắn với hóa đơn: " + invoice.getCustomer().getId());
        } else {
            System.out.println("Hóa đơn không có khách hàng.");
        }

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
            response.setCustomerId(invoice.getCustomer().getId()); // 👈 Thêm dòng này
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

        // 👉 Thêm tên khách hàng
        if (detail.getInvoice().getCustomer() != null) {
            response.setCustomerName(detail.getInvoice().getCustomer().getCustomerName());
        } else {
            response.setCustomerName("Khách lẻ"); // hoặc để null tùy bạn
        }

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

    @Override
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Override
    public List<InvoiceDisplayResponse> getInvoicesWithDetailsByIds(List<Long> ids) {
        List<Invoice> invoices = invoiceRepository.findAllById(ids);

        // Lấy toàn bộ chi tiết hóa đơn theo danh sách ID hóa đơn
        List<InvoiceDetail> allDetails = invoiceDetailRepository.findByInvoiceIdIn(ids);

        // Gộp các chi tiết theo invoiceId để ghép đúng hóa đơn
        Map<Long, List<InvoiceDetail>> detailMap = allDetails.stream()
                .collect(Collectors.groupingBy(detail -> detail.getInvoice().getId()));

        // Map từng hóa đơn với danh sách chi tiết tương ứng
        return invoices.stream()
                .map(invoice -> {
                    List<InvoiceDetail> details = detailMap.getOrDefault(invoice.getId(), new ArrayList<>());
                    return invoiceMapper.toInvoiceDisplayResponse(invoice, details);
                })
                .collect(Collectors.toList());
    }

}

