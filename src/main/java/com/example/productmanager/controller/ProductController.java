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
import com.example.productmanager.service.IProductService;
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
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private IProductService service;

//    @PostMapping
//    ApiResponse<ProductDto> create(@RequestBody @Valid ProductRequest productRequest){
//        return service.create(productRequest);
//    }

    @GetMapping()
    public ApiResponse<Page<GetAllProduct>> getAllStudentsPageable(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        return service.getPagedProductDetails(pageable);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProducts() {
        try {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Adjust the page size as needed
            ApiResponse<Page<GetAllProduct>> response = service.getPagedProductDetails(pageable);

            // Extract the list of products from the response
            List<GetAllProduct> products = response.getResult().getContent();

            // Assuming your export method has been updated to accept List<GetAllProduct>
            ByteArrayInputStream in = service.exportProductsToExcel(products);

            // Convert ByteArrayInputStream to byte array
            byte[] bytes = in.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=products.xlsx");
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
            @RequestParam("product") String productUpdateJson,
            @RequestParam("files") MultipartFile[] files) {

        try {
            // Chuyển đổi JSON request thành ProductUpdate
            ObjectMapper objectMapper = new ObjectMapper();
            ProductUpdate productUpdate = objectMapper.readValue(productUpdateJson, ProductUpdate.class);

            // Chuyển MultipartFile[] thành List<MultipartFile> và đặt vào ProductUpdate
            productUpdate.setImages(Arrays.asList(files));

            // Cập nhật sản phẩm
            ApiResponse<ProductDto> response = service.updateProductAndCategories(productUpdate);

            return response;
        } catch (IOException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    @DeleteMapping("/xoa-mem/{id}")
    public ApiResponse<ProductDto> deleteMem(@PathVariable Long id){

        return service.deleteMem(id);
    }
    @GetMapping("/findById/{id}")
    public ApiResponse<ProductDto> findById(@PathVariable Long id){
        return service.findById(id);
    }

    @PostMapping("/create")
    public ApiResponse<ProductDto> createProduct(
            @RequestParam("product") String productRequestJson,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ProductRequest productRequest = objectMapper.readValue(productRequestJson, ProductRequest.class);

            if (files != null) {
                productRequest.setImages(Arrays.asList(files));
            }

            return service.create(productRequest);

        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
