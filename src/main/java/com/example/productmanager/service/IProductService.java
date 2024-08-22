package com.example.productmanager.service;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.ProductDto;

public interface IProductService {
    ApiResponse<ProductDto> create(ProductRequest productRequest);
}
