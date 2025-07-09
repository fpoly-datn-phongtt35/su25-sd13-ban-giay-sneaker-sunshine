package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.EmployeeRequest;
import com.example.duantotnghiep.dto.response.EmployeeResponse;
import com.example.duantotnghiep.model.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {


    EmployeeResponse toEmployeeResponse(Employee employee);

    Employee toEntity(EmployeeRequest employeeRequest);
}
