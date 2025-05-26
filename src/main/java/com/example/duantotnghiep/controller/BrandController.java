package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Brand;
import com.example.duantotnghiep.model.Color;
import com.example.duantotnghiep.service.BrandService;
import com.example.duantotnghiep.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brand")
public class BrandController {

    @Autowired
    private BrandService service;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Brand>> getAll(){
        List<Brand> list = service.getAll();
        return ResponseEntity.ok(list);
    }

}

