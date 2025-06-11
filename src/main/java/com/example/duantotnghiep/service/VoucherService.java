package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.request.VoucherRequest;
import com.example.duantotnghiep.dto.request.VoucherSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.dto.response.VoucherResponse;
import org.springframework.data.domain.Pageable;

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

}
