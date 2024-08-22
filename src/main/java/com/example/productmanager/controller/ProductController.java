package com.example.productmanager.controller;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private IProductService service;

    @PostMapping
    ApiResponse<ProductDto> create(@RequestBody @Valid ProductRequest productRequest){
        return service.create(productRequest);
    }

    @GetMapping()
    public ApiResponse<Page<GetAllProduct>> getAllStudentsPageable(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        return service.getPagedProductDetails(pageable);
    }
}
