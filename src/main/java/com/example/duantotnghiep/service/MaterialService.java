package com.example.duantotnghiep.service;

import com.example.duantotnghiep.model.Brand;
import com.example.duantotnghiep.model.Color;
import com.example.duantotnghiep.model.Material;

import java.util.List;

public interface MaterialService {
    List<Material> getAll();
    Material them(String name);
    Material sua(Long id,String name);
    void xoa(Long id);
}
