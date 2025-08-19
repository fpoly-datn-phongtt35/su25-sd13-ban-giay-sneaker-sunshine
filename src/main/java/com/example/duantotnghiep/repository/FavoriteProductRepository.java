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

    @Query("""
            select new com.example.duantotnghiep.dto.response.FavoriteProductResponse(
            fp.id,fp.customer.id,c.customerName,p.id,fp.rate,fp.comment,fp.createdAt
            ) from FavoriteProduct fp 
              left join Product p on p.id = fp.product.id 
              left join Customer c on c.id = fp.customer.id
              where p.id = :productId and fp.status = 1 order by fp.createdAt desc 
            """)
    List<FavoriteProductResponse> getFavoritesByProductId(@Param("productId") Long productId);

}
