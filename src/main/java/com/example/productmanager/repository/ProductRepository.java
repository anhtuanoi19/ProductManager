package com.example.productmanager.repository;

import com.example.productmanager.entity.Product;
import com.example.productmanager.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p.id AS id, p.name AS productName, p.product_code AS productCode, " +
            "GROUP_CONCAT(c.name ORDER BY c.name SEPARATOR ', ') AS categoryName, " +
            "pc.status AS status, p.created_date AS createDate, p.modified_date AS modifiedDate, " +
            "p.quantity AS quantity, p.description AS description, p.price AS price " +
            "FROM product p " +
            "JOIN product_category pc ON p.id = pc.product_id " +
            "JOIN category c ON pc.category_id = c.id " +
            "GROUP BY p.id, p.name, p.product_code, pc.status, p.created_date, p.modified_date, " +
            "p.quantity, p.description, p.price " +
            "ORDER BY p.created_date DESC", // Thêm điều kiện sắp xếp theo ngày tạo
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM product p " +
                    "JOIN product_category pc ON p.id = pc.product_id " +
                    "JOIN category c ON pc.category_id = c.id",
            nativeQuery = true)
    Page<Object[]> findProductDetailsWithCategories(Pageable pageable);

    @Query("SELECT sc FROM ProductCategory sc WHERE sc.product.id = :productId")
    List<ProductCategory> findProductCategoryByIdProduct(@Param("productId") Long productId);

    @Query(value = "SELECT p.id AS id, p.name AS productName, p.product_code AS productCode, " +
            "GROUP_CONCAT(c.name ORDER BY c.name SEPARATOR ', ') AS categoryName, " +
            "pc.status AS status, p.created_date AS createDate, p.modified_date AS modifiedDate, " +
            "p.quantity AS quantity, p.description AS description, p.price AS price " +
            "FROM product p " +
            "JOIN product_category pc ON p.id = pc.product_id " +
            "JOIN category c ON pc.category_id = c.id " +
            "WHERE (:productName IS NULL OR p.name LIKE %:productName%) " + // Thêm điều kiện tìm kiếm theo tên
            "GROUP BY p.id, p.name, p.product_code, pc.status, p.created_date, p.modified_date, " +
            "p.quantity, p.description, p.price " +
            "ORDER BY p.created_date DESC", // Sắp xếp theo ngày tạo
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM product p " +
                    "JOIN product_category pc ON p.id = pc.product_id " +
                    "JOIN category c ON pc.category_id = c.id " +
                    "AND (:productName IS NULL OR p.name LIKE %:productName%)", // Điều kiện tìm kiếm trong count query
            nativeQuery = true)
    Page<Object[]> findProductDetailsWithCategories(@Param("productName") String productName, Pageable pageable);


}
