package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.FavoriteProductResponse;
import com.example.duantotnghiep.model.FavoriteProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {
    Optional<FavoriteProduct> findByCustomerIdAndProductIdAndStatus(Long customerId, Long productId, int status);

    @Query("""
    SELECT f FROM FavoriteProduct f
    WHERE f.customer.id = :customerId AND f.status = 1
""")
    List<FavoriteProduct> getFavoritesByCustomer(@Param("customerId") Long customerId);

}
