package com.example.productmanager.controller;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ConstraintViolationExceptionCustom;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.repository.CategoryRepository;
import com.example.productmanager.service.ICategoryService;
import com.example.productmanager.service.impl.CategoryService;
import com.example.productmanager.service.impl.ExcelExportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private ICategoryService service;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private CategoryRepository categoryRepository;


    @PostMapping("/create")
    public ApiResponse<CategoryDto> createCategory(
            @RequestParam("data") String data,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws JsonProcessingException, JsonProcessingException {
        ApiResponse<CategoryDto> response = service.createCategory(data, images);
        return response;
    }

    @DeleteMapping("/xoa-mem/{id}")
    public ApiResponse<Boolean> deleteMem(@PathVariable Long id) {
        return service.deleteMem(id);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> downloadCategoriesExcel() throws IOException {
        List<Category> categories = categoryRepository.findAll();

        ByteArrayInputStream in = excelExportService.exportCategoriesToExcel(categories);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=categories.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(in.readAllBytes());
    }

    @GetMapping("/search")
    public ApiResponse<Page<CategoryDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categorycode,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return service.findByName(name, status, categorycode, endDate, startDate, page, size);
    }
    @GetMapping("/findByIdProduct/{id}")
    ApiResponse<List<CategoryDto>> findCategoriesByProductIdAndStatus(@PathVariable Long id){
        return service.findCategoriesByProductIdAndStatus(id);
    }

    @GetMapping("/findById/{id}")
    ApiResponse<CategoryDto> findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping()
    public ApiResponse<Page<CategoryDto>> getAllStudentsPageable(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        return service.getAll(pageable);
    }

    @GetMapping("/findAll")
    public ApiResponse<List<CategoryDto>> findAll() {
        return service.findAll();
    }


    @PutMapping("/update/{id}")
    public ApiResponse<CategoryDto> updateCategory(
            @PathVariable("id") Long id,
            @RequestParam("category") String category,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws JsonProcessingException {
        ApiResponse<CategoryDto> response = service.update(id, category, images);
        return response;
    }


}
