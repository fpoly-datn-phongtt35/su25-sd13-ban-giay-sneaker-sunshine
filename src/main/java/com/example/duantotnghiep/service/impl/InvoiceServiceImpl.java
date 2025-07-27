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
import com.example.duantotnghiep.model.AddressCustomer;
import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.Employee;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.model.InvoiceTransaction;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.model.PromotionSuggestion;
import com.example.duantotnghiep.model.PurchaseStats;
import com.example.duantotnghiep.model.User;
import com.example.duantotnghiep.model.Voucher;
import com.example.duantotnghiep.model.VoucherHistory;
import com.example.duantotnghiep.repository.AddressRepository;
import com.example.duantotnghiep.repository.CustomerRepository;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import com.example.duantotnghiep.repository.EmployeeRepository;
import com.example.duantotnghiep.repository.InvoiceDetailRepository;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.repository.InvoiceTransactionRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import com.example.duantotnghiep.repository.ProductRepository;
import com.example.duantotnghiep.repository.UserRepository;
import com.example.duantotnghiep.repository.VoucherHistoryRepository;
import com.example.duantotnghiep.repository.VoucherRepository;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.VoucherEmailService;
import com.example.duantotnghiep.service.VoucherService;
import com.example.duantotnghiep.state.CustomerTier;
import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;

    private final InvoiceMapper invoiceMapper;
    private static final String DEFAULT_CUSTOMER_NAME = "Khách lẻ";
    private final UserRepository userRepository;
    private final VoucherEmailService voucherEmailService;
    private final AddressRepository addressRepository;
    private final EmployeeRepository employeeRepository;
    private final InvoiceTransactionRepository invoiceTransactionRepository;
    private final ZaloPayService zaloPayService;
    private final VnpayService vnpayService;
    private final DiscountCampaignRepository discountCampaignRepository;
    private final VoucherService voucherService;
