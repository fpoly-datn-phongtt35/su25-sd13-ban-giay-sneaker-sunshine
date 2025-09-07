package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.OrderRequest;
import com.example.duantotnghiep.dto.request.UpdateAddress;
import com.example.duantotnghiep.dto.response.*;
import com.example.duantotnghiep.model.*;
import com.example.duantotnghiep.repository.*;
import com.example.duantotnghiep.service.CustomerService;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.OnlineSaleService;
import com.example.duantotnghiep.service.VoucherEmailService;
import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineSaleServiceImpl implements OnlineSaleService {
    private final InvoiceRepository invoiceRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final InvoiceTransactionRepository invoiceTransactionRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final InvoiceRepository2 invoiceRepository2;
    private final ProductDetailRepository  productDetailRepository;
    private final ProductRepository productRepository;
    private final InvoiceService invoiceService;
    private final VoucherRepository voucherRepository;
    private final VoucherEmailService voucherEmailService;

    @Override
    public void chuyenTrangThai(Long invoiceId,String nextKey) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        invoice.setEmployee(employee);
        if(employee.getId() == null ){
            throw new RuntimeException("ko lấy đc nhân viên.");
        }

        Boolean exists = historyRepository.isProcessedByAnotherEmployee(invoiceId, employee.getId());
        if(exists) {
            throw new RuntimeException("Đơn hàng đang được xử lý bởi nhân viên khác.");
        }else{
            TrangThaiChiTiet currentStatus = invoice.getStatusDetail();

            TrangThaiChiTiet nextStatus;
            try {
                nextStatus = TrangThaiChiTiet.valueOf(nextKey);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái tiếp theo không hợp lệ: " + nextKey);
            }

            if (currentStatus == nextStatus) {
                throw new RuntimeException("Trạng thái mới trùng với trạng thái hiện tại.");
            }

            invoice.setStatusDetail(nextStatus);
            if (nextStatus == TrangThaiChiTiet.HUY_DON) {
                invoice.setStatus(TrangThaiTong.DA_HUY); // hoặc HUY, DA_HUY,... tùy định nghĩa
            }

            if (nextStatus == TrangThaiChiTiet.GIAO_THANH_CONG) {
                invoice.setDeliveredAt(new Date());
                invoice.setStatus(TrangThaiTong.THANH_CONG);
            }


            if (nextStatus == TrangThaiChiTiet.CHO_GIAO_HANG ||  nextStatus == TrangThaiChiTiet.DANG_GIAO_HANG || nextStatus == TrangThaiChiTiet.DA_XU_LY || nextStatus == TrangThaiChiTiet.CHO_XU_LY) {
                invoice.setStatus(TrangThaiTong.DANG_XU_LY);
            }

            Invoice s = invoiceRepository.save(invoice);
            handleAutoPromoVoucher(s, username, LocalDateTime.now());
            OrderStatusHistory history = new OrderStatusHistory();
            history.setInvoice(invoice);
            history.setEmployee(employee);
            history.setOldStatus(currentStatus.getMa());
            history.setNewStatus(nextStatus.getMa());
            history.setChangedAt(new Date());
            historyRepository.save(history);
        }
    }

    private void handleAutoPromoVoucher(Invoice invoice, String username, LocalDateTime now) {
        // Điều kiện tiên quyết
        if (invoice == null || invoice.getCustomer() == null || invoice.getTotalAmount() == null) return;

        // ✅ Chỉ tặng khi trạng thái tổng = THANH_CONG
        if (invoice.getStatus() != TrangThaiTong.THANH_CONG) return;

        BigDecimal totalAmount = invoice.getTotalAmount();
        Long customerId = invoice.getCustomer().getId();

        // Lấy các voucher đang hoạt động + còn hạn + có minOrderToReceive
        List<Voucher> activePromos = voucherRepository.findByStatus(1).stream()
                .filter(v -> {
                    LocalDateTime start = v.getStartDate();
                    LocalDateTime end   = v.getEndDate();
                    boolean timeOk = (start == null || !now.isBefore(start)) &&
                            (end   == null || !now.isAfter(end));
                    return timeOk && v.getMinOrderToReceive() != null;
                })
                .collect(Collectors.toList());

        // Chọn ngưỡng cao nhất thỏa điều kiện
        Voucher matchedPromo = activePromos.stream()
                .filter(v -> totalAmount.compareTo(v.getMinOrderToReceive()) >= 0)
                .sorted((v1, v2) -> v2.getMinOrderToReceive().compareTo(v1.getMinOrderToReceive()))
                .findFirst()
                .orElse(null);

        if (matchedPromo == null) return;

        int discountAmount = matchedPromo.getDiscountAmount() != null ? matchedPromo.getDiscountAmount() : 0;

        // Tránh tặng trùng
        boolean alreadyGiven = voucherRepository.existsByCustomerIdAndVoucherNameAndDiscountAmount(
                customerId, matchedPromo.getVoucherName(), discountAmount
        );
        if (alreadyGiven) return;

        // Tạo voucher tặng
        Voucher newVoucher = new Voucher();
        newVoucher.setCustomer(invoice.getCustomer());
        newVoucher.setVoucherCode("VOUCHER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newVoucher.setVoucherName(matchedPromo.getVoucherName());
        newVoucher.setDiscountAmount(discountAmount);

        // Điều kiện áp dụng (copy, có thể chỉnh về ZERO nếu muốn không yêu cầu tối thiểu)
        newVoucher.setDiscountPercentage(
                matchedPromo.getDiscountPercentage() != null ? matchedPromo.getDiscountPercentage() : BigDecimal.ZERO
        );
        newVoucher.setMinOrderValue(
                matchedPromo.getMinOrderValue() != null ? matchedPromo.getMinOrderValue() : BigDecimal.ZERO
        );
        newVoucher.setMaxDiscountValue(
                matchedPromo.getMaxDiscountValue() != null ? matchedPromo.getMaxDiscountValue() : BigDecimal.ZERO
        );

        // Hạn dùng 30 ngày
        newVoucher.setStartDate(now);
        newVoucher.setEndDate(now.plusDays(30));
        newVoucher.setStatus(1);
        newVoucher.setCreatedDate(now);
        newVoucher.setCreatedBy(username != null ? username : "SYSTEM");
        newVoucher.setQuantity(1);
        newVoucher.setVoucherType(0); // loại tặng
        newVoucher.setOrderType(1);   // tùy quy ước (online?)

        voucherRepository.save(newVoucher);

        // Gửi email
        String email = invoice.getCustomer().getEmail();
        if (email != null && !email.isEmpty()) {
            voucherEmailService.sendVoucherNotificationEmail(
                    email,
                    invoice.getCustomer().getCustomerName(),
                    totalAmount,
                    BigDecimal.valueOf(discountAmount),
                    newVoucher.getDiscountPercentage(),
                    newVoucher.getVoucherCode(),
                    newVoucher.getEndDate().toLocalDate()
            );
        }
    }


    @Override
    @Transactional
    public void huyDonEmployee(Long invoiceId, String nextKey) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        Employee c = user.getEmployee();
        if (c == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        TrangThaiChiTiet currentStatus = invoice.getStatusDetail();

        TrangThaiChiTiet nextStatus;
        try {
            nextStatus = TrangThaiChiTiet.valueOf(nextKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái tiếp theo không hợp lệ: " + nextKey);
        }

        if (currentStatus == nextStatus) {
            throw new RuntimeException("Trạng thái mới trùng với trạng thái hiện tại.");
        }

        invoice.setEmployee(c);
        invoice.setStatusDetail(nextStatus);

        if (nextStatus == TrangThaiChiTiet.HUY_DON) {
            invoice.setStatus(TrangThaiTong.DA_HUY);
        }

        invoiceRepository.save(invoice);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setInvoice(invoice);
        history.setEmployee(c);
        history.setOldStatus(currentStatus.getMa());
        history.setNewStatus(nextStatus.getMa());
        history.setChangedAt(new Date());

        historyRepository.save(history);
    }

    @Override
    @Transactional
    public void huyDonClient(Long invoiceId, String nextKey) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        Customer c = user.getCustomer();
        if (c == null) {
            throw new RuntimeException("Người dùng không phải là khách hàng.");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        TrangThaiChiTiet currentStatus = invoice.getStatusDetail();

        TrangThaiChiTiet nextStatus;
        try {
            nextStatus = TrangThaiChiTiet.valueOf(nextKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái tiếp theo không hợp lệ: " + nextKey);
        }

        if (currentStatus == nextStatus) {
            throw new RuntimeException("Trạng thái mới trùng với trạng thái hiện tại.");
        }

        invoice.setStatusDetail(nextStatus);

        if (nextStatus == TrangThaiChiTiet.HUY_DON) {
            invoice.setStatus(TrangThaiTong.DA_HUY); // hoặc HUY, DA_HUY,... tùy định nghĩa
        }
        invoice.setUpdatedDate(new Date()); // THÊM DÒNG NÀY
        invoiceRepository.save(invoice);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setCustomerId(c.getId());
        history.setInvoice(invoice);
        history.setOldStatus(currentStatus.getMa());
        history.setNewStatus(nextStatus.getMa());
        history.setChangedAt(new Date());

        historyRepository.save(history);

        // Gọi hàm kiểm tra và cấm nếu cần
        invoiceService.autoBlacklistIfTooManyCancellations(c);
    }

    @Override
    public InvoiceOnlineResponse getOrder(Long invoiceId) {
        InvoiceOnlineResponse response = invoiceRepository2.getOrder(invoiceId);
        List<InvoiceDetailOnline> invoiceDetailOnline = invoiceDetailRepository.findByInvoiceDetailOnline(invoiceId);
        List<InvoiceTransactionResponse> invoiceTransactionResponses = invoiceTransactionRepository.findInvoiceTransactionByIdInvoice(invoiceId);

        response.setInvoiceDetailResponses(invoiceDetailOnline);
        response.setInvoiceTransactionResponses(invoiceTransactionResponses);

        return response;
    }

    @Override
    @Transactional
    public void huyDonVaHoanTienClient(Long invoiceId, String nextKey, String note, Integer request, Boolean isPaid) {
        Invoice invoice = invoiceRepository.findPaidInvoiceById(invoiceId, isPaid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        huyDonClient(invoiceId, nextKey);

        List<InvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoiceId(invoiceId);
        for (InvoiceDetail detail : invoiceDetails) {
            ProductDetail productDetail = detail.getProductDetail();

            int oldDetailStock = productDetail.getQuantity();
            productDetail.setQuantity(oldDetailStock + detail.getQuantity());
            productDetailRepository.save(productDetail);

            Product product = productDetail.getProduct();
            int oldProductStock = product.getQuantity();
            product.setQuantity(oldProductStock + detail.getQuantity());
            productRepository.save(product);
        }

        //  Ghi nhận giao dịch hoàn tiền
        InvoiceTransaction invoiceTransaction = new InvoiceTransaction();
        invoiceTransaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
        invoiceTransaction.setInvoice(invoice);
        invoiceTransaction.setAmount(invoice.getFinalAmount());
        invoiceTransaction.setPaymentStatus(2);
        invoiceTransaction.setTransactionType("Yêu cầu hoàn tiền");
        invoiceTransaction.setNote(note);
        invoiceTransaction.setPaymentTime(new Date());
        invoiceTransactionRepository.save(invoiceTransaction);

        invoice.setRequest(request);
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void huyDonVaHoanTienEmployee(Long invoiceId, String nextKey, String note, String paymentMenthod, Boolean isPaid,String tradeCode,String bankName) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }
        Invoice invoice = invoiceRepository.findPaidInvoiceById(invoiceId, isPaid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        invoice.setEmployee(employee);
        huyDonEmployee(invoiceId, nextKey);

        List<InvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoiceId(invoiceId);
        for (InvoiceDetail detail : invoiceDetails) {
            ProductDetail productDetail = detail.getProductDetail();

            int oldDetailStock = productDetail.getQuantity();
            productDetail.setQuantity(oldDetailStock + detail.getQuantity());
            productDetailRepository.save(productDetail);

            Product product = productDetail.getProduct();
            int oldProductStock = product.getQuantity();
            product.setQuantity(oldProductStock + detail.getQuantity());
            productRepository.save(product);
        }

        InvoiceTransaction invoiceTransaction = new InvoiceTransaction();
        invoiceTransaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
        invoiceTransaction.setInvoice(invoice);
        invoiceTransaction.setAmount(invoice.getFinalAmount());
        invoiceTransaction.setPaymentStatus(2);
        invoiceTransaction.setTradeCode(tradeCode);
        invoiceTransaction.setBankName(bankName);
        invoiceTransaction.setPaymentMethod(paymentMenthod);
        invoiceTransaction.setTransactionType("Hoàn tiền");
        invoiceTransaction.setNote(note);
        invoiceTransaction.setPaymentTime(new Date());
        invoiceTransactionRepository.save(invoiceTransaction);

        invoiceRepository.save(invoice);
    }

    @Override
    public void giaoHangThatBaiVaHoanTien(Long invoiceId, String nextKey, String note, String paymentMenthod,Boolean isPaid,String tradeCode,String bankName) {
        huyDonVaHoanTienEmployee(invoiceId,nextKey,note,paymentMenthod,isPaid,tradeCode,bankName);
    }

    @Override
    public List<StatusCountResponse> getCountByStatusDetail() {
        List<Object[]> result = invoiceRepository.countByStatusDetail();
        return result.stream()
                .map(obj -> new StatusCountResponse(((TrangThaiChiTiet) obj[0]).name(), (Long) obj[1]))
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceOnlineResponse getOrderByCustomer(Long invoiceId) {

        InvoiceOnlineResponse response = invoiceRepository2.getOrder(invoiceId);
        if(response == null){
            return null;
        }
        List<InvoiceDetailOnline> invoiceDetailOnline = invoiceDetailRepository.findByInvoiceDetailOnline(invoiceId);
        List<InvoiceTransactionResponse> invoiceTransactionResponses = invoiceTransactionRepository.findInvoiceTransactionByIdInvoice(invoiceId);

        response.setInvoiceDetailResponses(invoiceDetailOnline);
        response.setInvoiceTransactionResponses(invoiceTransactionResponses);
        return response;
    }

    @Override
    public List<InvoiceOnlineResponse> getOrderByCustomer2(Integer status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("user: "+username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        System.out.println("user: "+user.getCustomer().getCustomerName());

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        List<InvoiceOnlineResponse> response = invoiceRepository2.getOrder3(customer.getId(),status);
        if(response == null){
            return null;
        }
        return response;
    }

    @Override
    public List<InvoiceOnlineResponse> getOrderByCustomer3(Integer status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("user: "+username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        System.out.println("user: "+user.getCustomer().getCustomerName());

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        List<InvoiceOnlineResponse> response = invoiceRepository2.getOrder4(customer.getId(),status);
        if(response == null){
            return null;
        }
        return response;
    }

    @Override
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(Long invoiceId) {
        List<OrderStatusHistoryResponse> responses = historyRepository.getOrderStatusHistoriesByInvoice(invoiceId);
        System.out.println("response: "+responses);
        return responses;
    }

    @Override
    public void updateAddressShipping(UpdateAddress address) {
        Invoice invoice = invoiceRepository.findById(address.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Ko thấy hóa đơn với id: " + address.getInvoiceId()));
        invoice.setDeliveryAddress(address.getNewAddress());
        invoice.setShippingFee(address.getShippingFee());
        invoice.setFinalAmount(address.getFinalAmount());
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void updateSDT(Long invoiceId, String phone) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("invoiceId không được null.");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Ko thấy hóa đơn với id: " + invoiceId));

        invoice.setPhone(phone);
        invoiceRepository.save(invoice);
    }
    @Override
    public List<StatusCountDTO> getCountByStatus() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("user: "+username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        System.out.println("user: "+user.getCustomer().getCustomerName());

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }
        List<Object[]> results = invoiceRepository.countInvoicesByStatusNative(customer.getId());
        List<StatusCountDTO> list = new ArrayList<>();

        for (Object[] row : results) {
            String status = (String) row[0];
            Integer count = ((Number) row[1]).intValue();
            list.add(new StatusCountDTO(status, count));
        }
        return list;
    }

    @Override
    public List<StatusCountDTO> getCountByStatusV2() {
        List<Object[]> results = invoiceRepository.countInvoicesByStatusNativeV2();
        List<StatusCountDTO> list = new ArrayList<>();

        for (Object[] row : results) {
            String status = (String) row[0];
            Integer count = ((Number) row[1]).intValue();
            list.add(new StatusCountDTO(status, count));
        }
        return list;
    }

    @Override
    public BigDecimal getRevenue(String type) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        Date fromDate;
        Date toDate;

        switch (type.toLowerCase()) {
            case "day":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                fromDate = calendar.getTime();

                calendar.add(Calendar.DATE, 1);
                toDate = calendar.getTime();
                break;

            case "week":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                fromDate = calendar.getTime();

                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                toDate = calendar.getTime();
                break;

            case "quarter":
                int month = calendar.get(Calendar.MONTH);
                int quarterStartMonth = (month / 3) * 3;

                calendar.set(Calendar.MONTH, quarterStartMonth);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                fromDate = calendar.getTime();

                calendar.add(Calendar.MONTH, 3);
                toDate = calendar.getTime();
                break;

            case "year":
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                fromDate = calendar.getTime();

                calendar.add(Calendar.YEAR, 1);
                toDate = calendar.getTime();
                break;

            default:
                throw new IllegalArgumentException("Invalid type. Use: day|week|quarter|year");
        }
        BigDecimal result = invoiceRepository.sumRevenueBetween(fromDate, toDate);

        return  result != null ? result : BigDecimal.ZERO;
    }

}