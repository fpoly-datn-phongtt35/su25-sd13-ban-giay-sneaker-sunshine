package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    @Query("""
    SELECT p 
    FROM Product p 
    LEFT JOIN p.productImages pi 
    WHERE p.id = :id AND p.status = 1 AND pi.status = 1
""")
    Optional<Product> getOneByStatus(@Param("id") Long productId);

    @Query("SELECT p FROM Product p where p.status = 1 order by p.createdDate desc ")
    Page<Product> findAllWithJPQL(Pageable pageable);

}
