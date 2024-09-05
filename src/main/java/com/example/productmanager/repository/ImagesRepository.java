package com.example.productmanager.repository;

import com.example.productmanager.entity.Images;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImagesRepository extends JpaRepository<Images, Long> {
    @Query("SELECT i FROM Images i WHERE i.product.id = :productId")
    List<Images> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM Images i WHERE i.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);

}
