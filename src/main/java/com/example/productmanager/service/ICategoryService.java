package com.example.productmanager.service;

import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ICategoryService {
    ApiResponse<Page<CategoryDto>> getAll(Pageable pageable);
    ApiResponse<List<CategoryDto>> findAll();
   List<CategoryDto> findAllE();
    ApiResponse<CategoryDto> createCategory(String data, List<MultipartFile> images) throws JsonProcessingException;
    ApiResponse<CategoryDto> findById(Long id);
    ApiResponse<CategoryDto> update(Long id, String data, List<MultipartFile> images) throws JsonProcessingException;
    ApiResponse<Boolean> deleteMem(Long categoryId);
    ApiResponse<CategoryDto> open(Long id);
    ApiResponse<Page<CategoryDto>> findByName(String name, String status, String categoryCode, LocalDate startDate, LocalDate endDate, int page, int size);
    ByteArrayInputStream exportCategoriesToExcel(List<CategoryDto> categories) throws IOException;
}
