package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.EmployeeRequest;
import com.example.duantotnghiep.dto.response.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    Page<EmployeeResponse> getAllUsers(Pageable pageable);

    EmployeeResponse getUserById(Long id);
    EmployeeResponse updateUser(Long id, EmployeeRequest employeeRequest);
    void deleteUser(Long id);
    EmployeeResponse createUser(EmployeeRequest employeeRequest);
}
