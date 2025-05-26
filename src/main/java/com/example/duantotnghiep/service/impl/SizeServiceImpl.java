package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Size;
import com.example.duantotnghiep.repository.SizeRepository;
import com.example.duantotnghiep.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepo;

    @Override
    public List<Size> getAll() {
        return sizeRepo.findByStatus();
    }
}
