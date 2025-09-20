package com.example.duantotnghiep.dto.response;

import com.example.duantotnghiep.state.TrangThaiChiTiet;
import com.example.duantotnghiep.state.TrangThaiTong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceResponse {
    Long id;
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    String description;
    Integer orderType;
    TrangThaiTong status;
    Date createdDate;
    Date updatedDate;
    String createdBy;
    String updatedBy;
    private Integer request;


    private String customerName;

    private String employeeName;

    private String phone;

    private String invoiceCode;

    private Long customerId;

    private VoucherResponse voucher;
    private BigDecimal shippingFee;
    private Boolean isPaid;
    private TrangThaiChiTiet statusDetail;
    private Date deliveredAt;

    private String deliveryAddress;

    private List<InvoiceDetailResponse> invoiceDetails;

    public InvoiceResponse(Long id, String invoiceCode, TrangThaiChiTiet status, Integer orderType,
                           Date createdDate, String customerName, String phone, BigDecimal totalAmount,BigDecimal finalAmount) {
        this.id = id;
        this.invoiceCode = invoiceCode;
        this.statusDetail = status;
        this.orderType = orderType;
        this.createdDate = createdDate;
        this.customerName = customerName;
        this.phone = phone;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
    }

}