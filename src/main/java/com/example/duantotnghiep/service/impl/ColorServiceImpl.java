package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Color;
import com.example.duantotnghiep.repository.ColorRepository;
import com.example.duantotnghiep.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {
    private final ColorRepository colorRepository;

    @Override
    public List<Color> getAll() {
        return colorRepository.findByStatus();
    }
}
