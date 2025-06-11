package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Style;
import com.example.duantotnghiep.service.StyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/style")
public class StyleController {
    @Autowired
    private StyleService styleService;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<Style>> getAll(){
        List<Style> list = styleService.getAll();
        return ResponseEntity.ok(list);
    }
}
