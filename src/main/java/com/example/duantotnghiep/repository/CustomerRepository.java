package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.phone LIKE CONCAT(:phonePrefix, '%')")
    List<Customer> findByPhoneStartingWith(@Param("phonePrefix") String phonePrefix);

}