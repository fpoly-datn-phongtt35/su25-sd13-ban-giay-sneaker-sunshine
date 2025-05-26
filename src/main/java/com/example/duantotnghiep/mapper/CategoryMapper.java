package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.response.CategoryResponse;
import com.example.duantotnghiep.model.Category;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductCategory;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
