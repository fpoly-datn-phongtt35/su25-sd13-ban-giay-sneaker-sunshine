package com.example.duantotnghiep.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDisplayResponse {
    private InvoiceResponse invoice;
    private List<InvoiceDetailResponse> details;
}
