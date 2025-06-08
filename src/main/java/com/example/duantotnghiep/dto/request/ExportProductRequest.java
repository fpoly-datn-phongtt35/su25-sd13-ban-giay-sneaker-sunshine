package com.example.duantotnghiep.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExportProductRequest {
    private ProductSearchRequest filter;  // điều kiện tìm kiếm (có thể null)
    private List<Long> productIds;       // danh sách Id sản phẩm (có thể null hoặc rỗng)
}
