package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.PaymentSummary;
import com.example.duantotnghiep.dto.request.AddressRequest;
import com.example.duantotnghiep.dto.request.CartItemRequest;
import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.InvoiceWithVnpayResponse;
import com.example.duantotnghiep.dto.response.InvoiceWithZaloPayResponse;
import com.example.duantotnghiep.dto.response.ProductAttributeResponse;
import com.example.duantotnghiep.dto.response.VnpayResponse;
import com.example.duantotnghiep.dto.response.ZaloPayResponse;
import com.example.duantotnghiep.mapper.InvoiceMapper;
import com.example.duantotnghiep.model.*;
import com.example.duantotnghiep.repository.*;
import com.example.duantotnghiep.service.EmailService;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.VoucherEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ProductDetailRepository productDetailRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;
    private final EmailService emailService;

    private final InvoiceMapper invoiceMapper;
    private static final String DEFAULT_CUSTOMER_NAME = "Khách lẻ";
    private final UserRepository userRepository;
    private final VoucherEmailService voucherEmailService;
    private final AddressRepository addressRepository;
    private final EmployeeRepository employeeRepository;
    private final ZaloPayService zaloPayService;
    private final VnpayService vnpayService;
    private final InvoiceEmailService invoiceEmailService;

    @Transactional
    @Override
    public InvoiceResponse createEmptyInvoice() {
        // Lấy username từ người đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm user theo username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        // Lấy employee từ user
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        // Tạo hóa đơn mới
        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setEmployee(employee);
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(0);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoice.setCreatedBy(username);
        invoice.setDescription("Hóa đơn bán tại quầy");
        invoice.setOrderType(0); // tại quầy

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
        List<Customer> customers = customerRepository.findByPhoneStartingWithAndStatusActive(phonePrefix);

        if (customers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy khách hàng đang hoạt động với số điện thoại bắt đầu bằng: " + phonePrefix);
        }

        return customers.stream()
                .map(invoiceMapper::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CustomerResponse createQuickCustomer(String phone, String name, String email) {
        // Tạo khách hàng mới
        Customer newCustomer = new Customer();
        newCustomer.setCustomerName(name != null ? name : "Khách lẻ");
        newCustomer.setPhone(phone);
        newCustomer.setEmail(email); // <-- Thêm dòng này

        long count = customerRepository.count() + 1;
        newCustomer.setCustomerCode(String.format("CUS%02d", count));
        newCustomer.setStatus(1);
        newCustomer.setCreatedDate(LocalDateTime.now());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        newCustomer.setCreatedBy(username);

        Customer customer = customerRepository.save(newCustomer);

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

//    @Transactional
//    @Override
//    public void checkout(Long invoiceId) {
//        // 1. Lấy hóa đơn
//        Invoice invoice = invoiceRepository.findById(invoiceId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
//
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        // 2. Cập nhật trạng thái hóa đơn
//        invoice.setStatus(1); // Đã thanh toán
//        invoice.setUpdatedDate(LocalDateTime.now());
//        invoice.setUpdatedBy(username);
//
//        // 3. Xử lý nếu hóa đơn sử dụng voucher
//        Voucher usedVoucher = invoice.getVoucher();
//        if (usedVoucher != null) {
//            List<VoucherHistory> histories = voucherHistoryRepository
//                    .findByInvoiceAndVoucherAndStatus(invoice, usedVoucher, 0);
//
//            for (VoucherHistory history : histories) {
//                history.setStatus(1);
//                history.setUpdatedDate(LocalDateTime.now());
//                history.setUpdatedBy(username);
//            }
//            voucherHistoryRepository.saveAll(histories);
//
//            if (usedVoucher.getQuantity() != null && usedVoucher.getQuantity() > 0) {
//                usedVoucher.setQuantity(usedVoucher.getQuantity() - 1);
//                usedVoucher.setUpdatedDate(LocalDateTime.now());
//                usedVoucher.setUpdatedBy(username);
//                voucherRepository.save(usedVoucher);
//            } else {
//                throw new RuntimeException("Voucher đã hết lượt sử dụng!");
//            }
//        }
//
//        // 4. Lưu hóa đơn cập nhật
//        invoiceRepository.save(invoice);
//
//        // 5. 🎁 Xử lý tự động tặng voucher nếu đủ điều kiện
//        if (invoice.getCustomer() != null) {
//            BigDecimal totalAmount = invoice.getTotalAmount();
//            Long customerId = invoice.getCustomer().getId();
//
//            // Lấy danh sách chương trình AUTO đang bật
//            List<Voucher> autoPromos = voucherRepository
//                    .findByVoucherNameStartingWithAndStatus("AUTO_PROMO_", 1);
//
//            // Tìm chương trình phù hợp với đơn hàng
//            Voucher matchedPromo = autoPromos.stream()
//                    .filter(v -> totalAmount.compareTo(v.getMinOrderValue()) >= 0)
//                    .sorted((v1, v2) -> v2.getMinOrderValue().compareTo(v1.getMinOrderValue()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (matchedPromo != null) {
//                boolean alreadyGiven = voucherRepository.existsByCustomerIdAndVoucherNameAndDiscountAmount(
//                        customerId,
//                        matchedPromo.getVoucherName(),
//                        matchedPromo.getDiscountAmount()
//                );
//
//                if (!alreadyGiven) {
//                    // Tạo voucher tặng
//                    Voucher newVoucher = new Voucher();
//                    newVoucher.setCustomer(invoice.getCustomer());
//                    newVoucher.setVoucherCode("VOUCHER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
//                    newVoucher.setVoucherName(matchedPromo.getVoucherName());
//                    newVoucher.setDiscountAmount(matchedPromo.getDiscountAmount());
//                    newVoucher.setMinOrderValue(matchedPromo.getMinOrderValue());
//                    newVoucher.setStartDate(LocalDateTime.now());
//                    newVoucher.setEndDate(LocalDateTime.now().plusDays(30));
//                    newVoucher.setStatus(1);
//                    newVoucher.setCreatedDate(LocalDateTime.now());
//                    newVoucher.setCreatedBy("SYSTEM");
//                    newVoucher.setQuantity(1);
//                    newVoucher.setVoucherType(0); // tặng
//                    newVoucher.setOrderType(1);
//
//                    voucherRepository.save(newVoucher);
//
//                    // Gửi email thông báo qua VoucherEmailService
//                    String email = invoice.getCustomer().getEmail();
//                    if (email != null && !email.isEmpty()) {
//                        voucherEmailService.sendVoucherNotificationEmail(
//                                email,
//                                invoice.getCustomer().getCustomerName(),
//                                totalAmount,
//                                matchedPromo.getDiscountAmount(),
//                                newVoucher.getVoucherCode(),
//                                newVoucher.getEndDate().toLocalDate()
//                        );
//                    }
//                }
//            }
//        }
//    }

    @Transactional
    @Override
    public void checkout(Long invoiceId) {
        // 1. Lấy hóa đơn và kiểm tra tồn tại
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();

        // 2. Trừ tồn kho theo chi tiết hóa đơn
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoice(invoice);
        for (InvoiceDetail detail : details) {
            ProductDetail productDetail = detail.getProductDetail();
            int currentStock = productDetail.getQuantity();
            int toDeduct = detail.getQuantity();

            if (currentStock < toDeduct) {
                throw new RuntimeException("Sản phẩm " + productDetail.getProduct().getProductName() + " không đủ tồn kho!");
            }

            productDetail.setQuantity(currentStock - toDeduct);
            productDetail.setUpdatedDate(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));            productDetail.setUpdatedBy(username);
            productDetailRepository.save(productDetail);
        }

        // 3. Cập nhật trạng thái hóa đơn
        invoice.setStatus(1); // Đã thanh toán
        invoice.setUpdatedDate(now);
        invoice.setUpdatedBy(username);
        invoiceRepository.save(invoice);

        // 4. Nếu có voucher được sử dụng
        handleUsedVoucher(invoice, username, now);

        // 5. Xử lý tặng voucher tự động nếu đủ điều kiện
        handleAutoPromoVoucher(invoice, username, now);
    }

    private void handleUsedVoucher(Invoice invoice, String username, LocalDateTime now) {
        Voucher usedVoucher = invoice.getVoucher();
        if (usedVoucher != null) {
            List<VoucherHistory> histories = voucherHistoryRepository
                    .findByInvoiceAndVoucherAndStatus(invoice, usedVoucher, 0);

            for (VoucherHistory history : histories) {
                history.setStatus(1);
                history.setUpdatedDate(now);
                history.setUpdatedBy(username);
            }
            voucherHistoryRepository.saveAll(histories);

            if (usedVoucher.getQuantity() != null && usedVoucher.getQuantity() > 0) {
                usedVoucher.setQuantity(usedVoucher.getQuantity() - 1);
                usedVoucher.setUpdatedDate(now);
                usedVoucher.setUpdatedBy(username);
                voucherRepository.save(usedVoucher);
            } else {
                throw new RuntimeException("Voucher đã hết lượt sử dụng!");
            }
        }
    }

    private void handleAutoPromoVoucher(Invoice invoice, String username, LocalDateTime now) {
        // Bỏ qua nếu không có khách hàng
        if (invoice.getCustomer() == null) return;

        BigDecimal totalAmount = invoice.getTotalAmount();
        Long customerId = invoice.getCustomer().getId();

        // Lọc danh sách voucher đang hoạt động và còn hiệu lực theo thời gian
        List<Voucher> activePromos = voucherRepository.findByStatus(1).stream()
                .filter(voucher -> {
                    LocalDateTime start = voucher.getStartDate();
                    LocalDateTime end = voucher.getEndDate();
                    return (start == null || !now.isBefore(start)) &&
                            (end == null || !now.isAfter(end));
                })
                .collect(Collectors.toList());

        // Tìm voucher phù hợp nhất dựa trên minOrderValue (ưu tiên giá trị cao nhất)
        Voucher matchedPromo = activePromos.stream()
                .filter(voucher -> {
                    BigDecimal minOrder = voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : BigDecimal.ZERO;
                    return totalAmount.compareTo(minOrder) >= 0;
                })
                .sorted((v1, v2) -> {
                    BigDecimal mv1 = v1.getMinOrderValue() != null ? v1.getMinOrderValue() : BigDecimal.ZERO;
                    BigDecimal mv2 = v2.getMinOrderValue() != null ? v2.getMinOrderValue() : BigDecimal.ZERO;
                    return mv2.compareTo(mv1); // giảm dần
                })
                .findFirst()
                .orElse(null);

        // Nếu tìm thấy voucher phù hợp
        if (matchedPromo != null) {
            int discountAmount = matchedPromo.getDiscountAmount() != null
                    ? matchedPromo.getDiscountAmount()
                    : 0;

            boolean alreadyGiven = voucherRepository.existsByCustomerIdAndVoucherNameAndDiscountAmount(
                    customerId,
                    matchedPromo.getVoucherName(),
                    discountAmount
            );

            if (!alreadyGiven) {
                Voucher newVoucher = new Voucher();
                newVoucher.setCustomer(invoice.getCustomer());
                newVoucher.setVoucherCode("VOUCHER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                newVoucher.setVoucherName(matchedPromo.getVoucherName());
                newVoucher.setDiscountAmount(discountAmount);

                BigDecimal discountPercentage = matchedPromo.getDiscountPercentage() != null
                        ? matchedPromo.getDiscountPercentage()
                        : BigDecimal.ZERO;
                newVoucher.setDiscountPercentage(discountPercentage);

                BigDecimal minOrderValue = matchedPromo.getMinOrderValue() != null
                        ? matchedPromo.getMinOrderValue()
                        : BigDecimal.ZERO;
                newVoucher.setMinOrderValue(minOrderValue);

                BigDecimal maxDiscountValue = matchedPromo.getMaxDiscountValue() != null
                        ? matchedPromo.getMaxDiscountValue()
                        : BigDecimal.ZERO;
                newVoucher.setMaxDiscountValue(maxDiscountValue);

                newVoucher.setStartDate(now);
                newVoucher.setEndDate(now.plusDays(30));
                newVoucher.setStatus(1);
                newVoucher.setCreatedDate(now);
                newVoucher.setCreatedBy("SYSTEM");
                newVoucher.setQuantity(1);
                newVoucher.setVoucherType(0); // loại: tặng
                newVoucher.setOrderType(1);   // offline/online tùy định nghĩa

                // Lưu voucher
                voucherRepository.save(newVoucher);

                // Gửi email nếu có địa chỉ
                String email = invoice.getCustomer().getEmail();
                if (email != null && !email.isEmpty()) {
                    voucherEmailService.sendVoucherNotificationEmail(
                            email,
                            invoice.getCustomer().getCustomerName(),
                            totalAmount,
                            BigDecimal.valueOf(discountAmount),
                            discountPercentage,
                            newVoucher.getVoucherCode(),
                            newVoucher.getEndDate().toLocalDate()
                    );
                }
            }
        }
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
    public InvoiceDisplayResponse addInvoiceDetails(Long invoiceId, Long productDetailId, Integer quantity) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại"));

        // ⚠️ CHỈ kiểm tra tồn kho nhưng KHÔNG trừ
        if (productDetail.getQuantity() < quantity) {
            throw new RuntimeException("Số lượng trong kho không đủ để thêm vào giỏ hàng");
        }

        InvoiceDetail invoiceDetail = invoiceDetailRepository
                .findByInvoiceAndProductDetail(invoice, productDetail)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (invoiceDetail != null) {
            invoiceDetail.setQuantity(invoiceDetail.getQuantity() + quantity);
            invoiceDetail.setUpdatedDate(now);
            invoiceDetail.setUpdatedBy(username);
        } else {
            invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductDetail(productDetail);
            invoiceDetail.setQuantity(quantity);
            invoiceDetail.setCreatedDate(now);
            invoiceDetail.setCreatedBy(username);
            invoiceDetail.setStatus(1);

            long count = invoiceDetailRepository.count() + 1;
            invoiceDetail.setInvoiceCodeDetail(String.format("INV-D-%04d", count));
        }

        invoiceDetailRepository.save(invoiceDetail);

        // ✅ KHÔNG cập nhật tồn kho tại đây

        updateInvoiceTotal(invoice);

        List<InvoiceDetail> allDetails = invoiceDetailRepository.findByInvoice(invoice);
        return invoiceMapper.toInvoiceDisplayResponse(invoice, allDetails);
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
        history.setStatus(0);


        voucherHistoryRepository.save(history);

        return updatedInvoice;
    }

    public Invoice removeVoucherFromInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Nếu không có voucher nào đang áp dụng thì không cần làm gì
        if (invoice.getVoucher() == null) {
            throw new RuntimeException("Hóa đơn hiện không có voucher để bỏ");
        }

        // Xóa thông tin voucher
        invoice.setVoucher(null);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(invoice.getTotalAmount());
        invoice.setUpdatedDate(LocalDateTime.now());

        // Lưu lại hóa đơn đã cập nhật
        return invoiceRepository.save(invoice);
    }


    /**
     * Tạo hóa đơn(bán tại quầy)
     */

    public List<ProductAttributeResponse> getProductAttributesByProductId(Long productId) {
        List<ProductDetail> details = productDetailRepository.findByProductId(productId);
        return invoiceMapper.toProductAttributeResponseList(details);
    }

    public Page<InvoiceResponse> getInvoicesByStatus(int status, int page, int size) {
        // Lấy username từ SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm user theo username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        // Lấy employee
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        // Tạo phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        // Truy vấn theo status và employeeId
        Page<Invoice> invoicePage = invoiceRepository.findByStatusAndEmployeeId(status, employee.getId(), pageable);

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

    @Transactional
    @Override
    public InvoiceDisplayResponse createInvoice(InvoiceRequest request) {
        // 1. Xử lý thông tin khách hàng
        Customer customer;
        Long customerId = request.getCustomerInfo().getCustomerId();

        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + customerId));
        } else {
            customer = customerRepository.findTop1ByPhoneAndStatus(request.getCustomerInfo().getPhone(), 1)
                    .orElseGet(() -> {
                        Customer c = new Customer();
                        c.setCustomerName(request.getCustomerInfo().getCustomerName());
                        c.setPhone(request.getCustomerInfo().getPhone());
                        c.setEmail(request.getCustomerInfo().getEmail());
                        c.setStatus(1);
                        c.setCustomerCode("KH" + System.currentTimeMillis());
                        c.setCreatedDate(LocalDateTime.now());
                        return customerRepository.save(c);
                    });
        }

        // 2. Lưu địa chỉ
        AddressRequest addr = request.getCustomerInfo().getAddress();
        AddressCustomer address = new AddressCustomer();
        address.setCustomer(customer);
        address.setCountry(addr.getCountry());
        address.setProvinceCode(addr.getProvinceCode());
        address.setProvinceName(addr.getProvinceName());
        address.setDistrictCode(addr.getDistrictCode());
        address.setDistrictName(addr.getDistrictName());
        address.setWardCode(addr.getWardCode());
        address.setWardName(addr.getWardName());
        address.setHouseName(addr.getHouseName());
        address.setStatus(1);
        address.setCreatedDate(new Date());
        address.setDefaultAddress(true);
        addressRepository.save(address);

        // 3. Tạo hóa đơn
        Invoice invoice = new Invoice();
        invoice.setInvoiceCode("INV" + System.currentTimeMillis());
        invoice.setCustomer(customer);
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setUpdatedDate(LocalDateTime.now());
        invoice.setDescription(request.getDescription());
        invoice.setOrderType(request.getOrderType());
        invoice.setStatus(0); // Mặc định

        invoice.setDiscountAmount(Optional.ofNullable(request.getDiscountAmount()).orElse(BigDecimal.ZERO));
        invoice.setShippingFee(Optional.ofNullable(request.getShippingFee()).orElse(BigDecimal.ZERO)); // ✅ thêm phí ship

        if (request.getEmployeeId() != null) {
            employeeRepository.findById(request.getEmployeeId()).ifPresent(invoice::setEmployee);
        }

        if (request.getVoucherId() != null) {
            voucherRepository.findById(request.getVoucherId()).ifPresent(invoice::setVoucher);
        }

        // 4. Tạo danh sách chi tiết hóa đơn
        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceDetail> details = new ArrayList<>();

        for (CartItemRequest item : request.getItems()) {
            ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + item.getProductDetailId()));

            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setProductDetail(productDetail);
            detail.setQuantity(item.getQuantity());
            detail.setCreatedDate(LocalDateTime.now());
            detail.setStatus(0);
            detail.setInvoiceCodeDetail("INV-DTL-" + UUID.randomUUID().toString().substring(0, 8));

            BigDecimal itemTotal = productDetail.getSellPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
            details.add(detail);
        }

        invoice.setTotalAmount(total);

        // ✅ Tính finalAmount = total - discount + phí ship
        invoice.setFinalAmount(total
                .subtract(invoice.getDiscountAmount())
                .add(invoice.getShippingFee()));

        invoice.setInvoiceDetails(details);

        // 5. Lưu và trả kết quả
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toInvoiceDisplayResponse(savedInvoice, savedInvoice.getInvoiceDetails());
    }

    @Transactional
    @Override
    public InvoiceWithZaloPayResponse createInvoiceAndZaloPay(InvoiceRequest request) throws Exception {
        // Bước 1: Tạo hóa đơn và lưu vào DB
        InvoiceDisplayResponse invoiceDisplay = this.createInvoice(request);
        Long invoiceId = invoiceDisplay.getInvoice().getId();

        // Bước 2: Tạo appTransId theo định dạng yyMMdd_id
        String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + invoiceId;

        // Bước 3: Gọi ZaloPay để tạo đơn thanh toán
        ZaloPayResponse zaloPayResponse = zaloPayService.createZaloPayOrder(
                invoiceDisplay.getInvoice().getPhone(),
                invoiceDisplay.getInvoice().getFinalAmount(),
                "Thanh toán đơn hàng #" + invoiceDisplay.getInvoice().getInvoiceCode(),
                appTransId
        );

        // Bước 4: Cập nhật appTransId vào DB
        invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
            invoice.setAppTransId(appTransId);
            invoice.setUpdatedDate(LocalDateTime.now());
            invoiceRepository.save(invoice);
        });

        // ✅ Bước 4.5: Đặt hẹn 1 phút nếu không thanh toán → chuyển status = 11
