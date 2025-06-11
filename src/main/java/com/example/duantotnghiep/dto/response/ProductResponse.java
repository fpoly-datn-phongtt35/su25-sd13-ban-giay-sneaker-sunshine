package com.example.duantotnghiep.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProductResponse {
    private Long id;
    private String productCode;
    private String productName;
    private String materialName;
    private String materialId;
    private String brandName;
    private String brandId;
    private String styleName;
    private String styleId;
    private String genderName;
    private String genderId;
    private String soleName;
    private String soleId;
    private String supplierName;
    private String supplierId;
    private BigDecimal originPrice;
    private BigDecimal sellPrice;
    private Integer quantity;
    private String description;
    private Integer status;
    private BigDecimal weight;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;

    private List<String> categoryNames;
    private List<ProductDetailResponse> productDetails;
    private List<ProductImageResponse> productImages;
    private List<CategoryResponse> categories;

}
