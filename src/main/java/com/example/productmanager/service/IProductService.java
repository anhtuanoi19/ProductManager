package com.example.productmanager.service;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface IProductService {
    ApiResponse<ProductDto> create(String data, List<MultipartFile> files) throws JsonProcessingException;
    ApiResponse<Page<GetAllProduct>> getPagedProductDetails(Pageable pageable, String name, String productCode, String status, LocalDate startDate, LocalDate endDate);
    ByteArrayInputStream exportProductsToExcel(List<GetAllProduct> products) throws IOException;
    List<Product> findAllProducts();
    ApiResponse<ProductDto> updateProductAndCategories(String data, List<MultipartFile> files) throws JsonProcessingException;
    ApiResponse<ProductDto> findById(Long id);
    ApiResponse<Page<GetAllProduct>> getPagedProductDetails(String productName, Pageable pageable);
    ApiResponse<Page<GetAllProduct>> getPagedProductDetailsCategory(String categoryName, Pageable pageable);
    ApiResponse<ProductDto> deleteMem(Long id);

}
