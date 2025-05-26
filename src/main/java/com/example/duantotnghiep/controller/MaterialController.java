package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Material;
import com.example.duantotnghiep.model.Size;
import com.example.duantotnghiep.service.MaterialService;
import com.example.duantotnghiep.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/material")
public class MaterialController {
    @Autowired
    private MaterialService service;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Material>> getAll(){
        List<Material> list = service.getAll();
        return ResponseEntity.ok(list);
    }
}
