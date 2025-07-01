package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherCode(String voucherCode);

    @Query("SELECT v FROM Voucher v WHERE v.status = 1 AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);

    @Query("""
    SELECT v FROM Voucher v
    WHERE v.status = 1
      AND v.quantity > 0
      AND :now BETWEEN v.startDate AND v.endDate
      AND (:customerId IS NULL OR v.customer.id = :customerId OR v.customer IS NULL)
      AND (
          v.product.id IN :productIds
          OR v.category.id IN :categoryIds
          OR (v.product IS NULL AND v.category IS NULL)
      )
    ORDER BY v.createdDate DESC
""")
    List<Voucher> findValidVouchers(
            @Param("now") LocalDateTime now,
            @Param("customerId") Long customerId,
            @Param("productIds") Set<String> productIds,
            @Param("categoryIds") Set<String> categoryIds
    );

    List<Voucher> findByStatus(Integer status);
    boolean existsByCustomerIdAndVoucherNameAndDiscountAmount(Long customerId, String voucherName, Integer discountAmount);

}
