package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Color;
import com.example.duantotnghiep.model.Gender;
import com.example.duantotnghiep.service.ColorService;
import com.example.duantotnghiep.service.GenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/color")
public class ColorController {
    @Autowired
    private ColorService service;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Color>> getAll(){
        List<Color> list = service.getAll();
        return ResponseEntity.ok(list);
    }

}
