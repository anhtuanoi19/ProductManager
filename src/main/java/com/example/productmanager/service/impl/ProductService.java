package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.service.IProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService implements IProductService {

    @Transactional
    @Override
    public ApiResponse<ProductDto> create(ProductRequest productRequest) {
        return null;
    }
}
