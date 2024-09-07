package com.example.productmanager.repository;

import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select c from Category c order by c.modifiedDate DESC ")
    Page<Category> findAll(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.id IN :categoryIds")
    List<Category> findAllByIds(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT sc FROM ProductCategory sc WHERE sc.category.id = :categoryId")
    List<ProductCategory> findProductCategoryByCategoryId(@Param("categoryId") Long categoryId);
    boolean existsByCategoryCode(String ma);

    @Query(value = "select distinct c " +
            "from Category c " +
            "left join fetch c.images i " +
            "where c.status = :status " +
            "and (:name is null or c.name like %:name%) " +
            "and (:categoryCode is null or c.categoryCode like %:categoryCode%) " +
            "and (:startDate is null or c.createdDate >= :startDate) " +
            "and (:endDate is null or c.createdDate <= :endDate) " +
            "order by c.modifiedDate desc")
    Page<Category> getAll(@Param("name") String name,
                          @Param("status") String status,
                          @Param("categoryCode") String categoryCode,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate,
                          Pageable pageable);
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.images")
    List<Category> findAll();
    List<Category> findByStatus(String status);
    @Query("SELECT c FROM Category c LEFT JOIN c.images i WHERE c.id = :id AND i.status = 1")
    Category findCategoryWithActiveImages(@Param("id") Long id);

    @Query("SELECT sc FROM ProductCategory sc WHERE sc.category.id = :categoryId")
    List<ProductCategory> findProductCategoryByIdCategory(@Param("categoryId") Long categoryId);
}
