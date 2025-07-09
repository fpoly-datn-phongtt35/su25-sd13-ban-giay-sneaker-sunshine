package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.repository.InvoiceDetailRepository;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import com.example.duantotnghiep.state.TrangThaiTong;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceCleanupTask {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ProductDetailRepository productDetailRepository;

    /**
     * Chạy mỗi 30 giây để kiểm tra hóa đơn chưa thanh toán quá 10 phút
     */
    @Scheduled(fixedRate = 60000) // 30 giây một lần
    public void cancelUnpaidInvoices() {
        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(10);

        // Tìm hóa đơn status = 0 (chưa thanh toán), tạo trước hơn 10 phút
        List<Invoice> expiredInvoices = invoiceRepository.findByStatusAndOrderTypeAndCreatedDateBefore(TrangThaiTong.DANG_XU_LY, 0, limitTime);

        for (Invoice invoice : expiredInvoices) {
            List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceAndStatus(invoice, 1);

            for (InvoiceDetail detail : details) {
                ProductDetail productDetail = detail.getProductDetail();
                productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
                productDetailRepository.save(productDetail);
            }

            // Xóa chi tiết hóa đơn
            invoiceDetailRepository.deleteAll(details);

            // Cập nhật trạng thái hóa đơn
            invoice.setStatus(TrangThaiTong.DA_HUY); // 2 = đã hủy
            invoice.setUpdatedDate(new Date());
            invoice.setTotalAmount(BigDecimal.ZERO);
            invoice.setFinalAmount(BigDecimal.ZERO);
            invoice.setDiscountAmount(BigDecimal.ZERO);
            invoice.setUpdatedDate(new Date());
            invoiceRepository.save(invoice);
        }

        if (!expiredInvoices.isEmpty()) {
            System.out.println("Đã hủy " + expiredInvoices.size() + " hóa đơn quá hạn.");
        }
    }
}

