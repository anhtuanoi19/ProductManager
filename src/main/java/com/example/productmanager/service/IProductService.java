package com.example.productmanager.service;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface IProductService {
    ApiResponse<ProductDto> create(ProductRequest productRequest);
    ApiResponse<Page<GetAllProduct>> getPagedProductDetails(Pageable pageable);
    ByteArrayInputStream exportProductsToExcel(List<Product> products) throws IOException;
    List<Product> findAllProducts();

    ApiResponse<ProductDto> updateProductAndCategories(ProductUpdate dto);
}
