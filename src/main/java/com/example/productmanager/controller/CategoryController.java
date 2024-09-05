package com.example.productmanager.controller;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.service.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private ICategoryService service;

    @PostMapping("/create")
    public ApiResponse<CategoryDto> createCategory(
            @RequestParam("category") String categoryRequestJson,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CategoryRequest categoryRequest = objectMapper.readValue(categoryRequestJson, CategoryRequest.class);

            if (files != null) {
                categoryRequest.setImages(Arrays.asList(files));
            }

            return service.createCategory(categoryRequest);

        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @DeleteMapping("/xoa-mem/{id}")
    public ApiResponse<Boolean> deleteMem(@PathVariable Long id) {
        return service.deleteMem(id);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCategories() {
        try {
            // Fetch categories from the service, ensure you call the right method
            List<CategoryDto> categories = service.findAllE();

            // Generate Excel file
            ByteArrayInputStream in = service.exportCategoriesToExcel(categories);

            // Convert ByteArrayInputStream to byte array
            byte[] bytes = in.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=categories.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(bytes);

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while generating Excel file".getBytes());
        }
    }

    @GetMapping("/search")
    public ApiResponse<Page<CategoryDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return service.findByName(name, page, size);
    }


    @GetMapping("/findById/{id}")
    ApiResponse<CategoryDto> findById(@PathVariable Long id){
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
    public ApiResponse<List<CategoryDto>> findAll(){
        return service.findAll();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestParam("category") String categoryUpdateJson,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CategoryUpdate categoryUpdate = objectMapper.readValue(categoryUpdateJson, CategoryUpdate.class);

            if (files != null) {
                // Assuming CategoryUpdate has a method to set images
                categoryUpdate.setImages(Arrays.asList(files));
            }

            return service.update(id, categoryUpdate);

        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


}
