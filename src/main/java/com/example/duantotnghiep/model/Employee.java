package com.example.duantotnghiep.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "employee_code", nullable = false, length = 50)
    private String employeeCode;

    @Size(max = 250)
    @NotNull
    @Nationalized
    @Column(name = "employee_name", nullable = false, length = 250)
    private String employeeName;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Size(max = 50)
    @NotNull
    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Size(max = 250)
    @NotNull
    @Nationalized
    @Column(name = "email", nullable = false, length = 250)
    private String email;

    @NotNull
    @Column(name = "salary", nullable = false, precision = 18, scale = 3)
    private BigDecimal salary;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "pass_word", nullable = false, length = 200)
    private String passWord;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Size(max = 50)
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @OneToMany(mappedBy = "employee")
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<User> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<Voucher> vouchers = new LinkedHashSet<>();

}