//    private final InvoiceService invoiceService;


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
        invoice.setCreatedDate(new Date());
        invoice.setStatus(TrangThaiTong.DANG_XU_LY);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoice.setCreatedBy(username);
        invoice.setDescription("Hóa đơn bán tại quầy");
        invoice.setOrderType(0); // tại quầy
        invoice.setIsPaid(false);

        invoiceRepository.save(invoice);
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    private String generateInvoiceCode() {
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%04d", count);
    }

    /**
     * Cập nhật tổng tiền hóa đơn:
     * - totalAmount: tổng tiền gốc (sellPrice × quantity)
     * - discountAmount: tổng tiền giảm giá
     * - finalAmount: tổng tiền sau giảm giá
     */
    @Transactional
    public void applyDiscountToInvoiceDetails(Invoice invoice) {
        List<DiscountCampaign> activeCampaigns = discountCampaignRepository.findActiveCampaigns(LocalDateTime.now());
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice ,1);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();

        for (InvoiceDetail detail : details) {
            // Tìm discount phù hợp (ví dụ theo mã SP)
            double discount = getBestDiscountPercentageForProductCode(
                    detail.getProductDetail().getProduct().getProductCode(),
                    activeCampaigns
            );

            int discountPercentInt = (int) Math.round(discount);

            BigDecimal sellPrice = detail.getSellPrice();
            BigDecimal discountAmount = calculateDiscountAmount(sellPrice, discountPercentInt);
            BigDecimal discountedPrice = sellPrice.subtract(discountAmount);

            // Update detail
            detail.setDiscountPercentage(discountPercentInt);
            detail.setDiscountedPrice(discountedPrice);
            detail.setUpdatedDate(now);
            detail.setUpdatedBy(username);

            invoiceDetailRepository.save(detail);
        }
    }


    @Transactional
    public void updateInvoiceTotal(Invoice invoice) {
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

        BigDecimal totalAmount = BigDecimal.ZERO;       // tổng tiền gốc
        BigDecimal productDiscountAmount = BigDecimal.ZERO; // tổng giảm giá sản phẩm
        BigDecimal finalAmount = BigDecimal.ZERO;       // sau giảm giá sản phẩm

        for (InvoiceDetail detail : details) {
            BigDecimal quantity = BigDecimal.valueOf(detail.getQuantity());

            BigDecimal itemTotal = detail.getSellPrice().multiply(quantity);          // giá gốc
            BigDecimal discountedItemTotal = detail.getDiscountedPrice().multiply(quantity); // giá sau giảm SP

            totalAmount = totalAmount.add(itemTotal);
            productDiscountAmount = productDiscountAmount.add(itemTotal.subtract(discountedItemTotal));
            finalAmount = finalAmount.add(discountedItemTotal);
        }

        // Tính giảm giá từ voucher nếu có
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        Voucher voucher = invoice.getVoucher();
        if (voucher != null) {
            voucherDiscount = calculateVoucherDiscount(invoice, voucher, totalAmount);
            finalAmount = finalAmount.subtract(voucherDiscount);
        }

        BigDecimal totalDiscount = productDiscountAmount.add(voucherDiscount);

        invoice.setTotalAmount(totalAmount);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setFinalAmount(finalAmount);
        invoice.setUpdatedDate(new Date());
        invoice.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        invoiceRepository.save(invoice);

        // Log để debug
        System.out.println("=== updateInvoiceTotal ===");
        System.out.println("TotalAmount (gốc): " + totalAmount);
        System.out.println("ProductDiscountAmount: " + productDiscountAmount);
        System.out.println("VoucherDiscount: " + voucherDiscount);
        System.out.println("FinalAmount (khách trả): " + finalAmount);
        System.out.println("=========================");
    }

    private BigDecimal calculateVoucherDiscount(Invoice invoice, Voucher voucher, BigDecimal totalAmount) {
        if (voucher == null || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Trường hợp giảm theo phần trăm
        BigDecimal percentage = voucher.getDiscountPercentage();
        if (percentage != null && percentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = totalAmount.multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);

            BigDecimal maxDiscount = voucher.getMaxDiscountValue();
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                return maxDiscount;
            }
            return discount;
        }

        // Trường hợp giảm theo số tiền cố định
        BigDecimal fixedDiscount = BigDecimal.valueOf(voucher.getDiscountAmount());
        if (fixedDiscount != null && fixedDiscount.compareTo(BigDecimal.ZERO) > 0) {
            return fixedDiscount;
        }

        // Mặc định không giảm
        return BigDecimal.ZERO;
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
        boolean isCustomerAssigned = invoiceRepository.existsByCustomer_IdAndStatus(customerId, TrangThaiTong.DANG_XU_LY);

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
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

        // 4. Xóa hết chi tiết hóa đơn liên quan
        invoiceDetailRepository.deleteAll(invoiceDetails);

        // 5. Cập nhật trạng thái hóa đơn là đã hủy (giả sử status=2 là hủy)
        invoice.setStatus(TrangThaiTong.DA_HUY);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setFinalAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setUpdatedDate(new Date());
        invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public void checkout(Long invoiceId) {
        // 1. Lấy hóa đơn và kiểm tra tồn tại
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime now = LocalDateTime.now();

        // 2. Cập nhật trạng thái hóa đơn
        invoice.setStatus(TrangThaiTong.THANH_CONG); // Đã thanh toán
        invoice.setIsPaid(true);
        invoice.setUpdatedDate(new Date());

        // 2. Trừ tồn kho theo chi tiết hóa đơn
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice ,1);
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

        Invoice invoice = detail.getInvoice();
        if (invoice == null) {
            throw new RuntimeException("Hóa đơn không tồn tại");
        }

        // Đánh dấu chi tiết là đã xóa
        detail.setStatus(2);
        invoiceDetailRepository.save(detail);

        // Tính lại tổng tiền chỉ với sản phẩm còn active
        List<InvoiceDetail> activeDetails = invoice.getInvoiceDetails().stream()
                .filter(d -> d.getStatus() == null || d.getStatus() != 2)
                .collect(Collectors.toList());

        BigDecimal newTotalAmount = activeDetails.stream()
                .map(d -> d.getProductDetail().getSellPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(newTotalAmount);

        // Nếu giỏ hàng rỗng → reset giảm giá
        if (activeDetails.isEmpty()) {
            invoice.setDiscountAmount(BigDecimal.ZERO);
        }

        // Tính lại số tiền phải trả
        BigDecimal discountAmount = invoice.getDiscountAmount() == null ? BigDecimal.ZERO : invoice.getDiscountAmount();
        invoice.setFinalAmount(newTotalAmount.subtract(discountAmount).max(BigDecimal.ZERO));

        invoiceRepository.save(invoice);
    }



    @Transactional
    public InvoiceDisplayResponse addInvoiceDetails(Long invoiceId, Long productDetailId, Integer quantity, Integer discountPercentage) {
        // 1. Lấy invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // 2. Lấy productDetail
        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại"));

        // 3. Kiểm tra tồn kho
        if (productDetail.getQuantity() < quantity) {
            throw new RuntimeException("Số lượng trong kho không đủ");
        }

        LocalDateTime now = LocalDateTime.now();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        BigDecimal sellPrice = productDetail.getSellPrice();
        BigDecimal discountedPrice = sellPrice;
        if (discountPercentage != null && discountPercentage > 0) {
            BigDecimal discountAmount = sellPrice.multiply(BigDecimal.valueOf(discountPercentage))
                    .divide(BigDecimal.valueOf(100));
            discountedPrice = sellPrice.subtract(discountAmount);
        }

        // 4. Tìm xem đã có sản phẩm này chưa
        InvoiceDetail invoiceDetail = invoiceDetailRepository.findByInvoiceAndProductDetail(invoice, productDetail)
                .orElse(null);

        if (invoiceDetail != null) {
            if (invoiceDetail.getStatus() == 2) {
                // Đã bị xóa mềm → khôi phục
                invoiceDetail.setStatus(1);
                invoiceDetail.setQuantity(quantity); // gán số lượng mới hoặc cộng thêm tuỳ nghiệp vụ
            } else {
                // Đang còn trong giỏ → cộng số lượng
                invoiceDetail.setQuantity(invoiceDetail.getQuantity() + quantity);
            }
            invoiceDetail.setDiscountPercentage(discountPercentage);
            invoiceDetail.setDiscountedPrice(discountedPrice);
            invoiceDetail.setUpdatedDate(now);
            invoiceDetail.setUpdatedBy(username);
        } else {
            // Chưa có → thêm mới
            invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductDetail(productDetail);
            invoiceDetail.setQuantity(quantity);
            invoiceDetail.setSellPrice(sellPrice);
            invoiceDetail.setDiscountPercentage(discountPercentage);
            invoiceDetail.setDiscountedPrice(discountedPrice);
            invoiceDetail.setStatus(1);
            invoiceDetail.setCreatedDate(now);
            invoiceDetail.setCreatedBy(username);

            long count = invoiceDetailRepository.count() + 1;
            invoiceDetail.setInvoiceCodeDetail(String.format("INV-D-%04d", count));
        }

        invoiceDetailRepository.save(invoiceDetail);

        // 5. Áp dụng giảm giá mới cho toàn bộ hoá đơn
        applyDiscountToInvoiceDetails(invoice);

        // 6. Cập nhật tổng tiền
        updateInvoiceTotal(invoice);

        // 7. Lấy lại dữ liệu trả về

        List<InvoiceDetail> allDetails = invoiceDetailRepository.findByInvoiceAndStatus(invoice ,1);
        return invoiceMapper.toInvoiceDisplayResponse(invoice, allDetails);
    }

    @Transactional
    public InvoiceDisplayResponse updateInvoiceDetailQuantity(Long invoiceDetailId, Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        // 1. Lấy invoiceDetail
        InvoiceDetail invoiceDetail = invoiceDetailRepository.findById(invoiceDetailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết hóa đơn không tồn tại"));

        // 2. Kiểm tra tồn kho của productDetail
        ProductDetail productDetail = invoiceDetail.getProductDetail();
        if (productDetail.getQuantity() < newQuantity) {
            throw new RuntimeException("Số lượng trong kho không đủ");
        }

        // 3. Cập nhật số lượng và thông tin sửa đổi
        invoiceDetail.setQuantity(newQuantity);
        invoiceDetail.setUpdatedDate(LocalDateTime.now());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        invoiceDetail.setUpdatedBy(username);

        invoiceDetailRepository.save(invoiceDetail);

        // 4. Áp dụng giảm giá (nếu có) cho toàn bộ hóa đơn
        Invoice invoice = invoiceDetail.getInvoice();
        applyDiscountToInvoiceDetails(invoice);

        // 5. Cập nhật tổng tiền của hóa đơn
        updateInvoiceTotal(invoice);

        // 6. Trả về dữ liệu mới
        List<InvoiceDetail> allDetails = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        return invoiceMapper.toInvoiceDisplayResponse(invoice, allDetails);
    }

    @Transactional
    public Invoice applyVoucherToInvoice(Long invoiceId, String voucherCode) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Validate voucher
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStatus() != 1) {
            throw new RuntimeException("Voucher không khả dụng");
        }
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Voucher đã hết hạn");
        }
        if (invoice.getTotalAmount().compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Không đạt giá trị tối thiểu để áp dụng voucher");
        }
        if (voucher.getCustomer() != null && invoice.getCustomer() != null
                && voucher.getCustomer().getId() != null
                && !voucher.getCustomer().getId().equals(invoice.getCustomer().getId())) {
            throw new RuntimeException("Voucher không áp dụng cho khách hàng này");
        }

        // Lấy chi tiết hoá đơn để tính giảm giá sản phẩm
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

        // Tính tổng discount từ sản phẩm
        BigDecimal productDiscount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO; // tổng tiền gốc
        for (InvoiceDetail detail : details) {
            BigDecimal quantity = BigDecimal.valueOf(detail.getQuantity());
            BigDecimal itemTotal = detail.getSellPrice().multiply(quantity);
            BigDecimal discountedTotal = detail.getDiscountedPrice().multiply(quantity);

            totalAmount = totalAmount.add(itemTotal);
            productDiscount = productDiscount.add(itemTotal.subtract(discountedTotal));
        }

        // Tính discount từ voucher
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (voucher.getDiscountPercentage() != null && voucher.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            voucherDiscount = totalAmount.multiply(voucher.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);

            if (voucher.getMaxDiscountValue() != null && voucherDiscount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                voucherDiscount = voucher.getMaxDiscountValue();
            }
        } else if (voucher.getDiscountAmount() != null && voucher.getDiscountAmount() > 0) {
            voucherDiscount = BigDecimal.valueOf(voucher.getDiscountAmount());
        }

        // Tổng giảm giá = giảm giá sản phẩm + giảm giá voucher
        BigDecimal totalDiscount = productDiscount.add(voucherDiscount);

        // finalAmount = tổng tiền gốc - tổng giảm giá
        BigDecimal finalAmount = totalAmount.subtract(totalDiscount);

        // Cập nhật invoice
        invoice.setVoucher(voucher);
        invoice.setTotalAmount(totalAmount);          // luôn là tổng tiền gốc
        invoice.setDiscountAmount(totalDiscount);     // tổng giảm giá (sản phẩm + voucher)
        invoice.setFinalAmount(finalAmount);          // khách cần trả
        invoice.setUpdatedDate(new Date());

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Lưu lịch sử voucher
        VoucherHistory history = new VoucherHistory();
        history.setVoucher(voucher);
        history.setInvoice(invoice);
        history.setCustomer(invoice.getCustomer());
        history.setUsedAt(now);
        history.setDiscountValueApplied(voucherDiscount); // chỉ lưu phần voucher
        history.setStatus(0);
        voucherHistoryRepository.save(history);

        return updatedInvoice;
    }

    @Transactional
    public Invoice applyBestVoucherToInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        if (details.isEmpty()) {
            throw new RuntimeException("Hóa đơn không có sản phẩm để áp dụng voucher");
        }

        // Tính lại tổng tiền gốc và chiết khấu sản phẩm
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal productDiscount = BigDecimal.ZERO;

        for (InvoiceDetail detail : details) {
            BigDecimal quantity = BigDecimal.valueOf(detail.getQuantity());
            BigDecimal itemTotal = detail.getSellPrice().multiply(quantity);
            BigDecimal discountedTotal = detail.getDiscountedPrice().multiply(quantity);

            totalAmount = totalAmount.add(itemTotal);
            productDiscount = productDiscount.add(itemTotal.subtract(discountedTotal));
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setUpdatedDate(new Date());

        Customer customer = invoice.getCustomer();
        if (customer == null) {
            // Không có khách => không áp dụng voucher
            invoice.setVoucher(null);
            invoice.setDiscountAmount(productDiscount);
            invoice.setFinalAmount(totalAmount.subtract(productDiscount));
            return invoiceRepository.save(invoice);
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal finalTotalAmount = totalAmount;

        List<Voucher> candidates = voucherRepository.findAll().stream()
                .filter(v -> v.getStatus() != null && v.getStatus() == 1)
                .filter(v -> v.getQuantity() == null || v.getQuantity() > 0)
                .filter(v -> now.isAfter(v.getStartDate()) && now.isBefore(v.getEndDate()))
                .filter(v -> v.getMinOrderValue() != null && finalTotalAmount.compareTo(v.getMinOrderValue()) >= 0)
                .filter(v -> v.getCustomer() == null || v.getCustomer().getId().equals(customer.getId()))
                .filter(v -> !voucherHistoryRepository.existsByVoucherAndCustomerAndInvoiceNot(v, customer, invoice))
                .collect(Collectors.toList());

        Voucher bestVoucher = null;
        BigDecimal bestVoucherDiscount = BigDecimal.ZERO;

        for (Voucher v : candidates) {
            BigDecimal discount = BigDecimal.ZERO;

            if (v.getDiscountPercentage() != null && v.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                discount = totalAmount.multiply(v.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

                if (v.getMaxDiscountValue() != null && discount.compareTo(v.getMaxDiscountValue()) > 0) {
                    discount = v.getMaxDiscountValue();
                }
            } else if (v.getDiscountAmount() != null && BigDecimal.valueOf(v.getDiscountAmount()).compareTo(BigDecimal.ZERO) > 0) {
                discount = BigDecimal.valueOf(v.getDiscountAmount());
            }

            if (discount.compareTo(bestVoucherDiscount) > 0) {
                bestVoucher = v;
                bestVoucherDiscount = discount;
            }
        }

        // Áp dụng voucher tốt nhất (nếu có)
        BigDecimal totalDiscount = productDiscount.add(bestVoucherDiscount);
        BigDecimal finalAmount = totalAmount.subtract(totalDiscount);

        invoice.setVoucher(bestVoucher);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setFinalAmount(finalAmount);

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Lưu lịch sử sử dụng nếu có voucher được áp
        if (bestVoucher != null) {
            VoucherHistory history = new VoucherHistory();
            history.setVoucher(bestVoucher);
            history.setInvoice(invoice);
            history.setCustomer(customer);
            history.setUsedAt(now);
            history.setDiscountValueApplied(bestVoucherDiscount);
            history.setStatus(0); // đã sử dụng
            voucherHistoryRepository.save(history);
        }

        return updatedInvoice;
    }

    @Transactional
    public Invoice removeVoucherFromInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Kiểm tra nếu hóa đơn không có voucher thì báo lỗi
        if (invoice.getVoucher() == null) {
            throw new RuntimeException("Hóa đơn hiện không có voucher để bỏ");
        }

        // Xóa voucher
        invoice.setVoucher(null);
        invoice.setUpdatedDate(new Date());
        invoice.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Tính lại tổng: discount chỉ còn giảm giá sản phẩm (nếu có)
        updateInvoiceTotal(invoice);

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
        TrangThaiTong statusEnum = TrangThaiTong.tuMa(status);
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
        Page<Invoice> invoicePage = invoiceRepository.findByStatusAndEmployeeId(statusEnum, employee.getId(), pageable);

        return invoicePage.map(invoiceMapper::toInvoiceResponse);
    }

    @Override
    public InvoiceDisplayResponse getInvoiceWithDetails(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoá đơn"));

        // Log ID khách hàng
        if (invoice.getCustomer() != null) {
            System.out.println("ID khách hàng gắn với hóa đơn: " + invoice.getCustomer().getId());
        } else {
            System.out.println("Hóa đơn không có khách hàng.");
        }

        // Lấy danh sách chi tiết chỉ có status = 1
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        List<InvoiceDetailResponse> detailResponses = invoiceMapper.toInvoiceDetailResponseList(details);

        InvoiceResponse invoiceResponse = invoiceMapper.toInvoiceResponse(invoice);
        return new InvoiceDisplayResponse(invoiceResponse, detailResponses);
    }


    private double getBestDiscountPercentageForProductCode(String productCode, List<DiscountCampaign> campaigns) {
        return campaigns.stream()
                .filter(campaign -> campaign.getDiscountPercentage() != null)
                .flatMap(campaign -> {
                    if (campaign.getProducts() != null) {
                        return campaign.getProducts().stream()
                                .filter(dcp -> dcp.getProduct() != null
                                        && dcp.getProduct().getProductCode().equals(productCode))
                                .map(dcp -> campaign.getDiscountPercentage().doubleValue());
                    }
                    return Stream.empty();
                })
                .max(Double::compare)
                .orElse(0.0);
    }

    private BigDecimal calculateDiscountAmount(BigDecimal price, int discountPercentage) {
        if (price == null || discountPercentage <= 0) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(discountPercentage)).divide(BigDecimal.valueOf(100));
    }

    @Override
    public Page<InvoiceDisplayResponse> getInvoiceDisplays(Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findAll(pageable);

        List<InvoiceDisplayResponse> displayResponses = invoices.stream()
                .map(invoice -> {
                    List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());
                    return invoiceMapper.toInvoiceDisplayResponse(invoice, details);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(displayResponses, pageable, invoices.getTotalElements());
    }

//    private InvoiceResponse convertToResponse(Invoice invoice) {
//        InvoiceResponse response = new InvoiceResponse();
//        response.setId(invoice.getId());
//        response.setInvoiceCode(invoice.getInvoiceCode());
//        if (invoice.getCustomer() != null) {
//            response.setPhone(invoice.getCustomer().getPhone());
//            response.setCustomerName(invoice.getCustomer().getCustomerName());
//            response.setCustomerId(invoice.getCustomer().getId()); // 👈 Thêm dòng này
//        }
//        response.setCreatedDate(invoice.getCreatedDate());
//        response.setTotalAmount(invoice.getTotalAmount());
//        response.setDiscountAmount(invoice.getDiscountAmount());
//        response.setFinalAmount(invoice.getFinalAmount());
//        response.setDescription(invoice.getDescription());
//        response.setOrderType(invoice.getOrderType());
//        response.setStatus(invoice.getStatus());
//        response.setCreatedBy(invoice.getCreatedBy());
//        response.setUpdatedBy(invoice.getUpdatedBy());
//        response.setUpdatedDate(invoice.getUpdatedDate());
//
//        if (invoice.getEmployee() != null) {
//            response.setEmployeeName(invoice.getEmployee().getEmployeeName());
//        }
//
//        return response;
//    }
//
//    private InvoiceDetailResponse convertToDetailResponse(InvoiceDetail detail) {
//        InvoiceDetailResponse response = new InvoiceDetailResponse();
//
//        response.setId(detail.getId());
//        response.setProductName(detail.getProductDetail().getProduct().getProductName());
//        response.setQuantity(detail.getQuantity());
//        response.setSellPrice(detail.getInvoice().getTotalAmount());
//
//        // 👉 Thêm tên khách hàng
//        if (detail.getInvoice().getCustomer() != null) {
//            response.setCustomerName(detail.getInvoice().getCustomer().getCustomerName());
//        } else {
//            response.setCustomerName("Khách lẻ"); // hoặc để null tùy bạn
//        }
//
//        return response;
//    }

    @Override
    public Page<InvoiceResponse> searchInvoices(String keyword, Integer status,
                                                LocalDate createdDate,
                                                Pageable pageable) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        LocalDateTime startOfDay = null;
        LocalDateTime startOfNextDay = null;
        TrangThaiTong statusEnum = TrangThaiTong.tuMa(status);
        if (createdDate != null) {
            startOfDay = createdDate.atStartOfDay();
            startOfNextDay = createdDate.plusDays(1).atStartOfDay();
        }

        // Gọi repository để tìm kiếm
        Page<Invoice> page = invoiceRepository.searchByKeywordStatusAndCreatedDate(
                keyword, statusEnum, startOfDay, startOfNextDay, pageable
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

        // 2. Địa chỉ
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
        invoice.setCreatedDate(new Date());
        invoice.setUpdatedDate(new Date());
        invoice.setDescription(request.getDescription());
        invoice.setOrderType(request.getOrderType());
        invoice.setIsPaid(false);
        invoice.setStatus(TrangThaiTong.DANG_XU_LY);
        invoice.setStatusDetail(TrangThaiChiTiet.CHO_XU_LY);
        invoice.setDiscountAmount(Optional.ofNullable(request.getDiscountAmount()).orElse(BigDecimal.ZERO));
        invoice.setShippingFee(Optional.ofNullable(request.getShippingFee()).orElse(BigDecimal.ZERO));

        if (request.getEmployeeId() != null) {
            employeeRepository.findById(request.getEmployeeId()).ifPresent(invoice::setEmployee);
        }

        if (request.getVoucherId() != null) {
            voucherRepository.findById(request.getVoucherId()).ifPresent(invoice::setVoucher);
        }

        // 4. Xử lý sản phẩm
        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceDetail> details = new ArrayList<>();
        Set<ProductDetail> productDetailsToUpdate = new HashSet<>();
        Set<Product> productsToUpdate = new HashSet<>();

        for (CartItemRequest item : request.getItems()) {
            ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + item.getProductDetailId()));

            int currentStock = productDetail.getQuantity();
            if (item.getQuantity() > currentStock) {
                throw new RuntimeException("Số lượng sản phẩm vượt quá tồn kho: " + item.getProductDetailId());
            }
            productDetail.setQuantity(currentStock - item.getQuantity());
            productDetailsToUpdate.add(productDetail);

            Product product = productDetail.getProduct();
            int currentProductStock = product.getQuantity() != null ? product.getQuantity() : 0;
            if (item.getQuantity() > currentProductStock) {
                throw new RuntimeException("Tồn kho tổng không đủ cho sản phẩm: " + product.getId());
            }
            product.setQuantity(currentProductStock - item.getQuantity());
            productsToUpdate.add(product);

            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setProductDetail(productDetail);
            detail.setQuantity(item.getQuantity());
            detail.setCreatedDate(LocalDateTime.now());
            detail.setStatus(0);
            detail.setInvoiceCodeDetail("INV-DTL-" + UUID.randomUUID().toString().substring(0, 8));

            detail.setSellPrice(item.getSellPrice() != null ? item.getSellPrice() : productDetail.getSellPrice());
            detail.setDiscountedPrice(item.getDiscountedPrice() != null ? item.getDiscountedPrice() : productDetail.getSellPrice());
            detail.setDiscountPercentage(item.getDiscountPercentage() != null ? item.getDiscountPercentage() : 0);

            BigDecimal itemTotal = detail.getDiscountedPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);

            details.add(detail);
        }

        invoice.setTotalAmount(total);
        invoice.setFinalAmount(total
                .subtract(invoice.getDiscountAmount())
                .add(invoice.getShippingFee()));
        invoice.setInvoiceDetails(details);

        // 5. Lưu dữ liệu
        Invoice savedInvoice = invoiceRepository.save(invoice);
        productDetailRepository.saveAll(productDetailsToUpdate);
        productRepository.saveAll(productsToUpdate);

        processInvoicePayment(savedInvoice.getId());

        // 6. Trả về kết quả
        return invoiceMapper.toInvoiceDisplayResponse(savedInvoice, savedInvoice.getInvoiceDetails());
    }


    @Transactional
    @Override
    public InvoiceDisplayResponse createInvoiceShipCode(InvoiceRequest request) {
        try {
            // 1. Xử lý khách hàng
            Customer customer;
            Long customerId = request.getCustomerInfo().getCustomerId();
            String phone = request.getCustomerInfo().getPhone();

            if (customerId != null) {
                customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + customerId));
            } else if (phone != null && !phone.isBlank()) {
                customer = customerRepository.findTop1ByPhoneAndStatus(phone, 1).orElse(null);

                if (customer == null) {
                    customer = new Customer();
                    customer.setCustomerName(request.getCustomerInfo().getCustomerName());
                    customer.setPhone(phone);
                    customer.setEmail(request.getCustomerInfo().getEmail());
                    customer.setStatus(1);
                    customer.setCustomerCode("KH" + System.currentTimeMillis());
                    customer.setCreatedDate(LocalDateTime.now());

                    // ✅ Fix lỗi null addressList
                    customer.setAddressList(new ArrayList<>());

                    customer = customerRepository.save(customer);
                }
            } else {
                throw new RuntimeException("Thiếu thông tin khách hàng (cần có customerId hoặc phone)");
            }

            // 2. Xử lý địa chỉ khách hàng nếu có
            AddressRequest addr = request.getCustomerInfo().getAddress();
            if (addr != null) {
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
            }

            // 3. Tạo hóa đơn
            Invoice invoice = new Invoice();
            invoice.setInvoiceCode("INV" + System.currentTimeMillis());
            invoice.setCustomer(customer);
            invoice.setCreatedDate(new Date());
            invoice.setUpdatedDate(new Date());
            invoice.setDescription(request.getDescription());
            invoice.setOrderType(request.getOrderType());
            invoice.setIsPaid(false);
            invoice.setStatus(TrangThaiTong.DANG_XU_LY);
            invoice.setStatusDetail(TrangThaiChiTiet.CHO_XU_LY);
            invoice.setDiscountAmount(Optional.ofNullable(request.getDiscountAmount()).orElse(BigDecimal.ZERO));
            invoice.setShippingFee(Optional.ofNullable(request.getShippingFee()).orElse(BigDecimal.ZERO));

            if (request.getEmployeeId() != null) {
                employeeRepository.findById(request.getEmployeeId()).ifPresent(invoice::setEmployee);
            }

            if (request.getVoucherId() != null) {
                voucherRepository.findById(request.getVoucherId()).ifPresent(invoice::setVoucher);
            }

            // 4. Xử lý danh sách sản phẩm
            BigDecimal total = BigDecimal.ZERO;
            List<InvoiceDetail> details = new ArrayList<>();

            for (CartItemRequest item : request.getItems()) {
                ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết ID: " + item.getProductDetailId()));

                // Kiểm tra tồn kho chi tiết
                if (item.getQuantity() > productDetail.getQuantity()) {
                    throw new RuntimeException("Số lượng vượt quá tồn kho chi tiết sản phẩm: " + item.getProductDetailId());
                }
                productDetail.setQuantity(productDetail.getQuantity() - item.getQuantity());

                // Kiểm tra tồn kho tổng
                Product product = productDetail.getProduct();
                int stock = Optional.ofNullable(product.getQuantity()).orElse(0);
                if (item.getQuantity() > stock) {
                    throw new RuntimeException("Tồn kho tổng không đủ cho sản phẩm: " + product.getId());
                }
                product.setQuantity(stock - item.getQuantity());

                // Tạo chi tiết hóa đơn
                InvoiceDetail detail = new InvoiceDetail();
                detail.setInvoice(invoice);
                detail.setProductDetail(productDetail);
                detail.setQuantity(item.getQuantity());
                detail.setCreatedDate(LocalDateTime.now());
                detail.setStatus(0);
                detail.setInvoiceCodeDetail("INV-DTL-" + UUID.randomUUID().toString().substring(0, 8));

                BigDecimal sellPrice = Optional.ofNullable(item.getSellPrice()).orElse(productDetail.getSellPrice());
                BigDecimal discountedPrice = Optional.ofNullable(item.getDiscountedPrice()).orElse(sellPrice);
                detail.setSellPrice(sellPrice);
                detail.setDiscountedPrice(discountedPrice);
                detail.setDiscountPercentage(Optional.ofNullable(item.getDiscountPercentage()).orElse(0));

                BigDecimal itemTotal = discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);

                details.add(detail);
            }

            invoice.setTotalAmount(total);
            invoice.setFinalAmount(total
                    .subtract(invoice.getDiscountAmount())
                    .add(invoice.getShippingFee()));
            invoice.setInvoiceDetails(details);

            // 5. Lưu hóa đơn
            Invoice savedInvoice = invoiceRepository.save(invoice);

            // 6. Giao dịch thanh toán (Ship COD)
            InvoiceTransaction transaction = new InvoiceTransaction();
            transaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
            transaction.setInvoice(savedInvoice);
            transaction.setAmount(savedInvoice.getFinalAmount());
            transaction.setPaymentStatus(1); // chờ thanh toán
            transaction.setPaymentMethod("Thanh toán khi nhận hàng");
            transaction.setTransactionType("Thanh toán sau");
            transaction.setNote(null);
            transaction.setPaymentTime(new Date());
            invoiceTransactionRepository.save(transaction);

            // 7. Xử lý hậu thanh toán (áp dụng khuyến mãi, cập nhật tồn kho v.v.)
            processInvoicePayment(savedInvoice.getId());

            // 8. Trả kết quả về FE
            return invoiceMapper.toInvoiceDisplayResponse(savedInvoice, savedInvoice.getInvoiceDetails());

        } catch (Exception e) {
            e.printStackTrace(); // Ghi log
            throw new RuntimeException("Lỗi khi tạo đơn hàng: " + e.getMessage());
        }
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
            invoice.setUpdatedDate(new Date());
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
    public void updateInvoiceStatusByAppTransId(String appTransId, Integer status,Integer statusDetail,Boolean isPaid) {
        TrangThaiTong statusEnum = TrangThaiTong.tuMa(status);
        TrangThaiChiTiet statusDetailEnum = TrangThaiChiTiet.tuMa(statusDetail);
        invoiceRepository.findByAppTransId(appTransId).ifPresent(invoice -> {
            invoice.setIsPaid(isPaid);
            invoice.setStatus(statusEnum);
            invoice.setStatusDetail(statusDetailEnum);
            invoice.setUpdatedDate(new Date());

            InvoiceTransaction invoiceTransaction = new InvoiceTransaction();
            invoiceTransaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
            invoiceTransaction.setInvoice(invoice);
            invoiceTransaction.setAmount(invoice.getFinalAmount());
            invoiceTransaction.setPaymentStatus(1);
            invoiceTransaction.setPaymentMethod("Chuyển khoản");
            invoiceTransaction.setTransactionType("Thanh toán trước");
            invoiceTransaction.setNote(null);
            invoiceTransaction.setPaymentTime(new Date());
            invoiceTransactionRepository.save(invoiceTransaction);
            invoiceRepository.save(invoice);
        });
    }

    @Override
    public void updateStatusIfPaid(String appTransId) throws Exception {
        JSONObject response = zaloPayService.queryOrder(appTransId);

        int returnCode = response.optInt("returncode");
        int bcTransStatus = response.optInt("bctransstatus");

        if (returnCode == 1 && bcTransStatus == 1) {
            updateInvoiceStatusByAppTransId(appTransId, 0,0,true); // PAID
            log.info(" Đơn {} đã được cập nhật thành PAID từ kết quả query ZaloPay", appTransId);
        }else if (returnCode != 1) {
            updateInvoiceStatusByAppTransId(appTransId, -1,-1,false); // FAIL
        }  else {
            log.warn(" Đơn {} chưa thanh toán (status={})", appTransId, bcTransStatus);
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
            invoice.setAppTransId(orderId); // dùng lại trường appTransId
            invoice.setUpdatedDate(new Date());
            invoiceRepository.save(invoice);
        });

        return new InvoiceWithVnpayResponse(invoiceDisplay, vnpayResponse);
    }

    @Override
    public Page<InvoiceResponse> searchInvoices(Integer status,Integer orderType, LocalDateTime createdFrom, LocalDateTime createdTo, String phone, String code, Pageable pageable) {
        System.out.println("status service: "+status);
                TrangThaiChiTiet statusEnum  = TrangThaiChiTiet.tuMa(status);
                System.out.println("status enum: "+statusEnum);
        Page<InvoiceResponse> invoices = invoiceRepository.searchInvoices(statusEnum,orderType,createdFrom,createdTo,phone,code,pageable);
        return invoices;
    }

    public PurchaseStats getCustomerPurchaseStats(Long customerId) {
        List<Invoice> invoices = invoiceRepository.findSuccessfulOnlineInvoicesByCustomerId(customerId, TrangThaiTong.THANH_CONG);

        BigDecimal total = invoices.stream()
                .map(Invoice::getFinalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int count = invoices.size();

        PurchaseStats stats = new PurchaseStats();
        stats.setInvoiceCount(count);
        stats.setTotalSpent(total);
        return stats;
    }

    public CustomerTier classifyCustomer(PurchaseStats stats) {
        BigDecimal total = stats.getTotalSpent();
        int count = stats.getInvoiceCount();

        if (count >= 20 && total.compareTo(BigDecimal.valueOf(20_000_000)) >= 0) {
            return CustomerTier.VIP;
        } else if (count >= 10 && total.compareTo(BigDecimal.valueOf(10_000_000)) >= 0) {
            return CustomerTier.THAN_THIET;
        } else if (count >= 3 && total.compareTo(BigDecimal.valueOf(3_000_000)) >= 0) {
            return CustomerTier.THANH_VIEN_THUONG;
        } else {
            return CustomerTier.MOI;
        }
    }

    @Override
    public PromotionSuggestion getSuggestedPromotion(Long customerId) {
        PurchaseStats stats = getCustomerPurchaseStats(customerId);
        CustomerTier tier = classifyCustomer(stats);

        PromotionSuggestion suggestion = new PromotionSuggestion();
        suggestion.setTier(tier);
        suggestion.setInvoiceCount(stats.getInvoiceCount());

        switch (tier) {
            case VIP -> {
                suggestion.setMessage("Chúc mừng! Bạn là khách VIP. Ưu đãi 100K dành riêng cho bạn!");
                suggestion.setCouponCode("VIP20");
                suggestion.setDiscountAmount(BigDecimal.valueOf(100_000));
            }
            case THAN_THIET -> {
                suggestion.setMessage("Cảm ơn bạn đã đồng hành! Nhận ưu đãi 50K.");
                suggestion.setCouponCode("LOYAL10");
                suggestion.setDiscountAmount(BigDecimal.valueOf(50_000));
            }
            case THANH_VIEN_THUONG -> {
                suggestion.setMessage("Tiếp tục mua sắm để lên hạng cao hơn! Nhận ưu đãi 30K.");
                suggestion.setCouponCode("REGULAR5");
                suggestion.setDiscountAmount(BigDecimal.valueOf(30_000));
            }
            case MOI -> {
                suggestion.setMessage("Ưu đãi chào mừng! Giảm ngay 20K cho đơn hàng tiếp theo.");
                suggestion.setCouponCode("WELCOME");
                suggestion.setDiscountAmount(BigDecimal.valueOf(20_000));
            }
        }

        return suggestion;
    }

    @Transactional
    @Override
    public void processInvoicePayment(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        invoice.setStatus(TrangThaiTong.THANH_CONG);
        LocalDateTime now = LocalDateTime.now();
        Date updatedDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        invoice.setUpdatedDate(updatedDate);

        invoiceRepository.save(invoice);

        // Gửi tặng voucher nếu đủ điều kiện
        checkAndGiftVoucher(invoice.getCustomer().getId());
    }

    @Override
    public void checkAndGiftVoucher(Long customerId) {
        PurchaseStats stats = getCustomerPurchaseStats(customerId);
        CustomerTier tier = classifyCustomer(stats);

        switch (tier) {
            case VIP -> {
                if (!hasVoucher(customerId, 100000)) {
                    createVoucher(customerId, "VIP20", "Chúc mừng! Bạn là khách VIP. Ưu đãi 100K dành riêng cho bạn!", 100000);
                }
            }
            case THAN_THIET -> {
                if (!hasVoucher(customerId, 50000)) {
                    createVoucher(customerId, "LOYAL10", "Cảm ơn bạn đã đồng hành! Nhận ưu đãi 50K.", 50000);
                }
            }
            case THANH_VIEN_THUONG -> {
                if (!hasVoucher(customerId, 30000)) {
                    createVoucher(customerId, "REGULAR5", "Tiếp tục mua sắm để lên hạng cao hơn! Nhận ưu đãi 30K.", 30000);
                }
            }
            case MOI -> {
                if (!hasVoucher(customerId, 20000)) {
                    createVoucher(customerId, "WELCOME", "Ưu đãi chào mừng! Giảm ngay 20K cho đơn hàng tiếp theo.", 20000);
                }
            }
        }
    }

    private boolean hasVoucher(Long customerId, int amount) {
        return voucherRepository.existsByCustomerIdAndDiscountAmount(customerId, amount);
    }

    private void createVoucher(Long customerId, String code, String name, int discountAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Voucher voucher = new Voucher();
        voucher.setCustomer(customer);
        voucher.setVoucherCode(code + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()); // eg: VIP20-4F7A1B
        voucher.setVoucherName(name);
        voucher.setDiscountAmount(discountAmount);
        voucher.setMinOrderValue(BigDecimal.ZERO);
        voucher.setStartDate(LocalDateTime.now());
        voucher.setEndDate(LocalDateTime.now().plusDays(30));
        voucher.setCreatedDate(LocalDateTime.now());
        voucher.setStatus(1); // đang hoạt động

        voucherRepository.save(voucher);
    }


}

