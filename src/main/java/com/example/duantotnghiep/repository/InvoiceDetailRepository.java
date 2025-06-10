package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {
    List<InvoiceDetail> findByInvoiceId(Long invoiceId);

    @Query("SELECT d FROM InvoiceDetail d WHERE d.invoice.invoiceCode = :invoiceCode")
    List<InvoiceDetail> findByInvoiceCodeQR(@Param("invoiceCode") String invoiceCode);

    Optional<InvoiceDetail> findByInvoiceIdAndProductDetailId(Long invoiceId, Long productDetailId);

    Optional<InvoiceDetail> findByInvoiceAndProductDetail(Invoice invoice, ProductDetail productDetail);
    List<InvoiceDetail> findByInvoice(Invoice invoice);


    @Query("SELECT d FROM InvoiceDetail d JOIN FETCH d.productDetail WHERE d.invoice = :invoice")
    List<InvoiceDetail> findByInvoiceWithProductDetail(@Param("invoice") Invoice invoice);

}