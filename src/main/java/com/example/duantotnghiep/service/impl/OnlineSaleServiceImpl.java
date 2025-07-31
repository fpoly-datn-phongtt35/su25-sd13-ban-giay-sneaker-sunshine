package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.OrderRequest;
import com.example.duantotnghiep.dto.response.InvoiceDetailOnline;
import com.example.duantotnghiep.dto.response.InvoiceOnlineResponse;
import com.example.duantotnghiep.dto.response.InvoiceTransactionResponse;
import com.example.duantotnghiep.dto.response.OrderStatusHistoryResponse;
import com.example.duantotnghiep.dto.response.StatusCountResponse;
import com.example.duantotnghiep.model.*;
import com.example.duantotnghiep.repository.*;
import com.example.duantotnghiep.service.OnlineSaleService;
import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private final CustomerRepository customerRepository;
    private final CustomerBlacklistHistoryRepository customerBlacklistHistoryRepository;

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
        invoiceRepository.save(invoice);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setInvoice(invoice);
        history.setEmployee(employee);
        history.setOldStatus(currentStatus.getMa());
        history.setNewStatus(TrangThaiChiTiet.DA_XU_LY.getMa());
        history.setChangedAt(new Date());

        historyRepository.save(history);
    }

    @Override
    public void huyDonClient(Long invoiceId,String nextKey) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

//        Customer c = user.getCustomer();
//        if (c == null) {
//            throw new RuntimeException("Người dùng không phải là nhân viên.");
//        }

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
        invoiceRepository.save(invoice);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setInvoice(invoice);
        history.setOldStatus(currentStatus.getMa());
        history.setNewStatus(TrangThaiChiTiet.DA_XU_LY.getMa());
        history.setChangedAt(new Date());

        historyRepository.save(history);
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
    public void huyDonVaHoanTien(Long invoiceId, String nextKey, String note, String paymentMethod, Boolean isPaid) {
        // 1. Tìm hóa đơn đã thanh toán
        Invoice invoice = invoiceRepository.findPaidInvoiceById(invoiceId, isPaid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 2. Gọi client để hủy đơn
        huyDonClient(invoiceId, nextKey);

        // 3. Hoàn lại số lượng sản phẩm
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoiceId(invoiceId);
        for (InvoiceDetail detail : invoiceDetails) {
            ProductDetail productDetail = detail.getProductDetail();
            productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
            productDetailRepository.save(productDetail);

            Product product = productDetail.getProduct();
            product.setQuantity(product.getQuantity() + detail.getQuantity());
            productRepository.save(product);
        }

        // 4. Ghi nhận giao dịch hoàn tiền
        InvoiceTransaction invoiceTransaction = new InvoiceTransaction();
        invoiceTransaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
        invoiceTransaction.setInvoice(invoice);
        invoiceTransaction.setAmount(invoice.getFinalAmount());
        invoiceTransaction.setPaymentStatus(2); // Hoàn tiền
        invoiceTransaction.setPaymentMethod(paymentMethod);
        invoiceTransaction.setTransactionType("Hoàn tiền");
        invoiceTransaction.setNote(note);

        // Chuyển LocalDateTime sang Date
        invoiceTransaction.setPaymentTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        invoiceTransactionRepository.save(invoiceTransaction);

        // 5. Cập nhật trạng thái đơn
        invoice.setStatus(TrangThaiTong.DA_HUY);
        invoice.setStatusDetail(TrangThaiChiTiet.HUY_DON);
        invoice.setUpdatedDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        invoiceRepository.save(invoice);

        // 6. Kiểm tra blacklist
        Customer customer = invoice.getCustomer();
        if (customer != null && !Boolean.TRUE.equals(customer.getIsBlacklisted())) {
            autoBlacklistIfTooManyCancellations(customer);
        }
    }

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

    @Override
    public void giaoHangThatBaiVaHoanTien(Long invoiceId, String nextKey, String note, String paymentMenthod,Boolean isPaid) {
        huyDonVaHoanTien(invoiceId,nextKey,note,paymentMenthod,isPaid);
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        System.out.println("user: "+user.getCustomer().getCustomerName());

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new RuntimeException("Người dùng không phải là nhân viên.");
        }

        InvoiceOnlineResponse response = invoiceRepository2.getOrder2(invoiceId,customer.getId());
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
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(Long invoiceId) {
        List<OrderStatusHistoryResponse> responses = historyRepository.getOrderStatusHistoriesByInvoice(invoiceId);
        System.out.println("response: "+responses);
        return responses;
    }

    @Override
    public void updateAddressShipping(Long invoiceId, String address) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Ko thấy hóa đơn với id: " + invoiceId));
        invoice.setDeliveryAddress(address);
        invoiceRepository.save(invoice);
    }

}