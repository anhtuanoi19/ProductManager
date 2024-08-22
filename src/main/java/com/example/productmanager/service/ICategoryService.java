package com.example.productmanager.service;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {
    ApiResponse<Page<CategoryDto>> getAll(Pageable pageable);
    ApiResponse<CategoryDto> create(CategoryRequest categoryRequest);
}
