package com.example.duantotnghiep.mapper;


import com.example.duantotnghiep.dto.response.UserDTO;
import com.example.duantotnghiep.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.duantotnghiep.dto.request.EmployeeRequest;
import com.example.duantotnghiep.dto.response.EmployeeResponse;
import com.example.duantotnghiep.model.Employee;
import com.example.duantotnghiep.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring")
public interface UserMapper {




    @Mapping(target = "employeeName", expression = "java(user.getEmployee() != null ? user.getEmployee().getEmployeeName() : null)")
    UserDTO toDto(User user);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "id", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "employeeName", source = "employee.employeeName")
    @Mapping(target = "email", source = "employee.email")
    @Mapping(target = "phone", source = "employee.phone")
    @Mapping(target = "gender", source = "employee.gender")
    @Mapping(target = "dateOfBirth", source = "employee.dateOfBirth")
    @Mapping(target = "hireDate", source = "employee.hireDate")
    @Mapping(target = "salary", source = "employee.salary")
    @Mapping(target = "country", source = "employee.country")
    @Mapping(target = "province", source = "employee.province")
    @Mapping(target = "district", source = "employee.district")
    @Mapping(target = "ward", source = "employee.ward")
    @Mapping(target = "houseName", source = "employee.houseName")
    @Mapping(target = "createdBy", source = "employee.createdBy")
    @Mapping(target = "createdDate", source = "employee.createdDate")
    @Mapping(target = "updatedBy", source = "employee.updatedBy")
    @Mapping(target = "updatedDate", source = "employee.updatedDate")
    EmployeeResponse toEmployeeResponse(User user);

    Employee toEmployeeEntity(EmployeeRequest request);

    User toUserEntity(EmployeeRequest employeeRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromRequest(EmployeeRequest request, @MappingTarget Employee employee);
}

