package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.VoucherStatusDTO;
import com.example.duantotnghiep.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
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

    @Query("SELECT v FROM Voucher v WHERE v.customer.id = :customerId AND v.voucherCode = :voucherCode")
    Optional<Voucher> findByCustomerIdAndVoucherCode(@Param("customerId") Long customerId,
                                                     @Param("voucherCode") String voucherCode);

    List<Voucher> findByCustomer_Id(Long customerId);

    boolean existsByCustomerIdAndDiscountAmount(Long customerId, int discountAmount);

    List<Voucher> findByCustomerId(Long customerId);

    @Query("""
      select v
      from Voucher v
      where v.status = 1
        and (v.quantity is null or v.quantity > 0)
        and (:now is null or (coalesce(v.startDate, :now) <= :now and coalesce(v.endDate, :now) >= :now))
        and (v.minOrderValue is null or v.minOrderValue <= :base)
        and (v.customer is null or v.customer.id = :customerId)
    """)
    List<Voucher> findEligibleVouchers(
            @Param("customerId") Long customerId,
            @Param("base") BigDecimal base,
            @Param("now") LocalDateTime now
    );


    @Query("""
      select v
      from Voucher v
      where v.status = 1
        and (v.quantity is null or v.quantity > 0)
        and v.customer.id = :customerId
        and (:now is null or (coalesce(v.startDate, :now) <= :now and coalesce(v.endDate, :now) >= :now))
    """)
    List<Voucher> findActiveOwnedByCustomer(@Param("customerId") Long customerId,
                                            @Param("now") LocalDateTime now);

    @Query("""
        select v from Voucher v where v.id in :ids and v.status = 1
                """)
    List<Voucher> getVoucherByIds(@Param("ids") List<Long> ids);

}
