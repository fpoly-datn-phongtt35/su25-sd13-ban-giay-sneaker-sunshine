package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.response.CategoryResponse;
import com.example.duantotnghiep.mapper.CategoryMapper;
import com.example.duantotnghiep.repository.CategoryRepository;
import com.example.duantotnghiep.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findByStatus().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
