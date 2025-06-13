package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail,Long> {
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.status = :status")
    List<ProductDetail> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") Integer status);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.status = :status")
    List<ProductDetail> findByProductIdAndStatusRemoved(@Param("productId") Long productId, @Param("status") Integer status);

    List<ProductDetail> findByProductId(Long productId);

}
