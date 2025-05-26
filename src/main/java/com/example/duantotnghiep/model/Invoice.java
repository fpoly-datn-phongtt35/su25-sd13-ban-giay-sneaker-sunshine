package com.example.duantotnghiep.model;

import jakarta.persistence.CascadeType;
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
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "invoice_code", length = 100, unique = true)
    private String invoiceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "total_amount", precision = 18, scale = 3)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 18, scale = 3)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", precision = 18, scale = 3)
    private BigDecimal finalAmount;

    @Size(max = 250)
    @Nationalized
    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "order_type")
    private Integer orderType;

    @Column(name = "status")
    private Integer status;

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

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceDetail> invoiceDetails;

}