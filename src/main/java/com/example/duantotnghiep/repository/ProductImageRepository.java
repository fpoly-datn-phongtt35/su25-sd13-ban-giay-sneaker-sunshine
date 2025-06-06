package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {

    @Query("SELECT pi FROM ProductImage pi WHERE pi.id IN :ids AND pi.status = :status")
    List<ProductImage> findAllByIdAndStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

}
