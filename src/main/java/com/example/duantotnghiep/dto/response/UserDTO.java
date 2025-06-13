package com.example.duantotnghiep.dto.response;

import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private Integer role;
    private Employee employee;
    private Customer customer;         // 👈 Thêm dòng này nếu chưa có
    private String employeeName;
    private String customerName;
}
