package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.CustomerRequest;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.dto.response.EmployeeResponse;
import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.Employee;
import com.example.duantotnghiep.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    EmployeeResponse toDto(Employee employee);

    Customer toDto(CustomerRequest customerRequest);
}
