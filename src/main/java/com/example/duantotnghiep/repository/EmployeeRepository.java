package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailAndStatus(String email,Integer status);
    boolean existsByPhoneAndStatus(String phone,Integer status);

}