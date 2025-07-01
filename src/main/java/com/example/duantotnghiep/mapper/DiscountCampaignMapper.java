package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignBrandResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignScopeResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignStyleResponse;
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignBrand;
import com.example.duantotnghiep.model.DiscountCampaignScope;
import com.example.duantotnghiep.model.DiscountCampaignStyle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DiscountCampaignMapper {

    // Entity -> Response
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.brandName", target = "brandName")
    DiscountCampaignBrandResponse toBrandResponse(DiscountCampaignBrand entity);

    @Mapping(source = "style.id", target = "styleId")
    @Mapping(source = "style.styleName", target = "styleName")
    DiscountCampaignStyleResponse toStyleResponse(DiscountCampaignStyle entity);

    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.brandName", target = "brandName")
    @Mapping(source = "style.id", target = "styleId")
    @Mapping(source = "style.styleName", target = "styleName")
    DiscountCampaignScopeResponse toScopeResponse(DiscountCampaignScope entity);

    DiscountCampaignResponse toResponse(DiscountCampaign entity);

    // KHÔNG map list brand/style từ request → entity (MapStruct sẽ không làm đúng)
    // => Chỉ map trường cơ bản
    @Mapping(target = "brands", ignore = true)
    @Mapping(target = "styles", ignore = true)
    @Mapping(target = "scopes", ignore = true)
    DiscountCampaign toEntity(DiscountCampaignRequest dto);
}






