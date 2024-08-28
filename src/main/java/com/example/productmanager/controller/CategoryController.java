package com.example.productmanager.controller;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private ICategoryService service;

    @PostMapping
    ApiResponse<CategoryDto> create(@RequestBody @Valid CategoryRequest categoryRequest){
        return service.create(categoryRequest);
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

    @PutMapping("update/{id}")
    public ApiResponse<CategoryDto> update(@RequestBody @Valid CategoryUpdate categoryUpdate,@PathVariable Long id){
        return service.update(id, categoryUpdate);
    }
}
