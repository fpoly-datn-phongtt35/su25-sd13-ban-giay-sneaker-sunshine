package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.AddressCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<AddressCustomer,Long> {
}
