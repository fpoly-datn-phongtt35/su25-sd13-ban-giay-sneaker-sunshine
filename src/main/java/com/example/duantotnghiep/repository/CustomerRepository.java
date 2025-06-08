package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Customer;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.phone LIKE CONCAT(:phonePrefix, '%')")
    List<Customer> findByPhoneStartingWith(@Param("phonePrefix") String phonePrefix);

}