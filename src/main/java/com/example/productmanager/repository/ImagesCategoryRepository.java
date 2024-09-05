package com.example.productmanager.repository;

import com.example.productmanager.entity.ImagesCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagesCategoryRepository extends JpaRepository<ImagesCategory, Long> {
    List<ImagesCategory> findByCategoryId(Long categoryId);
}
