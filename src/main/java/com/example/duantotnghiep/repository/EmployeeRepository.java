package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailAndStatus(String email,Integer status);
    boolean existsByPhoneAndStatus(String phone,Integer status);

    @Query("""
        select e from Employee e where e.status = 1 
                """)
    List<Employee> getData();

}