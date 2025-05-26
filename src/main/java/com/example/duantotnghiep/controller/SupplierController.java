package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Style;
import com.example.duantotnghiep.model.Supplier;
import com.example.duantotnghiep.service.StyleService;
import com.example.duantotnghiep.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/supplier")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Supplier>> getAll(){
        List<Supplier> list = supplierService.getAll();
        return ResponseEntity.ok(list);
    }
}
