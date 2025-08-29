package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherCode(String voucherCode);

    @Query("SELECT v FROM Voucher v WHERE v.status = 1 AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);

    @Query("""
    SELECT v
    FROM Voucher v
    WHERE v.status = 1
      AND (v.quantity IS NULL OR v.quantity > 0)
      AND :now BETWEEN v.startDate AND v.endDate
      AND (:orderType IS NULL OR v.orderType = :orderType)
      AND (v.customer IS NULL OR v.customer.id = :customerId)
      AND (
            (:useProducts = true AND (
                 v.product IS NULL
                 OR :hasProductIds = false
                 OR v.product.id IN :productIds
            ))
         OR (
            :useProducts = false AND :useCategories = true AND (
                 v.category IS NULL
                 OR :hasCategoryIds = false
                 OR v.category.id IN :categoryIds
            )
         )
         OR (
            :useProducts = false AND :useCategories = false
            AND v.product IS NULL AND v.category IS NULL
         )
      )
    ORDER BY v.createdDate DESC
    """)
    List<Voucher> findValidVouchers(
            @Param("now") LocalDateTime now,
            @Param("customerId") Long customerId,
            @Param("orderType") Integer orderType,   // 0: quầy, 1: online, null: không lọc
            @Param("useProducts") boolean useProducts,
            @Param("useCategories") boolean useCategories,
            @Param("hasProductIds") boolean hasProductIds,
            @Param("hasCategoryIds") boolean hasCategoryIds,
            @Param("productIds") Set<Long> productIds,
            @Param("categoryIds") Set<Long> categoryIds
    );



    @Query("""
            SELECT v
            FROM Voucher v
            WHERE v.status = 1
              AND (v.quantity IS NULL OR v.quantity > 0)
              AND (v.startDate IS NULL OR :now >= v.startDate)
              AND (v.endDate   IS NULL OR :now <= v.endDate)
              AND (:orderType IS NULL OR v.orderType = :orderType)
              AND v.product IS NOT NULL
              AND v.product.id IN :productIds
            ORDER BY v.createdDate DESC
            """)
    List<Voucher> findValidVouchersStrictByProducts(
            @Param("now") LocalDateTime now,
            @Param("orderType") Integer orderType,
            @Param("productIds") Collection<Long> productIds
    );


    List<Voucher> findByStatus(Integer status);

    boolean existsByCustomerIdAndVoucherNameAndDiscountAmount(Long customerId, String voucherName, Integer discountAmount);

    @Query("""
            SELECT v
            FROM Voucher v
            WHERE v.voucherCode = :voucherCode
              AND (v.customer IS NULL OR v.customer.id = :customerId)
            """)
    Optional<Voucher> findByCustomerIdOrGlobalVoucherCode(@Param("customerId") Long customerId,
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
