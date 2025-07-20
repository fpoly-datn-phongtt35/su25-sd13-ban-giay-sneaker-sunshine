package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.InvoiceOnlineResponse;
import com.example.duantotnghiep.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public class InvoiceRepository2 {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String BASE_SELECT = """
        SELECT
            i.id, i.invoice_code, i.customer_id, c.customer_name,
            i.employee_id, e.employee_name,c.phone,
            i.total_amount, i.discount_amount, i.final_amount,
            i.description, i.status, i.status_detail,
            i.created_date, i.updated_date,
            i.delivery_address, i.shipping_fee, i.delivered_at,i.is_paid
        FROM invoice i
        LEFT JOIN customer c ON c.id = i.customer_id
        LEFT JOIN employee e ON e.id = i.employee_id
    """;

    public InvoiceOnlineResponse getOrder(Long invoiceId) {
        String sql = BASE_SELECT + " WHERE i.id = :invoiceId";
        try {
            Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter("invoiceId", invoiceId)
                    .getSingleResult();
            return mapRowToInvoice(row);
        } catch (NoResultException e) {
            throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + invoiceId);
        }
    }

    public InvoiceOnlineResponse getOrder2(Long invoiceId, Long customerId) {
        String sql = BASE_SELECT + " WHERE i.id = :invoiceId AND i.customer_id = :customerId";
        try {
            Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter("invoiceId", invoiceId)
                    .setParameter("customerId", customerId)
                    .getSingleResult();
            return mapRowToInvoice(row);
        } catch (NoResultException e) {
            throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + invoiceId + " và customerId: " + customerId);
        }
    }

    public List<InvoiceOnlineResponse> getOrder3(Long customerId,Integer statusDetail) {
        String sql = BASE_SELECT + " WHERE i.customer_id = :customerId and i.status_detail = :statusDetail";
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("customerId", customerId)
                .setParameter("statusDetail",statusDetail)
                .getResultList();

        return rows.stream()
                .map(this::mapRowToInvoice)
                .toList();
    }

    // Mapping helper
    private InvoiceOnlineResponse mapRowToInvoice(Object[] row) {
        if (row == null || row.length < 17) {
            throw new RuntimeException("Dữ liệu không đầy đủ. Số cột nhận được: " + (row != null ? row.length : 0));
        }

        return new InvoiceOnlineResponse(
                toLong(row[0]),   // id
                toString(row[1]), // invoice_code
                toLong(row[2]),   // customer_id
                toString(row[3]), // customer_name
                toLong(row[4]),   // employee_id
                toString(row[5]), // employee_name
                toString(row[6]), // phone
                toBigDecimal(row[7]),  // total_amount
                toBigDecimal(row[8]),  // discount_amount
                toBigDecimal(row[9]),  // final_amount
                toString(row[10]),     // description
                toInt(row[11]),        // status
                toInt(row[12]),        // status_detail
                toDate(row[13]),       // created_date
                toDate(row[14]),       // updated_date
                toString(row[15]),     // delivery_address
                toBigDecimal(row[16]), // shipping_fee
                toDate(row[17]),        // delivered_at
                toBoolean(row[18])
        );
    }

    // Safe conversion helpers
    private Long toLong(Object o) {
        return o != null ? ((Number) o).longValue() : null;
    }

    private int toInt(Object o) {
        return o != null ? ((Number) o).intValue() : 0; //  tránh NPE bằng cách trả về 0 nếu null
    }

    private BigDecimal toBigDecimal(Object o) {
        return o != null ? (BigDecimal) o : BigDecimal.ZERO; // optional: có thể là null nếu bạn muốn
    }

    private String toString(Object o) {
        return o != null ? o.toString() : null;
    }

    private Date toDate(Object o) {
        return o != null ? (Date) o : null;
    }
    private Boolean toBoolean(Object o) {
        return o != null ? Boolean.valueOf(o.toString()) : false;
    }

}
