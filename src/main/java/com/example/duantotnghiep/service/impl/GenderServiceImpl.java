package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Gender;
import com.example.duantotnghiep.repository.GenderRepository;
import com.example.duantotnghiep.service.GenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenderServiceImpl implements GenderService {
    private final GenderRepository genderRepository;

    @Override
    public List<Gender> getAll() {
        return genderRepository.findByStatus();
    }
}
