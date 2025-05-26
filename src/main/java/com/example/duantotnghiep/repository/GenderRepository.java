package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenderRepository extends JpaRepository<Gender,Long> {
}
