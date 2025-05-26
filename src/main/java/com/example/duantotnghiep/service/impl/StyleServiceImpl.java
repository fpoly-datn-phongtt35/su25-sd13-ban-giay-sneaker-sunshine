package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Style;
import com.example.duantotnghiep.repository.StyleRepository;
import com.example.duantotnghiep.service.StyleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StyleServiceImpl implements StyleService {
    private final StyleRepository styleRepository;

    @Override
    public List<Style> getAll() {
        return styleRepository.findByStatus();
    }
}
