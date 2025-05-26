package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatus(int status);

    List<Invoice> findAllByOrderByCreatedDateDesc();

    @Query("SELECT i FROM Invoice i LEFT JOIN i.customer c " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:startOfDay IS NULL OR i.createdDate >= :startOfDay) " +
            "AND (:startOfNextDay IS NULL OR i.createdDate < :startOfNextDay)")
    Page<Invoice> searchByKeywordStatusAndCreatedDate(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable);


    Invoice findByInvoiceCode(String invoiceCode);


    @Query("SELECT i FROM Invoice i WHERE i.invoiceCode = :invoiceCode")
    Optional<Invoice> findByInvoiceCodeQR(@Param("invoiceCode") String invoiceCode);

}