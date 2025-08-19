package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.FavoriteProductResponse;
import com.example.duantotnghiep.model.FavoriteProduct;
import com.example.duantotnghiep.model.ProductRatingView;
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
        select 
            fp.product.id as productId,
            avg(cast(fp.rate as double)) as avgRating,
            count(fp.id) as totalReviews,
            sum(case when fp.rate = 1 then 1 else 0 end) as star1,
            sum(case when fp.rate = 2 then 1 else 0 end) as star2,
            sum(case when fp.rate = 3 then 1 else 0 end) as star3,
            sum(case when fp.rate = 4 then 1 else 0 end) as star4,
            sum(case when fp.rate = 5 then 1 else 0 end) as star5
        from FavoriteProduct fp
        where fp.status = 1 
          and fp.rate is not null
          and fp.product.id = :productId
        group by fp.product.id
    """)
    Optional<ProductRatingView> findRatingByProductId(@Param("productId") Long productId);

    // Nhiều sản phẩm (bulk) -> trả list, mỗi phần tử là 1 productId
    @Query("""
        select 
            fp.product.id as productId,
            avg(cast(fp.rate as double)) as avgRating,
            count(fp.id) as totalReviews,
            sum(case when fp.rate = 1 then 1 else 0 end) as star1,
            sum(case when fp.rate = 2 then 1 else 0 end) as star2,
            sum(case when fp.rate = 3 then 1 else 0 end) as star3,
            sum(case when fp.rate = 4 then 1 else 0 end) as star4,
            sum(case when fp.rate = 5 then 1 else 0 end) as star5
        from FavoriteProduct fp
        where fp.status = 1
          and fp.rate is not null
          and fp.product.id in :productIds
        group by fp.product.id
    """)
    List<ProductRatingView> findRatingsByProductIds(@Param("productIds") List<Long> productIds);
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
