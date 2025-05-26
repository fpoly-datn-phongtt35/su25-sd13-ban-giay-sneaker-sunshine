package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Material;
import com.example.duantotnghiep.repository.MaterialRepository;
import com.example.duantotnghiep.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository materialRepository;

    @Override
    public List<Material> getAll() {
        return materialRepository.findByStatus();
    }
}
