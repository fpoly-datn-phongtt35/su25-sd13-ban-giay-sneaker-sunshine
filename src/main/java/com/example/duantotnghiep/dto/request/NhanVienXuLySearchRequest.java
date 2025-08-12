package com.example.duantotnghiep.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NhanVienXuLySearchRequest {
    private Date startDate;
    private Date endDate;
    private Long employeeId;
    private Integer orderType;
}
