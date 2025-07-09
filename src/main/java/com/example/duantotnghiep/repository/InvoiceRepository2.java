package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.InvoiceOnlineResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
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
            i.employee_id, e.employee_name,
            i.total_amount, i.discount_amount, i.final_amount,
            i.description, i.status, i.status_detail,
            i.created_date, i.updated_date,
            i.delivery_address, i.shipping_fee, i.delivered_at
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
                toLong(row[0]),
                toString(row[1]),
                toLong(row[2]),
                toString(row[3]),
                toLong(row[4]),
                toString(row[5]),
                toBigDecimal(row[6]),
                toBigDecimal(row[7]),
                toBigDecimal(row[8]),
                toString(row[9]),
                toInt(row[10]),       // dùng kiểu nguyên thủy int để tránh NullPointerException
                toInt(row[11]),       //
                toDate(row[12]),
                toDate(row[13]),
                toString(row[14]),
                toBigDecimal(row[15]),
                toDate(row[16])
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
}
