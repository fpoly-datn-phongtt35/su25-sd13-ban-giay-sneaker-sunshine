package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}