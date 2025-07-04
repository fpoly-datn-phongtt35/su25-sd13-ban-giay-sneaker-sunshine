package com.example.duantotnghiep.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceDetailOnline {
    private Long id;
    private String code;
    private Long invoiceId;
    private Long productDetailId;
    private String productDetailName;
    private Long sizeId;
    private String sizeName;
    private Long colorId;
    private String colorName;
    private Integer quantity;

}
