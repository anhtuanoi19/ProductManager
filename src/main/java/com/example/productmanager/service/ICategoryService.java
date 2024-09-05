package com.example.productmanager.service;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface ICategoryService {
    ApiResponse<Page<CategoryDto>> getAll(Pageable pageable);
    ApiResponse<List<CategoryDto>> findAll();
   List<CategoryDto> findAllE();
    ApiResponse<CategoryDto> createCategory(CategoryRequest dto);
    ApiResponse<CategoryDto> findById(Long id);
    ApiResponse<CategoryDto> update(Long id, CategoryUpdate request);
    ApiResponse<Boolean> deleteMem(Long categoryId);
    ApiResponse<CategoryDto> open(Long id);
    ApiResponse<Page<CategoryDto>> findByName(String title, int page, int size);
    ByteArrayInputStream exportCategoriesToExcel(List<CategoryDto> categories) throws IOException;
}
