package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.model.Brand;
import com.example.duantotnghiep.model.Gender;
import com.example.duantotnghiep.model.Material;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.Sole;
import com.example.duantotnghiep.model.Style;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {CategoryMapper.class, ProductImageMapper.class, ProductDetailMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "materialName", source = "material.materialName")
    @Mapping(target = "brandName", source = "brand.brandName")
    @Mapping(target = "styleName", source = "style.styleName")
    @Mapping(target = "genderName", source = "gender.genderName")
    @Mapping(target = "soleName", source = "sole.soleName")
    ProductResponse toResponse(Product product);

    @Mapping(target = "material.id", source = "materialId")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "style.id", source = "styleId")
    @Mapping(target = "gender.id", source = "genderId")
    @Mapping(target = "sole.id", source = "soleId")
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "productCategories", ignore = true)
    @Mapping(target = "productDetails", ignore = true)
    Product toEntity(ProductRequest request);

    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "material", source = "materialId")
    @Mapping(target = "brand", source = "brandId")
    @Mapping(target = "style", source = "styleId")
    @Mapping(target = "gender", source = "genderId")
    @Mapping(target = "sole", source = "soleId")
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);

    // Helper method chuyển Long -> Material
    default Material mapMaterial(Long id) {
        if (id == null) {
            return null;
        }
        Material material = new Material();
        material.setId(id);
        return material;
    }

    // Helper method chuyển Long -> Brand
    default Brand mapBrand(Long id) {
        if (id == null) {
            return null;
        }
        Brand brand = new Brand();
        brand.setId(id);
        return brand;
    }

    // Helper method chuyển Long -> Style
    default Style mapStyle(Long id) {
        if (id == null) {
            return null;
        }
        Style style = new Style();
        style.setId(id);
        return style;
    }

    // Helper method chuyển Long -> Gender
    default Gender mapGender(Long id) {
        if (id == null) {
            return null;
        }
        Gender gender = new Gender();
        gender.setId(id);
        return gender;
    }

    // Helper method chuyển Long -> Sole
    default Sole mapSole(Long id) {
        if (id == null) {
            return null;
        }
        Sole sole = new Sole();
        sole.setId(id);
        return sole;
    }

}
