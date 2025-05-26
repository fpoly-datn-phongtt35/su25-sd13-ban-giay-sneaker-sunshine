package com.example.duantotnghiep.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryRequest {

    private String categoryName;
    private String categoryCode;
    private String description;
    private String status;

    private Date createdDate;

    private Date updatedDate;

    private String createdBy;
    private String updatedBy;

}
