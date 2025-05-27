package com.example.duantotnghiep.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Size(max = 250)
    @Nationalized
    @Column(name = "role_name", length = 250)
    private String roleName;

    @Size(max = 255)
    @Nationalized
    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Size(max = 50)
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @OneToMany(mappedBy = "role")
    private Set<Customer> customers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "role")
    private Set<Employee> employees = new LinkedHashSet<>();

}