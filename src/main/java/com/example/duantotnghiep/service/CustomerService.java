package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.AddressCustomerRequest;
import com.example.duantotnghiep.dto.request.CustomerRequest;
import com.example.duantotnghiep.dto.response.AddressCustomerResponse;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    void deleteCustomer(Long id);
    CustomerResponse getCustomerById(Long id);
    List<CustomerResponse> getAllCustomers();
    Page<CustomerResponse> findCustomerByStatus( Pageable pageable);

    List<AddressCustomerResponse> getByCustomerId(Long customerId);

    AddressCustomerResponse create(AddressCustomerRequest request);
    AddressCustomerResponse getAddressById(Long id);

    AddressCustomerResponse update(Long id, AddressCustomerRequest request);

    void deleteAddressCustomer(Long id);
    void setDefaultAddress(Long customerId, Long addressId);

}
