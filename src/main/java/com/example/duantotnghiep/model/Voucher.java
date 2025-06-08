package com.example.duantotnghiep.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "voucher_code", nullable = false, length = 150)
    private String voucherCode;

    @Size(max = 250)
    @Nationalized
    @Column(name = "voucher_name", length = 250)
    private String voucherName;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @NotNull
    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @NotNull
    @Column(name = "min_order_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount_value", precision = 10, scale = 2)
    private BigDecimal maxDiscountValue;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "status")
    private Integer status;

    @Size(max = 250)
    @Nationalized
    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Size(max = 50)
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "order_type")
    private Integer orderType;

    @OneToMany(mappedBy = "voucher")
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "voucher")
    private Set<com.example.duantotnghiep.model.VoucherHistory> voucherHistories = new LinkedHashSet<>();

}