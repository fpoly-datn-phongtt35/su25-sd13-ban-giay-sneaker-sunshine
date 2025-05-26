package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Sole;
import com.example.duantotnghiep.model.Style;
import com.example.duantotnghiep.service.SoleService;
import com.example.duantotnghiep.service.StyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sole")
public class SoleController {
    @Autowired
    private SoleService service;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Sole>> getAll(){
        List<Sole> list = service.getAll();
        return ResponseEntity.ok(list);
    }
}
