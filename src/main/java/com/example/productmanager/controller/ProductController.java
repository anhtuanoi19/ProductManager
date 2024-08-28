package com.example.productmanager.controller;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Product;
import com.example.productmanager.service.IProductService;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProducts() {
        try {
            List<Product> products = service.findAllProducts(); // Phương thức để lấy danh sách sản phẩm
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

    @PutMapping("/update")
    public ApiResponse<ProductDto> update(@RequestBody @Valid ProductUpdate productUpdate){
        return service.updateProductAndCategories(productUpdate);
    }
}
