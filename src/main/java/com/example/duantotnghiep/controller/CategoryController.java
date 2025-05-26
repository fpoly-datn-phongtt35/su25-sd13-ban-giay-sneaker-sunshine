package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.response.CategoryResponse;
import com.example.duantotnghiep.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/hien-thi")
    public ResponseEntity<List<CategoryResponse>> getAll(){
        List<CategoryResponse> list = categoryService.getAll();
        return ResponseEntity.ok(list);
    }

}
