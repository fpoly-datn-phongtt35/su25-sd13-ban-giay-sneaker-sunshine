package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.AddressCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressCustomer,Long> {
    List<AddressCustomer> findByCustomerIdAndStatusOrderByDefaultAddressDesc(Long customerId,Integer status);

    @Modifying
    @Query("UPDATE AddressCustomer ca SET ca.defaultAddress = false WHERE ca.customer.id = :customerId")
    void clearDefaultAddress(@Param("customerId") Long customerId);

    AddressCustomer findFirstByCustomerIdAndDefaultAddressTrue(Long customerId);

}
