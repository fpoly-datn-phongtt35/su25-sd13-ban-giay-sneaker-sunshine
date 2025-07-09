package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.Voucher;
import com.example.duantotnghiep.model.VoucherHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoucherHistoryRepository extends JpaRepository<VoucherHistory, Long> {
    @Query("SELECT vh FROM VoucherHistory vh WHERE vh.customer.id = :customerId")
    List<VoucherHistory> findByCustomerId(@Param("customerId") Long customerId);

    List<VoucherHistory> findByCustomerIdAndStatus(Long customerId, Integer status);

    // Trả về nhiều lịch sử voucher nếu có
    List<VoucherHistory> findByInvoiceAndVoucherAndStatus(Invoice invoice, Voucher voucher, int status);


}