package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getValidVouchers();

    List<VoucherResponse> getVouchersByCustomerInInvoice(Long invoiceId);

//    List<VoucherResponse> getVoucherResponsesByCustomerId(Long customerId);

//    VoucherResponse getVoucherByInvoiceId(Long invoiceId);

}
