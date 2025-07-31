package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("""
    SELECT new com.example.duantotnghiep.dto.response.InvoiceResponse(
        i.id,
        i.invoiceCode,
        i.statusDetail,
        i.orderType,
        i.createdDate,
        i.customer.customerName,
        i.customer.phone,
        i.totalAmount
    )
    FROM Invoice i
    WHERE (:status IS NULL OR i.statusDetail = :status)
      AND (:orderType IS NULL OR i.orderType = :orderType)
      AND (:createdFrom IS NULL OR i.createdDate >= :createdFrom)
      AND (:createdTo IS NULL OR i.createdDate <= :createdTo)
      AND (:phone IS NULL OR i.customer.phone LIKE %:phone%)
      AND (:code IS NULL OR i.invoiceCode LIKE %:code%)
      order by i.createdDate asc 
""")
    Page<InvoiceResponse> searchInvoices(
            @Param("status") TrangThaiChiTiet status,
            @Param("orderType") Integer orderType,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("phone") String phone,
            @Param("code") String code,
            Pageable pageable
    );


    List<Invoice> findByStatus(int status);

    @Query("SELECT i FROM Invoice i LEFT JOIN i.customer c " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:startOfDay IS NULL OR i.createdDate >= :startOfDay) " +
            "AND (:startOfNextDay IS NULL OR i.createdDate < :startOfNextDay) " +
            "ORDER BY i.createdDate DESC")
    Page<Invoice> searchByKeywordStatusAndCreatedDate(
            @Param("keyword") String keyword,
            @Param("status") TrangThaiTong status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable);

    Invoice findByInvoiceCode(String invoiceCode);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceCode = :invoiceCode")
    Optional<Invoice> findByInvoiceCodeQR(@Param("invoiceCode") String invoiceCode);

    Page<Invoice> findByStatusAndEmployeeId(TrangThaiTong status, Long employeeId, Pageable pageable);

    List<Invoice> findByStatusAndOrderTypeAndCreatedDateBefore(TrangThaiTong status, int orderType, LocalDateTime time);

    boolean existsByCustomer_IdAndStatus(Long customerId, TrangThaiTong status);

    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.isPaid = :isPaid")
    Optional<Invoice> findPaidInvoiceById(@Param("id") Long id,@Param("isPaid") Boolean isPaid);


    @Query("""
        SELECT MONTH(i.createdDate), SUM(i.finalAmount)
        FROM Invoice i
        WHERE YEAR(i.createdDate) = :year AND i.status = :status
        GROUP BY MONTH(i.createdDate)
        ORDER BY MONTH(i.createdDate)
    """)
    List<Object[]> getMonthlyRevenue(@Param("year") int year,@Param("status") TrangThaiTong status);

    @Query("""
        SELECT YEAR(i.createdDate), SUM(i.finalAmount)
        FROM Invoice i
        WHERE i.status = :status
        GROUP BY YEAR(i.createdDate)
        ORDER BY YEAR(i.createdDate)
    """)
    List<Object[]> getYearlyRevenue(@Param("status") TrangThaiTong status);

    @Query("""
        SELECT i.orderType, SUM(i.finalAmount)
        FROM Invoice i
        WHERE i.status = :status
        GROUP BY i.orderType
    """)
    List<Object[]> getRevenueByOrderType(@Param("status") TrangThaiTong status);

    @Query("""
        SELECT pd.product.id, pd.product.productName, SUM(idt.quantity)
        FROM InvoiceDetail idt
        JOIN idt.invoice i
        JOIN idt.productDetail pd
        WHERE i.status = :status AND i.createdDate BETWEEN :start AND :end
        GROUP BY pd.product.id, pd.product.productName
        ORDER BY SUM(idt.quantity) DESC
    """)
    List<Object[]> getTopSellingProducts(@Param("status") TrangThaiTong status,@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("""
    SELECT i.status, COUNT(i)
    FROM Invoice i
    GROUP BY i.status
""")
    List<Object[]> countInvoicesByStatus();

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
            "WHERE i.createdDate >= :startOfDay AND i.createdDate < :endOfDay AND i.status = :status")
    Long getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay,
                         @Param("endOfDay") LocalDateTime endOfDay,
                         @Param("status") TrangThaiTong status);

    Optional<Invoice> findByAppTransId(String appTransId);

    @Query("SELECT i.statusDetail, COUNT(i) FROM Invoice i WHERE i.statusDetail IS NOT NULL GROUP BY i.statusDetail")
    List<Object[]> countByStatusDetail();

    @Query("SELECT i FROM Invoice i WHERE i.customer.id = :customerId AND i.status = :status AND i.orderType = 1")
    List<Invoice> findSuccessfulOnlineInvoicesByCustomerId(@Param("customerId") Long customerId, @Param("status") TrangThaiTong status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.customer.id = :customerId AND i.status = :paidStatus")
    Long countPaidInvoicesByCustomer(@Param("customerId") Long customerId,
                                     @Param("paidStatus") TrangThaiTong paidStatus);

    int countByCustomerAndStatusDetailAndUpdatedDateAfter(
            Customer customer,
            TrangThaiChiTiet statusDetail,
            LocalDateTime updatedAfter
    );


}