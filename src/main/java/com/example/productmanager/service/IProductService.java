package com.example.productmanager.service;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    ApiResponse<ProductDto> create(ProductRequest productRequest);
    ApiResponse<Page<GetAllProduct>> getPagedProductDetails(Pageable pageable);
}
