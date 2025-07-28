package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.OrderRequest;
import com.example.duantotnghiep.dto.response.InvoiceOnlineResponse;
import com.example.duantotnghiep.dto.response.OrderStatusHistoryResponse;
import com.example.duantotnghiep.dto.response.StatusCountResponse;

import java.util.List;

public interface OnlineSaleService {

    void chuyenTrangThai(Long invoiceId, String nextKey);
    InvoiceOnlineResponse getOrder(Long invoiceId);

    void huyDonVaHoanTien(Long invoiceId,String nextKey,String note,String paymentMenthod,Boolean isPaid);
    void giaoHangThatBaiVaHoanTien(Long invoiceId,String nextKey,String note,String paymentMenthod,Boolean isPaid);
    List<StatusCountResponse> getCountByStatusDetail();

    InvoiceOnlineResponse getOrderByCustomer(Long invoiceId);

    List<InvoiceOnlineResponse> getOrderByCustomer2(Integer status);

    List<OrderStatusHistoryResponse> getOrderStatusHistory(Long invoiceId);

    void updateAddressShipping(Long invoiceId,String address);

}