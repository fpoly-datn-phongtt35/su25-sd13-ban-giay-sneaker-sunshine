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
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.id = :id and p.status = 1")
    Optional<Product> findByStatus(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.id = :id and p.status = 0")
    Optional<Product> findByStatusRemoved(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productDetails WHERE p.id = :id and p.status = 1 ")
    Optional<Product> findByIdWithProductDetails(@Param("id") Long id);



    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.id = :id")
    Optional<Product> findByIdWithProductImages(@Param("id") Long id);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.productCategories pc " +
            "LEFT JOIN FETCH pc.category " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithCategories(@Param("id") Long id);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.productImages pi  where p.status = 1 and pi.status = 1 order by p.createdDate desc ")
    List<Product> findProductWithImage(Pageable pageable);

    @Query("SELECT p FROM Product p where p.status = 1 order by p.createdDate desc ")
    List<Product> findAllWithJPQL();

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.material " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.style " +
            "LEFT JOIN FETCH p.gender " +
            "LEFT JOIN FETCH p.sole " +
            "LEFT JOIN FETCH p.supplier " +
            "LEFT JOIN FETCH p.productCategories pc " +
            "LEFT JOIN FETCH pc.category " +
            "WHERE p.id IN :ids")
    List<Product> findByIdsWithCategories(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.material " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.style " +
            "LEFT JOIN FETCH p.gender " +
            "LEFT JOIN FETCH p.sole " +
            "LEFT JOIN FETCH p.supplier " +
            "LEFT JOIN FETCH p.productDetails pd " +
            "WHERE p.id IN :ids")
    List<Product> findByIdsWithDetails(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Product p WHERE p.status = 1 order by p.createdDate desc ")
    Page<Product> findAllWithJPQL(Pageable pageable);

    Optional<Product> findTop1ByProductCodeAndStatus(String productCode, int status);

    @Query("SELECT p FROM Product p WHERE p.id in :ids and p.status = 1 order by p.createdDate desc ")
    List<Product> findAllByIds(List<Long> ids);

    @Query("""
                select p from Product p
                join fetch p.material
                join fetch p.brand
                join fetch p.style
                join fetch p.gender
                join fetch p.sole
                join fetch p.supplier
                where coalesce(p.status, 1) = 1 
                  and p.brand.id = :brandId
                order by p.createdDate desc
            """)
    Page<Product> findAllByBrand(@Param("brandId") Long brandId, Pageable pageable);


    @Query("""
                select p from Product p
                join fetch p.material
                join fetch p.brand
                join fetch p.style
                join fetch p.gender
                join fetch p.sole
                join fetch p.supplier
                where coalesce(p.status,1)=1 and lower(p.brand.brandName) = lower(:brandName)
                order by p.createdDate desc
            """)
    Page<Product> findAllByBrandName(@Param("brandName") String brandName, Pageable pageable);

    @Query("""
    SELECT p
    FROM Product p
    WHERE p.status = 1
      AND EXISTS (
        SELECT 1
        FROM ProductCategory pc
        WHERE pc.product = p
          AND pc.category.id = :categoryId
          AND pc.status = 1
      )
    """)
    Page<Product> findAllByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);


    // Nếu bạn muốn lọc trực tiếp qua tên danh mục (vd: Sneaker)
    @Query("""
        select p
        from Product p
        join ProductCategory pc on pc.product = p
        join Category c on pc.category = c
        where lower(c.categoryName) = lower(:categoryName)
        """)
    Page<Product> findAllByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
}
