package com.example.productmanager.controller;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Product;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.repository.ProductRepository;
import com.example.productmanager.service.IProductService;
import com.example.productmanager.service.impl.ExcelExportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private IProductService service;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ExcelExportService excelExportService;

//    @PostMapping
//    ApiResponse<ProductDto> create(@RequestBody @Valid ProductRequest productRequest){
//        return service.create(productRequest);
//    }

    @GetMapping("/search")
    public ApiResponse<Page<GetAllProduct>> getAllProductsPageable(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "productCode", required = false) String productCode,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, size);
        return service.getPagedProductDetails(pageable, name, productCode, status, startDate, endDate);
    }


    @GetMapping("/findByName")
    public ResponseEntity<ApiResponse<Page<GetAllProduct>>> getProducts(
            @RequestParam(value = "productName", required = false) String productName,
            Pageable pageable) {

        ApiResponse<Page<GetAllProduct>> response = service.getPagedProductDetails(productName, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findByCategoryName")
    public ResponseEntity<ApiResponse<Page<GetAllProduct>>> getProductsByCategory(
            @RequestParam(value = "categoryName", required = false) String categoryName,
            Pageable pageable) {

        ApiResponse<Page<GetAllProduct>> response = service.getPagedProductDetails(categoryName, pageable);
        return ResponseEntity.ok(response);
    }



    @PutMapping("/update")
    public ApiResponse<ProductDto> updateProduct(
            @RequestParam("product") String category,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws JsonProcessingException {
        ApiResponse<ProductDto> response = service.updateProductAndCategories(category, images);
        return response;
    }
    @DeleteMapping("/xoa-mem/{id}")
    public ApiResponse<ProductDto> deleteMem(@PathVariable Long id){

        return service.deleteMem(id);
    }
    @GetMapping("/findById/{id}")
    public ApiResponse<ProductDto> findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/create")
    public ApiResponse<ProductDto> createCategory(
            @RequestParam("data") String data,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws JsonProcessingException, JsonProcessingException {
        ApiResponse<ProductDto> response = service.create(data, images);
        return response;
    }


        @GetMapping("/export")
    public ResponseEntity<byte[]> downloadProductsExcel() throws IOException {
        List<Product> products = productRepository.getAllExport();

        ByteArrayInputStream in = excelExportService.exportProductsToExcel(products);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=products.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(in.readAllBytes());
    }
}
