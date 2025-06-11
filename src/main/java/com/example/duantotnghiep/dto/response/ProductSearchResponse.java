package com.example.duantotnghiep.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductSearchResponse {
    private Integer id;
    private String productName;
    private String productCode;
    private String materialName;
    private String brandName;
    private String styleName;
    private String genderName;
    private String soleName;
    private String supplierName;
    private BigDecimal originPrice;
    private BigDecimal sellPrice;
    private Integer quantity;
    private String description;
    private BigDecimal weight;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;
    private List<String> categoryNames;
    private List<ProductDetailResponse> productDetails;
}
