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
    private String brandName;
    private String styleName;
    private String genderName;
    private String soleName;
    private String supplierName;
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

    public ProductResponse(Long id, @Size(max = 250) String productName, @NotNull BigDecimal sellPrice) {
    }
}
