package com.example.duantotnghiep.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceStatusStatisticResponse {
    private Integer statusCode;      // vẫn giữ code nếu cần
    private String status; // chuỗi hiển thị
    private Long totalInvoices;  // số lượng hóa đơn
}
