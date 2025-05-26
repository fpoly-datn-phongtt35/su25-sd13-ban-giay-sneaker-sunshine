package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size,Long> {
}
