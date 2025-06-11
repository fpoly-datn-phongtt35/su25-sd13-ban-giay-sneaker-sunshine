package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.request.EmployeeRequest;
import com.example.duantotnghiep.dto.response.EmployeeResponse;
import com.example.duantotnghiep.model.Employee;
import com.example.duantotnghiep.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {


    EmployeeResponse toEmployeeResponse(Employee employee);

    Employee toEntity(EmployeeRequest employeeRequest);
}
