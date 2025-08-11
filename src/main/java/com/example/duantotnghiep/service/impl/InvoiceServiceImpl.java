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
import com.example.duantotnghiep.model.CustomerBlacklistHistory;
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
import com.example.duantotnghiep.repository.CustomerBlacklistHistoryRepository;
import com.example.duantotnghiep.repository.CustomerRepository;
import com.example.duantotnghiep.repository.DiscountCampaignProductRepository;
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
    private final CustomerBlacklistHistoryRepository customerBlacklistHistoryRepository;
    private final DiscountCampaignProductRepository discountCampaignProductRepository;
//    private final InvoiceService invoiceService;

    @Transactional
    @Override
    public InvoiceResponse createEmptyInvoice() {
        String username = currentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        Employee employee = user.getEmployee();
        if (employee == null) throw new RuntimeException("Người dùng không phải là nhân viên.");

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(safeInvoiceCode()); // tránh count()+1
        invoice.setEmployee(employee);
        invoice.setCreatedDate(new Date());
        invoice.setUpdatedDate(new Date());
        invoice.setStatus(TrangThaiTong.DANG_XU_LY);
        invoice.setStatusDetail(TrangThaiChiTiet.CHO_XU_LY);
        invoice.setTotalAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setDiscountAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setFinalAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setShippingFee(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setCreatedBy(username);
        invoice.setDescription("Hóa đơn bán tại quầy");
        invoice.setOrderType(0);
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
        LocalDateTime now = LocalDateTime.now();
        List<DiscountCampaign> activeCampaigns = discountCampaignRepository.findActiveCampaigns(now);
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

        String username = currentUsername();

        for (InvoiceDetail d : details) {
            BigDecimal sellPrice = money(d.getSellPrice());
            if (sellPrice.compareTo(BigDecimal.ZERO) < 0) sellPrice = BigDecimal.ZERO;

            int percent = 0;
            if (d.getDiscountCampaign() != null) {
                DiscountCampaign dc = d.getDiscountCampaign();
                if (dc.getDiscountPercentage() != null && isActive(now, dc.getStartDate(), dc.getEndDate())) {
                    percent = clampPercent(dc.getDiscountPercentage().intValue());
                }
            }
            if (percent == 0) {
                double best = getBestDiscountPercentageForProductCode(
                        d.getProductDetail().getProduct().getProductCode(), activeCampaigns);
                percent = clampPercent((int) Math.round(best));
            }

            BigDecimal discountAmount = sellPrice.multiply(BigDecimal.valueOf(percent))
                    .divide(BigDecimal.valueOf(100), MONEY_SCALE, RM);
            BigDecimal discountedPrice = money(sellPrice.subtract(discountAmount));
            if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) discountedPrice = BigDecimal.ZERO;

            d.setSellPrice(sellPrice);
            d.setDiscountPercentage(percent);
            d.setDiscountedPrice(discountedPrice);
            d.setUpdatedDate(now);
            d.setUpdatedBy(username);
            invoiceDetailRepository.save(d);
        }
    }

    @Transactional
    public void updateInvoiceTotal(Invoice invoice) {
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

        BigDecimal totalAmount = BigDecimal.ZERO;          // tổng gốc
        BigDecimal productDiscount = BigDecimal.ZERO;      // giảm theo sản phẩm
        BigDecimal subtotalAfterProduct = BigDecimal.ZERO; // sau giảm sản phẩm

        for (InvoiceDetail d : details) {
            BigDecimal qty = BigDecimal.valueOf(d.getQuantity());
            BigDecimal itemTotal = money(d.getSellPrice()).multiply(qty);
            BigDecimal discounted = money(d.getDiscountedPrice()).multiply(qty);

            totalAmount = totalAmount.add(itemTotal);
            productDiscount = productDiscount.add(itemTotal.subtract(discounted));
            subtotalAfterProduct = subtotalAfterProduct.add(discounted);
        }

        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (invoice.getVoucher() != null) {
            voucherDiscount = calculateVoucherDiscountForAmount(subtotalAfterProduct, invoice.getVoucher());
            if (voucherDiscount.compareTo(subtotalAfterProduct) > 0) voucherDiscount = subtotalAfterProduct;
        }

        BigDecimal shipping = money(invoice.getShippingFee());
        BigDecimal totalDiscount = productDiscount.add(voucherDiscount);
        BigDecimal finalAmount = subtotalAfterProduct.subtract(voucherDiscount).add(shipping);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        invoice.setTotalAmount(money(totalAmount));
        invoice.setDiscountAmount(money(totalDiscount));
        invoice.setFinalAmount(money(finalAmount));
        invoice.setUpdatedDate(new Date());
        invoice.setUpdatedBy(currentUsername());
        invoiceRepository.save(invoice);
    }

    /** TÍNH VOUCHER: dựa trên subtotal sau giảm sản phẩm */
    private BigDecimal calculateVoucherDiscountForAmount(BigDecimal base, Voucher voucher) {
        if (voucher == null) return BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        if (voucher.getStatus() == null || voucher.getStatus() != 1) return BigDecimal.ZERO;
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) return BigDecimal.ZERO;
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) return BigDecimal.ZERO;

        BigDecimal minOrder = money(voucher.getMinOrderValue());
        BigDecimal amount = money(base);
        if (amount.compareTo(minOrder) < 0) return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountPercentage() != null && voucher.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discount = amount.multiply(voucher.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), MONEY_SCALE, RM);
            BigDecimal cap = money(voucher.getMaxDiscountValue());
            if (cap.compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(cap) > 0) discount = cap;
        } else if (voucher.getDiscountAmount() != null && voucher.getDiscountAmount() > 0) {
            discount = money(BigDecimal.valueOf(voucher.getDiscountAmount()));
        }
        return money(discount);
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
        if (phonePrefix == null || phonePrefix.isBlank()) return Collections.emptyList();
        return customerRepository.findByPhoneStartingWithAndStatusActive(phonePrefix)
                .stream().map(invoiceMapper::toCustomerResponse).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CustomerResponse createQuickCustomer(String phone, String name, String email) {
        if (phone == null || phone.isBlank()) throw new RuntimeException("Số điện thoại không được trống");
        Optional<Customer> existed = customerRepository.findTop1ByPhoneAndStatus(phone, 1);
        if (existed.isPresent()) return invoiceMapper.toCustomerResponse(existed.get());

        Customer c = new Customer();
        c.setCustomerName((name==null||name.isBlank()) ? "Khách lẻ" : name.trim());
        c.setPhone(phone.trim());
        c.setEmail((email==null||email.isBlank()) ? null : email.trim());
        c.setCustomerCode("CUS-" + System.currentTimeMillis());
        c.setStatus(1);
        c.setCreatedDate(LocalDateTime.now());
        c.setCreatedBy(currentUsername());
        return invoiceMapper.toCustomerResponse(customerRepository.save(c));
    }

    @Transactional
    public void assignCustomer(Long invoiceId, Long customerId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với id: " + invoiceId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + customerId));

        // Kiểm tra nếu khách hàng đã được gán vào hóa đơn khác có status = 0
        boolean isCustomerAssigned = invoiceRepository.existsByCustomer_IdAndStatusAndOrderType(customerId, TrangThaiTong.DANG_XU_LY, 0);

        if (isCustomerAssigned) {
            throw new RuntimeException("Khách hàng đã được gán vào một hóa đơn đang xử lý (status = 0)");
        }

        // Gán khách hàng vào hóa đơn
        invoice.setCustomer(customer);
        invoiceRepository.save(invoice);
    }

    @Override
    public PaymentSummary calculatePayment(Long invoiceId, BigDecimal amountGiven) {
        if (amountGiven == null || amountGiven.compareTo(BigDecimal.ZERO)<0)
            throw new RuntimeException("Số tiền đưa không hợp lệ");

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        applyDiscountToInvoiceDetails(invoice);
        updateInvoiceTotal(invoice);

        BigDecimal finalAmount = money(invoice.getFinalAmount());
        if (amountGiven.compareTo(finalAmount) < 0) throw new RuntimeException("Số tiền đưa không đủ");

        BigDecimal change = amountGiven.subtract(finalAmount);
        return new PaymentSummary(finalAmount, amountGiven, money(change));
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
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        if (invoice.getStatus() != TrangThaiTong.DANG_XU_LY)
            throw new RuntimeException("Chỉ checkout khi hóa đơn đang xử lý");

        // Tính lại giá/giảm trước khi trừ kho
        applyDiscountToInvoiceDetails(invoice);
        updateInvoiceTotal(invoice);

        String username = currentUsername();
        LocalDateTime now = LocalDateTime.now();

        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        for (InvoiceDetail d : details) {
            ProductDetail pd = productDetailRepository.findById(d.getProductDetail().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể trong kho"));
            int stock = pd.getQuantity() == null ? 0 : pd.getQuantity();
            if (stock < d.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + pd.getProduct().getProductName() + " không đủ tồn kho!");
            }
            pd.setQuantity(stock - d.getQuantity());
            pd.setUpdatedDate(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
            pd.setUpdatedBy(username);
            productDetailRepository.save(pd);
        }

        invoice.setStatus(TrangThaiTong.THANH_CONG);
        invoice.setIsPaid(true);
        invoice.setUpdatedBy(username);
        invoice.setUpdatedDate(new Date());
        invoiceRepository.save(invoice);

        handleUsedVoucher(invoice, username, now);
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

        final boolean RESERVE_STOCK_ON_ADD_POS = false; // nếu bạn có giữ kho khi add giỏ POS thì set true

        if (RESERVE_STOCK_ON_ADD_POS) {
            for (InvoiceDetail d : invoice.getInvoiceDetails()) {
                if (d.getStatus()!=null && d.getStatus()!=1) continue;
                ProductDetail pd = d.getProductDetail();
                pd.setQuantity((pd.getQuantity()==null?0:pd.getQuantity()) + d.getQuantity());
                productDetailRepository.save(pd);
            }
        }

        for (InvoiceDetail d : invoice.getInvoiceDetails()) d.setStatus(2);
        invoiceDetailRepository.saveAll(invoice.getInvoiceDetails());
        invoice.getInvoiceDetails().clear();

        invoice.setTotalAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setDiscountAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setFinalAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RM));
        invoice.setUpdatedDate(new Date());
        invoice.setUpdatedBy(currentUsername());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void deleteCartItemById(Long invoiceDetailId) {
        if (invoiceDetailId == null) {
            throw new IllegalArgumentException("Thiếu invoiceDetailId");
        }

        // Nếu bạn có khoá ghi, dùng findByIdForUpdate để tránh race
        InvoiceDetail detail = invoiceDetailRepository.findById(invoiceDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        Invoice invoice = detail.getInvoice();
        if (invoice == null) {
            throw new RuntimeException("Hóa đơn không tồn tại");
        }

        // (Tuỳ chọn) nếu bạn có cơ chế giữ kho khi thêm vào giỏ, hoàn kho khi xóa:
        final boolean RESERVE_STOCK_ON_ADD = false; // đặt true nếu bạn đã trừ kho khi add-to-cart
        if (RESERVE_STOCK_ON_ADD && detail.getStatus() != null && detail.getStatus() == 1) {
            ProductDetail pd = detail.getProductDetail();
            pd.setQuantity((pd.getQuantity() == null ? 0 : pd.getQuantity()) + detail.getQuantity());
            productDetailRepository.save(pd);
        }

        // Xóa mềm dòng giỏ hàng
        detail.setStatus(2);
        detail.setUpdatedDate(LocalDateTime.now());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        detail.setUpdatedBy(username);
        invoiceDetailRepository.save(detail);

        // Lấy lại các dòng còn active
        List<InvoiceDetail> activeDetails = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

        if (activeDetails.isEmpty()) {
            // Giỏ trống: reset toàn bộ số tiền
            invoice.setTotalAmount(BigDecimal.ZERO);
            invoice.setDiscountAmount(BigDecimal.ZERO);
            invoice.setFinalAmount(BigDecimal.ZERO);

            // Nếu có voucher/chiến dịch áp toàn hoá đơn thì có thể clear ở đây (nếu có field)
            // invoice.setVoucher(null);
            // invoice.setDiscountCampaign(null);

            invoiceRepository.save(invoice);
            return;
        }

        // Áp lại giảm giá từng dòng (nếu có logic tự tìm campaign/% giảm tốt nhất)
        applyDiscountToInvoiceDetails(invoice);

        // Tính lại tổng tiền, số tiền giảm & số tiền phải trả (gồm cả giảm giá cấp hoá đơn nếu có)
        updateInvoiceTotal(invoice);

        invoiceRepository.save(invoice);
    }

    @Transactional
    public InvoiceDisplayResponse addInvoiceDetails(
            Long invoiceId, Long productDetailId, Integer quantity,
            Integer discountPercentage, Long discountCampaignId
    ) {
        if (invoiceId == null || productDetailId == null) throw new RuntimeException("Thiếu id");
        if (quantity == null || quantity <= 0) throw new RuntimeException("Số lượng phải > 0");

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        if (invoice.getStatus() != TrangThaiTong.DANG_XU_LY)
            throw new RuntimeException("Chỉ thêm sản phẩm khi hóa đơn đang xử lý");

        ProductDetail pd = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại"));

        int available = pd.getQuantity() == null ? 0 : pd.getQuantity();
        if (available < quantity) throw new RuntimeException("Số lượng trong kho không đủ");

        // chọn campaign nếu có & còn hiệu lực
        DiscountCampaign dc = null;
        LocalDateTime now = LocalDateTime.now();
        if (discountCampaignId != null) {
            dc = discountCampaignRepository.findById(discountCampaignId)
                    .orElseThrow(() -> new RuntimeException("Chiến dịch giảm giá không tồn tại"));
            if (!isActive(now, dc.getStartDate(), dc.getEndDate())) dc = null;
        }

        int percent = clampPercent(discountPercentage);
        if (percent == 0 && dc != null && dc.getDiscountPercentage() != null) {
            percent = clampPercent(dc.getDiscountPercentage().intValue());
        }

        BigDecimal sellPrice = money(pd.getSellPrice());
        BigDecimal discountAmount = sellPrice.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), MONEY_SCALE, RM);
        BigDecimal discountedPrice = money(sellPrice.subtract(discountAmount));
        if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) discountedPrice = BigDecimal.ZERO;

        InvoiceDetail line = invoiceDetailRepository.findByInvoiceAndProductDetail(invoice, pd).orElse(null);
        String username = currentUsername();

        if (line != null) {
            if (line.getStatus() != null && line.getStatus() == 2) {
                line.setStatus(1);
                line.setQuantity(quantity);
            } else {
                line.setQuantity(line.getQuantity() + quantity);
            }
            line.setSellPrice(sellPrice);
            line.setDiscountPercentage(percent);
            line.setDiscountedPrice(discountedPrice);
            line.setDiscountCampaign(dc);
            line.setUpdatedDate(now);
            line.setUpdatedBy(username);
        } else {
            line = new InvoiceDetail();
            line.setInvoice(invoice);
            line.setProductDetail(pd);
            line.setQuantity(quantity);
            line.setSellPrice(sellPrice);
            line.setDiscountPercentage(percent);
            line.setDiscountedPrice(discountedPrice);
            line.setDiscountCampaign(dc);
            line.setStatus(1);
            line.setCreatedDate(now);
            line.setCreatedBy(username);
            line.setInvoiceCodeDetail("INV-D-" + invoice.getId() + "-" + (System.nanoTime()%100000));
        }

        invoiceDetailRepository.saveAndFlush(line);
        applyDiscountToInvoiceDetails(invoice);
        updateInvoiceTotal(invoice);

        List<InvoiceDetail> all = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        return invoiceMapper.toInvoiceDisplayResponse(invoice, all);
    }

    @Transactional
    public InvoiceDisplayResponse updateInvoiceDetailQuantity(Long invoiceDetailId, Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) throw new RuntimeException("Số lượng phải > 0");

        InvoiceDetail line = invoiceDetailRepository.findById(invoiceDetailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết hóa đơn không tồn tại"));

        ProductDetail pd = line.getProductDetail();
        int available = pd.getQuantity() == null ? 0 : pd.getQuantity();
        int delta = newQuantity - line.getQuantity();
        if (delta > 0 && available < delta) throw new RuntimeException("Số lượng trong kho không đủ");

        line.setQuantity(newQuantity);
        line.setUpdatedDate(LocalDateTime.now());
        line.setUpdatedBy(currentUsername());
        invoiceDetailRepository.save(line);

        Invoice invoice = line.getInvoice();
        applyDiscountToInvoiceDetails(invoice);
        updateInvoiceTotal(invoice);

        List<InvoiceDetail> all = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        return invoiceMapper.toInvoiceDisplayResponse(invoice, all);
    }

    @Transactional
    public Invoice applyVoucherToInvoice(Long invoiceId, String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) throw new RuntimeException("Voucher code trống");

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode.trim())
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStatus() == null || voucher.getStatus() != 1) throw new RuntimeException("Voucher không khả dụng");
        if (voucher.getStartDate()!=null && now.isBefore(voucher.getStartDate())) throw new RuntimeException("Voucher chưa hiệu lực");
        if (voucher.getEndDate()!=null && now.isAfter(voucher.getEndDate())) throw new RuntimeException("Voucher đã hết hạn");
        if (voucher.getCustomer()!=null && invoice.getCustomer()!=null
                && !voucher.getCustomer().getId().equals(invoice.getCustomer().getId()))
            throw new RuntimeException("Voucher không áp dụng cho khách hàng này");

        // Gán voucher và tính lại tổng (voucher tính trên subtotal sau giảm SP)
        invoice.setVoucher(voucher);
        applyDiscountToInvoiceDetails(invoice);
        updateInvoiceTotal(invoice);

        // Tính chính xác phần voucher để lưu history
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        BigDecimal subtotalAfterProduct = details.stream()
                .map(d -> money(d.getDiscountedPrice()).multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal voucherDiscount = calculateVoucherDiscountForAmount(subtotalAfterProduct, voucher);

        VoucherHistory hist = new VoucherHistory();
        hist.setVoucher(voucher);
        hist.setInvoice(invoice);
        hist.setCustomer(invoice.getCustomer());
        hist.setUsedAt(now);
        hist.setDiscountValueApplied(voucherDiscount);
        hist.setStatus(0); // pending, sẽ set=1 khi thanh toán
        voucherHistoryRepository.save(hist);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice applyBestVoucherToInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);
        if (details.isEmpty()) throw new RuntimeException("Hóa đơn không có sản phẩm để áp dụng voucher");

        applyDiscountToInvoiceDetails(invoice);
        updateInvoiceTotal(invoice);

        Customer customer = invoice.getCustomer();
        if (customer == null) {
            invoice.setVoucher(null);
            updateInvoiceTotal(invoice);
            return invoiceRepository.save(invoice);
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal base = details.stream()
                .map(d -> money(d.getDiscountedPrice()).multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Voucher> candidates = voucherRepository.findAll().stream()
                .filter(v -> v.getStatus()!=null && v.getStatus()==1)
                .filter(v -> v.getQuantity()==null || v.getQuantity()>0)
                .filter(v -> isActive(now, v.getStartDate(), v.getEndDate()))
                .filter(v -> base.compareTo(money(v.getMinOrderValue())) >= 0)
                .filter(v -> v.getCustomer()==null || v.getCustomer().getId().equals(customer.getId()))
                .filter(v -> !voucherHistoryRepository.existsByVoucherAndCustomerAndInvoiceNot(v, customer, invoice))
                .collect(Collectors.toList());

        Voucher best = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;
        for (Voucher v : candidates) {
            BigDecimal d = calculateVoucherDiscountForAmount(base, v);
            if (d.compareTo(bestDiscount) > 0) {
                best = v; bestDiscount = d;
            }
        }

        invoice.setVoucher(best);
        updateInvoiceTotal(invoice);

        if (best != null) {
            VoucherHistory h = new VoucherHistory();
            h.setVoucher(best);
            h.setInvoice(invoice);
            h.setCustomer(customer);
            h.setUsedAt(now);
            h.setDiscountValueApplied(bestDiscount);
            h.setStatus(0);
            voucherHistoryRepository.save(h);
        }
        return invoiceRepository.save(invoice);
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

        // ✅ Kiểm tra blacklist
        if (Boolean.TRUE.equals(customer.getIsBlacklisted())) {
            LocalDateTime expiry = customer.getBlacklistExpiryDate();
            if (expiry == null || expiry.isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Khách hàng đang bị cấm mua hàng. Lý do: " + customer.getBlacklistReason());
            }
            // ✅ Gỡ blacklist nếu đã hết hạn
            customer.setIsBlacklisted(false);
            customer.setBlacklistReason(null);
            customer.setBlacklistExpiryDate(null);
            customerRepository.save(customer);
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

        // 6. Xử lý sau thanh toán
        processInvoicePayment(savedInvoice.getId());

        // ✅ Tự động chặn nếu khách hủy quá nhiều đơn (giống ship code)
        autoBlacklistIfTooManyCancellations(customer);

        // 7. Trả kết quả
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
                    customer.setAddressList(new ArrayList<>());
                    customer = customerRepository.save(customer);
                }
            } else {
                throw new RuntimeException("Thiếu thông tin khách hàng (cần có customerId hoặc phone)");
            }

            // Kiểm tra blacklist
            if (Boolean.TRUE.equals(customer.getIsBlacklisted())) {
                LocalDateTime expiry = customer.getBlacklistExpiryDate();
                if (expiry == null || expiry.isAfter(LocalDateTime.now())) {
                    throw new RuntimeException("Khách hàng đang bị cấm mua hàng. Lý do: " + customer.getBlacklistReason());
                }

                customer.setIsBlacklisted(false);
                customer.setBlacklistReason(null);
                customer.setBlacklistExpiryDate(null);
                customerRepository.save(customer);
            }

            // 2. Lấy địa chỉ giao hàng
            AddressRequest addr = request.getCustomerInfo().getAddress();
            String addressNew = addr.getHouseName() + " - " + addr.getProvinceName() + " - " + addr.getDistrictName() + " - Việt Nam";

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
            invoice.setDeliveryAddress(addressNew);

            // Gán nhân viên
            if (request.getEmployeeId() != null) {
                employeeRepository.findById(request.getEmployeeId()).ifPresent(invoice::setEmployee);
            }

            // Gán voucher
            if (request.getVoucherId() != null) {
                voucherRepository.findById(request.getVoucherId()).ifPresent(invoice::setVoucher);
            }

            // 4. Xử lý sản phẩm
            BigDecimal total = BigDecimal.ZERO;
            List<InvoiceDetail> details = new ArrayList<>();

            for (CartItemRequest item : request.getItems()) {
                ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết ID: " + item.getProductDetailId()));

                if (item.getQuantity() > productDetail.getQuantity()) {
                    throw new RuntimeException("Số lượng vượt quá tồn kho chi tiết sản phẩm: " + item.getProductDetailId());
                }
                productDetail.setQuantity(productDetail.getQuantity() - item.getQuantity());

                Product product = productDetail.getProduct();
                int stock = Optional.ofNullable(product.getQuantity()).orElse(0);
                if (item.getQuantity() > stock) {
                    throw new RuntimeException("Tồn kho tổng không đủ cho sản phẩm: " + product.getId());
                }
                product.setQuantity(stock - item.getQuantity());

                InvoiceDetail detail = new InvoiceDetail();
                detail.setInvoice(invoice);
                detail.setProductDetail(productDetail);
                detail.setQuantity(item.getQuantity());
                detail.setCreatedDate(LocalDateTime.now());
                detail.setStatus(1);
                detail.setInvoiceCodeDetail("INV-DTL-" + UUID.randomUUID().toString().substring(0, 8));

                BigDecimal sellPrice = Optional.ofNullable(item.getSellPrice()).orElse(productDetail.getSellPrice());
                BigDecimal discountedPrice = Optional.ofNullable(item.getDiscountedPrice()).orElse(sellPrice);
                detail.setSellPrice(sellPrice);
                detail.setDiscountedPrice(discountedPrice);
                detail.setDiscountPercentage(Optional.ofNullable(item.getDiscountPercentage()).orElse(0));

                // ✅ Gán chiến dịch giảm giá theo từng sản phẩm nếu có
                if (item.getDiscountCampaignId() != null) {
                    DiscountCampaign productCampaign = discountCampaignRepository
                            .findById(item.getDiscountCampaignId())
                            .orElse(null);

                    if (productCampaign != null) {
                        // Kiểm tra xem sản phẩm có thuộc chiến dịch không
                        boolean isInCampaign = discountCampaignProductRepository
                                .existsByCampaign_IdAndProduct_Id(productCampaign.getId(), product.getId());

                        if (isInCampaign) {
                            detail.setDiscountCampaign(productCampaign);
                        }
                    }
                }

                BigDecimal itemTotal = discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
                details.add(detail);
            }

            invoice.setTotalAmount(total);
            invoice.setFinalAmount(total.subtract(invoice.getDiscountAmount()).add(invoice.getShippingFee()));
            invoice.setInvoiceDetails(details);

            // 5. Lưu hóa đơn
            Invoice savedInvoice = invoiceRepository.save(invoice);

            // 6. Giao dịch Ship COD
            InvoiceTransaction transaction = new InvoiceTransaction();
            transaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
            transaction.setInvoice(savedInvoice);
            transaction.setAmount(savedInvoice.getFinalAmount());
            transaction.setPaymentStatus(1);
            transaction.setPaymentMethod("Thanh toán khi nhận hàng");
            transaction.setTransactionType("Thanh toán sau");
            transaction.setNote(null);
            transaction.setPaymentTime(new Date());
            invoiceTransactionRepository.save(transaction);

            // 7. Hậu thanh toán
            processInvoicePayment(savedInvoice.getId());

            // 8. Tự động kiểm tra blacklist nếu hủy nhiều đơn
            autoBlacklistIfTooManyCancellations(customer);

            // 9. Trả về kết quả
            return invoiceMapper.toInvoiceDisplayResponse(savedInvoice, savedInvoice.getInvoiceDetails());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo đơn hàng: " + e.getMessage());
        }
    }

    @Override
    public void autoBlacklistIfTooManyCancellations(Customer customer) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime lastChecked = Optional.ofNullable(customer.getLastBlacklistChecked())
                .orElse(now.minusDays(30));

        int newCancelledOrders = invoiceRepository.countByCustomerAndStatusDetailAndUpdatedDateAfter(
                customer,
                TrangThaiChiTiet.HUY_DON,
                lastChecked
        );

        if (newCancelledOrders <= 0) {
            customer.setLastBlacklistChecked(now);
            customerRepository.save(customer);
            return;
        }

        int previousCancelCount = Optional.ofNullable(customer.getLastBlacklistCancelCount()).orElse(0);
        int totalCancelled = previousCancelCount + newCancelledOrders;

        int currentScore = Optional.ofNullable(customer.getTrustScore()).orElse(100);
        int deducted = newCancelledOrders * 10;
        int newTrustScore = Math.max(0, currentScore - deducted);
        customer.setTrustScore(newTrustScore);

        boolean warned = false;
        boolean blacklisted = false;

        // ⚠️ Cảnh báo nếu hủy 3 hoặc 4 đơn
        if ((totalCancelled == 3 || totalCancelled == 4) && previousCancelCount < totalCancelled) {
            String warningMsg = "⚠️ Cảnh báo: Đã hủy " + totalCancelled + " đơn hàng. Nếu hủy đến 5 đơn sẽ bị cấm mua hàng 3 ngày.";

            // Lưu cảnh báo vào lịch sử
            CustomerBlacklistHistory warning = new CustomerBlacklistHistory();
            warning.setCustomer(customer);
            warning.setReason(warningMsg);
            warning.setStartTime(now);
            warning.setEndTime(null); // chỉ là cảnh báo, không có thời hạn
            customerBlacklistHistoryRepository.save(warning);

            // Ghi cảnh báo vào bảng khách hàng
            customer.setBlacklistReason(warningMsg);
            warned = true;
        }

        // ⛔ Cấm nếu hủy từ 5 đơn trở lên
        if (totalCancelled >= 5 && previousCancelCount < 5) {
            customer.setIsBlacklisted(true);
            customer.setBlacklistReason("Đã hủy ≥ 5 đơn hàng");
            customer.setBlacklistExpiryDate(now.plusDays(3));

            // Lưu lịch sử blacklist
            CustomerBlacklistHistory blacklist = new CustomerBlacklistHistory();
            blacklist.setCustomer(customer);
            blacklist.setReason(customer.getBlacklistReason());
            blacklist.setStartTime(now);
            blacklist.setEndTime(customer.getBlacklistExpiryDate());
            customerBlacklistHistoryRepository.save(blacklist);

            blacklisted = true;
        }

        // Cập nhật thông tin sau cùng
        customer.setLastBlacklistChecked(now);
        customer.setLastBlacklistCancelCount(totalCancelled);
        customerRepository.save(customer);
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

    // ===== Helpers tiền tệ & validate =====
    private static final int MONEY_SCALE = 0; // VND
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    private BigDecimal money(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(MONEY_SCALE, RM);
    }
    private int clampPercent(Integer p) {
        if (p == null) return 0;
        return Math.max(0, Math.min(100, p));
    }
    private boolean isActive(LocalDateTime now, LocalDateTime start, LocalDateTime end) {
        return (start == null || !now.isBefore(start)) && (end == null || !now.isAfter(end));
    }
    private String safeInvoiceCode() { return "INV-" + System.currentTimeMillis(); }
    private String currentUsername() {
        try { return SecurityContextHolder.getContext().getAuthentication().getName(); }
        catch (Exception e) { return "SYSTEM"; }
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

