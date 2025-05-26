package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}