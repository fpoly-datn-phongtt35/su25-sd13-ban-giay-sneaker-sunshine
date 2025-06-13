package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"employee"})
    @Query("SELECT u FROM User u WHERE u.employee.status = 1")
    Page<User> findAllByEmployeeIsNotNullAndStatusIsOne(Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.customer c WHERE c.id = :customerId and c.status = 1")
    Optional<User> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT u FROM User u JOIN FETCH u.employee e WHERE e.id = :employeeId AND e.status = 1")
    Optional<User> findByEmployeeId(@Param("employeeId") Long employeeId);


    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u " +
            "JOIN u.employee e " +
            "WHERE u.username = :username AND e.status = :status")
    boolean existsByUsernameAndEmployeeStatus(@Param("username") String username,
                                              @Param("status") Integer status);
    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.status = 0 WHERE e.id =:employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);



}

