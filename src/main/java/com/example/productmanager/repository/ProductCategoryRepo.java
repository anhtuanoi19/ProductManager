package com.example.productmanager.repository;

import com.example.productmanager.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductCategoryRepo extends JpaRepository<ProductCategory, Long> {
    @Query("SELECT pc.category.id FROM ProductCategory pc WHERE pc.product.id = :productId")
    List<Long> findCategoryIdsByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE ProductCategory pc SET pc.status = '0' WHERE pc.product.id = :productId AND pc.category.id IN :categoryIds")
    void updateStatusByProductIdAndCategoryIds(@Param("productId") Long productId, @Param("categoryIds") Set<Long> categoryIds);

    Optional<ProductCategory> findByProductIdAndCategoryId(Long productId, Long categoryId);

    @Query("SELECT i FROM Images i WHERE i.product.id = :productId")
    List<ProductCategory> findByProductId(@Param("productId") Long productId);
}
