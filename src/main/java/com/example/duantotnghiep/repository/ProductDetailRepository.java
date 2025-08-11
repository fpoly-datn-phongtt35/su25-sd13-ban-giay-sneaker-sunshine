package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail,Long> {
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.status = :status")
    List<ProductDetail> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") Integer status);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.status = :status")
    List<ProductDetail> findByProductIdAndStatusRemoved(@Param("productId") Long productId, @Param("status") Integer status);

    List<ProductDetail> findByProductId(Long productId);

    List<ProductDetail> findByProductIdAndColorId(Long productId, Long colorId);


    @Query("""
        SELECT pd FROM ProductDetail pd
        JOIN FETCH pd.product p
        LEFT JOIN FETCH pd.color
        LEFT JOIN FETCH pd.size
        WHERE pd.productDetailCode = :code AND pd.status = 1 AND p.status = 1
    """)
    Optional<ProductDetail> findActiveByDetailCode(@Param("code") String code);

    @Query("""
        SELECT pd FROM ProductDetail pd
        LEFT JOIN FETCH pd.color
        LEFT JOIN FETCH pd.size
        WHERE pd.product.id = :productId AND pd.status = 1
    """)
    List<ProductDetail> findActiveByProductIdFetchAttrs(@Param("productId") Long productId);

}
