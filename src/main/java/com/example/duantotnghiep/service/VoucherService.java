package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.VoucherRequest;
import com.example.duantotnghiep.dto.request.VoucherSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.VoucherResponse;
import com.example.duantotnghiep.model.Voucher;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<VoucherResponse> getValidVouchers();

    List<VoucherResponse> getVouchersByCustomerInInvoice(Long invoiceId);

    VoucherResponse themMoi(VoucherRequest voucherRequest);
    VoucherResponse capNhat(Long id,VoucherRequest voucherRequest);
    Optional<VoucherResponse> getOne(Long id);
    void deteleVoucherById(Long id);

    PaginationDTO<VoucherResponse> phanTrangHienThi(VoucherSearchRequest request, Pageable pageable);

    Voucher validateVoucher(Long customerId, String voucherCode, BigDecimal orderTotal);

    Voucher findBestVoucherForCustomer(Long customerId, BigDecimal orderTotal);

    List<VoucherResponse> getVouchersByCustomerId(String customerId);
}
