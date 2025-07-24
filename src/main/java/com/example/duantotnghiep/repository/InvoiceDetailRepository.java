package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.InvoiceDetailOnline;
import com.example.duantotnghiep.dto.response.RatingProductResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {
    List<InvoiceDetail> findByInvoiceId(Long invoiceId);

    @Query("SELECT d FROM InvoiceDetail d WHERE d.invoice.invoiceCode = :invoiceCode")
    List<InvoiceDetail> findByInvoiceCodeQR(@Param("invoiceCode") String invoiceCode);


    Optional<InvoiceDetail> findByInvoiceAndProductDetail(Invoice invoice, ProductDetail productDetail);
    List<InvoiceDetail> findByInvoiceAndStatus(Invoice invoice, Integer status);

    List<InvoiceDetail> findByInvoiceIdIn(Collection<Long> invoiceIds);


    @Query("""
        select new com.example.duantotnghiep.dto.response.RatingProductResponse(
          i.id,p.id,p.productName
        ) from Invoice i
        left join InvoiceDetail id on i.id = id.invoice.id
        left join ProductDetail pd on pd.id = id.productDetail.id
        left join Product p on p.id = pd.product.id
        where i.id = :invoiceId
"""
    )
    List<RatingProductResponse> findAllByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("""
    select new com.example.duantotnghiep.dto.response.InvoiceDetailOnline(
    id.id,id.invoiceCodeDetail,id.invoice.id,id.productDetail.id,id.productDetail.product.productName,id.productDetail.size.id,id.productDetail.size.sizeName,
    id.productDetail.color.id,id.productDetail.color.colorName,id.quantity,id.sellPrice,id.discountedPrice,id.discountPercentage
    ) from InvoiceDetail  id
    where id.invoice.id =:invoiceId
""")
    List<InvoiceDetailOnline> findByInvoiceDetailOnline(@Param("invoiceId") Long invoiceId);
}