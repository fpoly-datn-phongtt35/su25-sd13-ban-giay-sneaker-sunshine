package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.AddressCustomer;
import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.Employee;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.phone LIKE CONCAT(:phonePrefix, '%') AND c.status = 1")
    List<Customer> findByPhoneStartingWithAndStatusActive(@Param("phonePrefix") String phonePrefix);

    List<Customer> findCustomerByStatus(Integer status);

    Optional<Customer> findCustomerByIdAndStatus(Long id, Integer status);

    @Query("SELECT c FROM Customer c WHERE c.status = :status")
    Page<Customer> findCustomerByStatus1(@Param("status") Integer status, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<Customer> findTop1ByPhoneAndStatus(String phone, int status);


    public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {}

    public interface InvoiceRepository extends JpaRepository<Invoice, Long> {}

    public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {}

    public interface AddressCustomerRepository extends JpaRepository<AddressCustomer, Long> {}

    public interface EmployeeRepository extends JpaRepository<Employee, Long> {}

    public interface VoucherRepository extends JpaRepository<Voucher, Long> {}

    List<Customer> findAllByIsBlacklistedTrue();

    List<Customer> findByIsBlacklistedTrue();

}