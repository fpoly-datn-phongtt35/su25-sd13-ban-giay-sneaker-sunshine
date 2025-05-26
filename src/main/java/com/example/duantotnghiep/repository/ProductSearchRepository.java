package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.mapper.PaginationMapper;
import com.example.duantotnghiep.mapper.ProductMapper;
import com.example.duantotnghiep.model.Category;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ProductSearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PaginationMapper paginationMapper;

    public PaginationDTO<ProductSearchResponse> searchProducts(ProductSearchRequest request, Pageable pageable){
        String baseSql = "FROM Product p WHERE 1=1";
        StringBuilder whereClause = new StringBuilder(" And p.status = :status");
        Map<String,Object> params = new HashMap<>();
        params.put("status",1);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            whereClause.append(" AND (LOWER(p.productName) LIKE LOWER(:keyword) OR LOWER(p.productCode) LIKE LOWER(:keyword))");
            params.put("keyword", "%" + request.getKeyword() + "%");
        }

        if (request.getBrandId() != null) {
            whereClause.append(" AND (p.brand.id IS NULL OR p.brand.id = :brandId)");
            params.put("brandId", request.getBrandId());
        }

        if (request.getGenderId() != null) {
            whereClause.append(" AND (p.gender.id IS NULL OR p.gender.id = :genderId)");
            params.put("genderId", request.getGenderId());
        }

        if (request.getStyleId() != null) {
            whereClause.append(" AND (p.style.id IS NULL OR p.style.id = :styleId)");
            params.put("styleId", request.getStyleId());
        }

        if (request.getSoleId() != null) {
            whereClause.append(" AND (p.sole.id IS NULL OR p.sole.id = :soleId)");
            params.put("soleId", request.getSoleId());
        }

        if (request.getMaterialId() != null) {
            whereClause.append(" AND (p.material.id IS NULL OR p.material.id = :materialId)");
            params.put("materialId", request.getMaterialId());
        }

        if (request.getCreatedFrom() != null) {
            whereClause.append(" AND (p.createdDate IS NULL OR p.createdDate >= :createdFrom)");
            params.put("createdFrom", request.getCreatedFrom());
        }

        if (request.getCreatedTo() != null) {
            whereClause.append(" AND (p.createdDate IS NULL OR p.createdDate <= :createdTo)");
            params.put("createdTo", request.getCreatedTo());
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            whereClause.append(" AND EXISTS (SELECT 1 FROM ProductCategory pc WHERE pc.product = p AND (pc.category.id IS NULL OR pc.category.id IN :categoryIds) AND pc.status = 1)");
            params.put("categoryIds", request.getCategoryIds());
        }

        String dataSql = "SELECT p " + baseSql + whereClause + " ORDER BY p.id DESC";
        TypedQuery<Product> query = entityManager.createQuery(dataSql, Product.class);

        params.forEach(query::setParameter);
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        List<Product> products = query.getResultList();

        if (!products.isEmpty()) {
            Set<Long> productIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());

            // Truy vấn ProductCategory với JOIN FETCH để lấy Category
            String productCategorySql = "SELECT pc FROM ProductCategory pc " +
                    "LEFT JOIN FETCH pc.category c " +
                    "WHERE pc.product.id IN :productIds AND pc.status = 1";
            TypedQuery<ProductCategory> productCategoryQuery = entityManager.createQuery(productCategorySql, ProductCategory.class);
            productCategoryQuery.setParameter("productIds", productIds);
            List<ProductCategory> productCategories;
            try {
                productCategories = productCategoryQuery.getResultList();
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi lấy ProductCategory: " + e.getMessage(), e);
            }

            Map<Long, List<ProductCategory>> productCategoriesMap = productCategories.stream()
                    .collect(Collectors.groupingBy(pc -> pc.getProduct().getId()));

            products.forEach(p -> p.setProductCategories(productCategoriesMap.getOrDefault(p.getId(), new ArrayList<>())));
        }

        String countSql = "SELECT COUNT(DISTINCT p) " + baseSql + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();


        // Chuyển đổi sang DTO
        List<ProductSearchResponse> productResponses = products.stream()
                .map(productMapper::toResponseSearch)
                .collect(Collectors.toList());

        // Tạo Page để sử dụng PaginationMapper
        Page<ProductSearchResponse> page = new PageImpl<>(productResponses, pageable, total);

        return paginationMapper.toPaginationDTO(page);
    }

    public List<ProductSearchResponse> searchProductWithoutPaging(ProductSearchRequest request) {
        Pageable pageable = Pageable.unpaged();
        List<ProductSearchResponse> result = searchProducts(request, pageable).getData();

        if (!result.isEmpty()) {
            System.out.println("Đã tìm thấy " + result.size() + " sản phẩm.");
        }

        return result;
    }

}
