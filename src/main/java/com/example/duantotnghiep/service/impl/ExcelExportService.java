package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.response.InvoiceDetailResponse;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportInvoicesToExcel(List<InvoiceDisplayResponse> invoices) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");

            // Style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Style cho dữ liệu text
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setAlignment(HorizontalAlignment.LEFT);
            textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            // Style cho số
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);

            // Header - thêm cột "Trạng thái"
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã hóa đơn", "Tên khách hàng", "Tên nhân viên", "Ngày tạo", "Tổng tiền", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            for (InvoiceDisplayResponse invoice : invoices) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(invoice.getInvoice().getId());
                row.getCell(0).setCellStyle(textStyle);

                row.createCell(1).setCellValue(invoice.getInvoice().getCustomerName());
                row.getCell(1).setCellStyle(textStyle);

                row.createCell(2).setCellValue(invoice.getInvoice().getEmployeeName());
                row.getCell(2).setCellStyle(textStyle);

                row.createCell(3).setCellValue(invoice.getInvoice().getCreatedDate().toString());
                row.getCell(3).setCellStyle(textStyle);

                row.createCell(4).setCellValue(currencyFormatter.format(calculateTotal(invoice.getDetails())));
                row.getCell(4).setCellStyle(numberStyle);

                // Lấy trạng thái dạng số từ invoice, ví dụ: invoice.getInvoice().getStatus()
                int status = invoice.getInvoice().getStatus();  // Giả sử có getStatus() trả về int

                // Chuyển trạng thái số sang text
                String statusText = convertStatus(status);

                row.createCell(5).setCellValue(statusText);
                row.getCell(5).setCellStyle(textStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // Hàm tính tổng tiền
    private double calculateTotal(List<InvoiceDetailResponse> details) {
        return details.stream()
                .mapToDouble(d -> d.getPrice().doubleValue() * d.getQuantity())
                .sum();
    }

    // Hàm chuyển trạng thái số thành chuỗi tương ứng
    private String convertStatus(int status) {
        switch (status) {
            case 0:
                return "Chờ xử lý";
            case 1:
                return "Đã thanh toán";
            case 2:
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }
}




