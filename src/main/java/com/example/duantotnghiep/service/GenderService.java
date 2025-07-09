package com.example.duantotnghiep.service;

import com.example.duantotnghiep.model.Gender;

import java.util.List;

public interface GenderService {
    List<Gender> getAll();
    Gender them(String name);

}
