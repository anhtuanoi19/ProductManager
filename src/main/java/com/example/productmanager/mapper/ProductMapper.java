package com.example.productmanager.mapper;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {

    ProductMapper INTANCE = Mappers.getMapper(ProductMapper.class);

    Product toEntity(ProductDto productDto);
    ProductDto toDto(Product product);
    ProductRequest toProductRequest(Product product);
    Product toEntityProduct(ProductRequest productRequest);
}
