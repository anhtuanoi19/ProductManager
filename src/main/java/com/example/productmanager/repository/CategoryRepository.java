package com.example.productmanager.repository;

import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select c from Category c order by c.modifiedDate DESC ")
    Page<Category> findAll(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.id IN :categoryIds")
    List<Category> findAllByIds(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT sc FROM ProductCategory sc WHERE sc.category.id = :categoryId")
    List<ProductCategory> findProductCategoryByCategoryId(@Param("categoryId") Long categoryId);
}
