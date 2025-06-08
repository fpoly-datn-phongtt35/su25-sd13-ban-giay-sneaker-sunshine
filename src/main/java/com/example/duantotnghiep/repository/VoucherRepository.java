package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherCode(String voucherCode);

    @Query("SELECT v FROM Voucher v WHERE v.status = 1 AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);

//    @Query("SELECT v FROM Voucher v WHERE v.status = 1 AND :now BETWEEN v.startDate AND v.endDate AND (v.customer.id = :customerId OR v.customer IS NULL)")
//    List<Voucher> findValidVouchersByCustomerId(@Param("now") LocalDateTime now, @Param("customerId") Long customerId);
//
//    // Nếu 1 khách hàng chỉ có 1 voucher (hoặc bạn muốn lấy 1 voucher ưu tiên nhất)
//    Optional<Voucher> findFirstByCustomer_Id(Long customerId);
//
//    // Nếu khách hàng có thể có nhiều voucher
//    List<Voucher> findByCustomer_Id(Long customerId);

    @Query("""
    SELECT v FROM Voucher v
    WHERE v.status = 1
      AND :now BETWEEN v.startDate AND v.endDate
      AND (v.customer.id = :customerId OR v.customer IS NULL)
    ORDER BY v.createdDate DESC
""")
    List<Voucher> findValidVouchersForCustomer(@Param("now") LocalDateTime now, @Param("customerId") Long customerId);
}
