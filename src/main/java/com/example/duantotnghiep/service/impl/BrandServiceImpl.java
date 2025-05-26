package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Brand;
import com.example.duantotnghiep.repository.BrandRepository;
import com.example.duantotnghiep.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    @Override
    public List<Brand> getAll() {
        return brandRepository.findByStatus();
    }

}
