package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.qr.NumberToVietnameseWords;
import com.example.duantotnghiep.qr.QRCodeUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceExportService {

    public void exportInvoice(HttpServletResponse response, Invoice invoice, List<InvoiceDetail> details)
            throws IOException, DocumentException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice_" + invoice.getInvoiceCode() + ".pdf");

        // Kích thước giấy nhỏ (khoảng 58mm x 800pt)
        Rectangle smallPage = new Rectangle(226, 800);
        Document document = new Document(smallPage, 10, 10, 10, 10);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Load font Unicode hỗ trợ tiếng Việt
        String fontPath = "C:/Users/ADMIN/Downloads/Be_Vietnam_Pro/BeVietnamPro-Bold.ttf";
        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontTitle = new Font(baseFont, 12, Font.BOLD);
        Font fontNormal = new Font(baseFont, 9);
        Font fontBold = new Font(baseFont, 9, Font.BOLD);

        // Thông tin cửa hàng
        Paragraph storeName = new Paragraph("SunShine Sneaker Store", fontBold);
        storeName.setAlignment(Element.ALIGN_CENTER);
        document.add(storeName);

        document.add(new Paragraph("ĐC: Cầu Giấy, Hà Nội", fontNormal));
        document.add(new Paragraph("Mã số: 0105458483 - 0907.123.321", fontNormal));
        document.add(Chunk.NEWLINE);

        // Tiêu đề hóa đơn
        Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", fontBold);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Thông tin hóa đơn
        document.add(new Paragraph("Ngày: " + invoice.getCreatedDate(), fontNormal));
        document.add(new Paragraph("Số phiếu: " + invoice.getInvoiceCode(), fontNormal));
        document.add(new Paragraph("Thu ngân: " + invoice.getEmployee().getEmployeeName(), fontNormal));
        document.add(new Paragraph("Khách hàng: " + (invoice.getCustomer() != null
                ? invoice.getCustomer().getCustomerName() : "Khách lẻ"), fontNormal));
        document.add(Chunk.NEWLINE);

        // Tiêu đề bảng sản phẩm (không có dòng "Mặt hàng SL ĐVT Giá T.Tiền" theo yêu cầu)
        // document.add(new Paragraph("Mặt hàng         SL ĐVT   Giá     T.Tiền", fontBold));
        // document.add(new Paragraph("--------------------------------------", fontNormal));

        // In danh sách sản phẩm, căn chỉnh tên, số lượng, đơn vị, giá, thành tiền
        for (InvoiceDetail detail : details) {
            String name = detail.getProductDetail().getProduct().getProductName();
            int qty = detail.getQuantity();
            String unit = "đôi";
            BigDecimal price = detail.getProductDetail().getSellPrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(qty));

            String line = String.format("%-22s %3d %-5s %10s %12s",
                    truncate(name, 22),
                    qty,
                    unit,
                    formatCurrency(price),
                    formatCurrency(total));
            document.add(new Paragraph(line, fontNormal));
        }

        document.add(new Paragraph("---------------------------------------------", fontNormal));

        // Hiển thị giảm giá nếu có
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            Paragraph discount = new Paragraph("Giảm giá: " + formatCurrency(invoice.getDiscountAmount()), fontNormal);
            discount.setAlignment(Element.ALIGN_RIGHT);
            document.add(discount);
        }

        // Tổng tiền sau giảm
        Paragraph total = new Paragraph("Tổng cộng: " + formatCurrency(invoice.getFinalAmount()), fontBold);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        // Bằng chữ
        document.add(new Paragraph("Bằng chữ: " +
                NumberToVietnameseWords.convert(invoice.getFinalAmount()) + " đồng.", fontNormal));

        document.add(Chunk.NEWLINE);

        // QR code
        document.add(new Paragraph("Mã QR:", fontNormal));
        Image qrImage = QRCodeUtil.generateQRCode(invoice.getInvoiceCode(), 100, 100);
        if (qrImage != null) {
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);
        }

        // Lời cảm ơn
        Paragraph thanks = new Paragraph("Xin cảm ơn Quý khách! Hẹn gặp lại!", fontNormal);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        document.close();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }

    private String truncate(String str, int maxLength) {
        return str.length() > maxLength ? str.substring(0, maxLength - 1) + "…" : str;
    }
}



