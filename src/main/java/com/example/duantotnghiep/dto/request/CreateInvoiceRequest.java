package com.example.duantotnghiep.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateInvoiceRequest {
    private Long customerId; // có thể null (khách lẻ)
    private Long employeeId;
}
