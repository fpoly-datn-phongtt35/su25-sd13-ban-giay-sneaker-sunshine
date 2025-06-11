package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Size;
import com.example.duantotnghiep.model.Sole;
import com.example.duantotnghiep.service.SizeService;
import com.example.duantotnghiep.service.SoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/size")
public class SizeController {
    @Autowired
    private SizeService service;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Size>> getAll(){
        List<Size> list = service.getAll();
        return ResponseEntity.ok(list);
    }
}
