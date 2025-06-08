package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.response.VoucherResponse;
import com.example.duantotnghiep.mapper.VoucherMapper;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.Voucher;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.repository.VoucherHistoryRepository;
import com.example.duantotnghiep.repository.VoucherRepository;
import com.example.duantotnghiep.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceIpml implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;
    private final VoucherMapper voucherMapper;
    private final InvoiceRepository invoiceRepository;

    public List<VoucherResponse> getValidVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findValidVouchers(now);

        return vouchers.stream()
                .map(voucherMapper::toVoucherResponse)
                .collect(Collectors.toList());
    }

//    public List<VoucherResponse> getVoucherResponsesByCustomerId(Long customerId) {
//        LocalDateTime now = LocalDateTime.now();
//        List<Voucher> vouchers = voucherRepository.findValidVouchersByCustomerId(now, customerId);
//        return vouchers.stream()
//                .map(voucherMapper::toVoucherResponse)
//                .collect(Collectors.toList());
//    }
//
//    public VoucherResponse getVoucherByInvoiceId(Long invoiceId) {
//        Invoice invoice = invoiceRepository.findById(invoiceId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
//
//        Voucher voucher = invoice.getVoucher();
//        if (voucher == null) {
//            throw new RuntimeException("Hóa đơn không có voucher nào.");
//        }
//
//        return voucherMapper.toVoucherResponse(voucher);
//    }

    public List<VoucherResponse> getVouchersByCustomerInInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoice.getCustomer() == null) {
            throw new RuntimeException("Hóa đơn không có khách hàng.");
        }

        Long customerId = invoice.getCustomer().getId();
        LocalDateTime now = LocalDateTime.now();

        // Lấy danh sách voucher hợp lệ theo logic hiện có
        List<Voucher> vouchers = voucherRepository.findValidVouchersForCustomer(now, customerId);

        if (vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher hợp lệ cho khách hàng có ID: " + customerId);
        }

        // Lấy voucherId đã dùng của khách hàng
        Set<Long> usedVoucherIds = voucherHistoryRepository.findByCustomerId(customerId).stream()
                .map(vh -> vh.getVoucher().getId())
                .collect(Collectors.toSet());

        // Lọc voucher để loại bỏ voucher đã dùng
        List<Voucher> availableVouchers = vouchers.stream()
                .filter(v -> !usedVoucherIds.contains(v.getId()))
                .collect(Collectors.toList());

        return availableVouchers.stream()
                .map(voucherMapper::toVoucherResponse)
                .toList();
    }


}
