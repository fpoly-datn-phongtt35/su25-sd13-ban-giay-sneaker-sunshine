package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.ProductDetailRequest;
import com.example.duantotnghiep.dto.response.ProductDetailResponse;
import com.example.duantotnghiep.model.ProductDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {
    @Mapping(target = "sizeId", source = "size.id")
    @Mapping(target = "sizeName", source = "size.sizeName")
    @Mapping(target = "colorId", source = "color.id")
    @Mapping(target = "colorName", source = "color.colorName")
    @Mapping(target = "barcode", source = "productDetailCode")
    ProductDetailResponse toResponse(ProductDetail productDetail);

    @Mapping(target = "size.id", source = "sizeId")
    @Mapping(target = "color.id", source = "colorId")
    @Mapping(target = "product", ignore = true)
    ProductDetail fromRequest(ProductDetailRequest request);

    List<ProductDetail> mapProductDetailRequests(List<ProductDetailRequest> requests);
}
