package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    @Query("""
        select p from Product p where p.id =:id and p.status = 1
""")
    Optional<Product> getOneByStatus(@Param("id") Long productId);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.productImages pi
    LEFT JOIN FETCH p.productCategories pc
    LEFT JOIN FETCH pc.category c
    LEFT JOIN FETCH p.productDetails pd
    WHERE p.id = :id AND p.status = 1
""")
    Optional<Product> getProductWithAllRelations(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.productImages pi
    LEFT JOIN FETCH p.productCategories pc
    LEFT JOIN FETCH pc.category c
    LEFT JOIN FETCH p.productDetails pd
    WHERE p.status = 1
""")
    List<Product> getAllProductsWithAllRelations();

}
