package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Sole;
import com.example.duantotnghiep.repository.SoleRepository;
import com.example.duantotnghiep.service.SoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoleServiceImpl implements SoleService {
    private final SoleRepository soleRepository;

    @Override
    public List<Sole> getAll() {
        return soleRepository.findByStatus();
    }
}
