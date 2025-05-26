package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.model.Supplier;
import com.example.duantotnghiep.repository.SupplierRepository;
import com.example.duantotnghiep.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;

    @Override
    public List<Supplier> getAll() {
        return supplierRepository.findByStatus();
    }
}
