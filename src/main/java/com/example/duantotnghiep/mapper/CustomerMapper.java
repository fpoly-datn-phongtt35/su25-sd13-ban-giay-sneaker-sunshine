package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.CustomerRequest;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toDto(Customer customer);

    Customer toDto(CustomerRequest customerRequest);
}
