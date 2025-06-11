package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.response.UserDTO;
import com.example.duantotnghiep.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "employeeName", expression = "java(user.getEmployee() != null ? user.getEmployee().getEmployeeName() : null)")
    UserDTO toDto(User user);
}


