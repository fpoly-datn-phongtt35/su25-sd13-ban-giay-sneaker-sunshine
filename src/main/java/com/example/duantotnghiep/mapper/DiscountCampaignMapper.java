package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignProductResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignProductDetailResponse;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignProduct;
import com.example.duantotnghiep.model.DiscountCampaignProductDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DiscountCampaignMapper {

    // Entity -> Response cho sản phẩm
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    DiscountCampaignProductResponse toProductResponse(DiscountCampaignProduct entity);

    // Entity -> Response cho sản phẩm chi tiết (chỉ map id nếu không có name)
    @Mapping(source = "productDetail.id", target = "productDetailId")
    DiscountCampaignProductDetailResponse toProductDetailResponse(DiscountCampaignProductDetail entity);

    // Entity -> Response chính
    DiscountCampaignResponse toResponse(DiscountCampaign entity);

    // Request -> Entity (bỏ qua danh sách products & productDetails)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "productDetails", ignore = true)
    DiscountCampaign toEntity(DiscountCampaignRequest dto);
}











