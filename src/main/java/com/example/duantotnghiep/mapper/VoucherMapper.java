package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.response.VoucherResponse;
import com.example.duantotnghiep.model.Voucher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    VoucherResponse toVoucherResponse(Voucher voucher);
}
