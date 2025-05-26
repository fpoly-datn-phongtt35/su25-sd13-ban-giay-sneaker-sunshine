package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Sole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoleRepository extends JpaRepository<Sole,Long> {
}
