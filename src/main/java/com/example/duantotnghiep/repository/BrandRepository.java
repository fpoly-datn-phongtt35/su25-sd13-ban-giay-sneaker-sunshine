package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {
}
