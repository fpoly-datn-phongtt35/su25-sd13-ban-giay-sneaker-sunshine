package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.OrderRequest;
import com.example.duantotnghiep.dto.response.*;
import com.example.duantotnghiep.model.*;
import com.example.duantotnghiep.repository.*;
import com.example.duantotnghiep.service.CustomerService;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.OnlineSaleService;
import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
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
            invoiceRepository.save(invoice);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setInvoice(invoice);
            history.setEmployee(employee);
            history.setOldStatus(currentStatus.getMa());
            history.setNewStatus(nextStatus.getMa());
            history.setChangedAt(new Date());

            historyRepository.save(history);
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
    public void huyDonVaHoanTienClient(Long invoiceId, String nextKey, String note, String paymentMenthod, Boolean isPaid) {
        Invoice invoice = invoiceRepository.findPaidInvoiceById(invoiceId, isPaid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        huyDonClient(invoiceId, nextKey); // ✅ Gọi hàm hủy đơn

        // Cập nhật lại kho cho từng sản phẩm
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

        // ✅ Ghi nhận giao dịch hoàn tiền
        InvoiceTransaction invoiceTransaction = new InvoiceTransaction();
        invoiceTransaction.setTransactionCode("GD-" + UUID.randomUUID().toString().substring(0, 8));
        invoiceTransaction.setInvoice(invoice);
        invoiceTransaction.setAmount(invoice.getFinalAmount());
        invoiceTransaction.setPaymentStatus(2);
        invoiceTransaction.setPaymentMethod(paymentMenthod);
        invoiceTransaction.setTransactionType("Hoàn tiền");
        invoiceTransaction.setNote(note);
        invoiceTransaction.setPaymentTime(new Date());
        invoiceTransactionRepository.save(invoiceTransaction);

        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void huyDonVaHoanTienEmployee(Long invoiceId, String nextKey, String note, String paymentMenthod, Boolean isPaid,String tradeCode) {
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
        invoiceTransaction.setPaymentMethod(paymentMenthod);
        invoiceTransaction.setTransactionType("Hoàn tiền");
        invoiceTransaction.setNote(note);
        invoiceTransaction.setPaymentTime(new Date());
        invoiceTransactionRepository.save(invoiceTransaction);

        invoiceRepository.save(invoice);
    }

    @Override
    public void giaoHangThatBaiVaHoanTien(Long invoiceId, String nextKey, String note, String paymentMenthod,Boolean isPaid,String tradeCode) {
        huyDonVaHoanTienEmployee(invoiceId,nextKey,note,paymentMenthod,isPaid,tradeCode);
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

    @Override
    public List<StatusCountDTO> getCountByStatus() {
        List<Object[]> results = invoiceRepository.countInvoicesByStatusNative();
        List<StatusCountDTO> list = new ArrayList<>();

        for (Object[] row : results) {
            String status = (String) row[0];
            Integer count = ((Number) row[1]).intValue();
            list.add(new StatusCountDTO(status, count));
        }
        return list;
    }


}