//        scheduleFailIfNotPaid(appTransId);

        // Bước 5: Trả kết quả về client
        return new InvoiceWithZaloPayResponse(invoiceDisplay, zaloPayResponse);
    }

//    @Async
//    public void scheduleFailIfNotPaid(String appTransId) {
//        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
//            Optional<Invoice> opt = invoiceRepository.findByAppTransId(appTransId);
//            if (opt.isPresent()) {
//                Invoice invoice = opt.get();
//                if (invoice.getStatus() == 0) { // chưa thanh toán
//                    invoice.setStatus(11); // thanh toán thất bại
//                    invoice.setUpdatedDate(LocalDateTime.now());
//                    invoiceRepository.save(invoice);
//                    log.info("⏰ Đơn {} không thanh toán sau 1 phút → chuyển status = 11", appTransId);
//                }
//            }
//        }, 1, TimeUnit.MINUTES);
//    }

    @Transactional
    public void updateInvoiceStatusByAppTransId(String appTransId, int status) {
        invoiceRepository.findByAppTransId(appTransId).ifPresent(invoice -> {
            invoice.setStatus(status);
            invoice.setUpdatedDate(LocalDateTime.now());
            invoiceRepository.save(invoice);
        });
    }

    @Override
    public void updateStatusIfPaid(String appTransId) throws Exception {
        JSONObject response = zaloPayService.queryOrder(appTransId); // ✅ gọi từ ZaloPayService

        int returnCode = response.optInt("returncode");
        int bcTransStatus = response.optInt("bctransstatus");

        if (returnCode == 1 && bcTransStatus == 1) {
            updateInvoiceStatusByAppTransId(appTransId, 1); // PAID
            log.info("✅ Đơn {} đã được cập nhật thành PAID từ kết quả query ZaloPay", appTransId);
        } else {
            log.warn("❌ Đơn {} chưa thanh toán (status={})", appTransId, bcTransStatus);
        }
    }

    @Transactional
    @Override
    public InvoiceWithVnpayResponse createInvoiceAndVnpay(InvoiceRequest request) throws Exception {
        // Bước 1: Tạo hóa đơn
        InvoiceDisplayResponse invoiceDisplay = this.createInvoice(request);
        Long invoiceId = invoiceDisplay.getInvoice().getId();

        // Bước 2: Tạo orderId
        String orderId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + invoiceId;

        // Bước 3: Gọi VNPay
        VnpayResponse vnpayResponse = vnpayService.createVnpayOrder(
                orderId,
                invoiceDisplay.getInvoice().getFinalAmount(),
                "Thanh toán đơn hàng #" + invoiceDisplay.getInvoice().getInvoiceCode()
        );

        // Bước 4: Lưu orderId vào DB
        invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
            invoice.setAppTransId(orderId); // ✅ dùng lại trường appTransId
            invoice.setUpdatedDate(LocalDateTime.now());
            invoiceRepository.save(invoice);
        });

        return new InvoiceWithVnpayResponse(invoiceDisplay, vnpayResponse);
    }

    @Override
    public Page<InvoiceResponse> searchInvoices(Integer status,Integer orderType, LocalDateTime createdFrom, LocalDateTime createdTo, String phone, String code, Pageable pageable) {
        Page<InvoiceResponse> invoices = invoiceRepository.searchInvoices(status,orderType,createdFrom,createdTo,phone,code,pageable);
        return invoices;
    }


}

