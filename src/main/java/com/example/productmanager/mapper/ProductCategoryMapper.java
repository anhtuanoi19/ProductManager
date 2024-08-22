package com.example.productmanager.mapper;

import com.example.productmanager.dto.response.ProductCategoryDto;
import com.example.productmanager.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {CategoryMapper.class, ProductMapper.class})
public interface ProductCategoryMapper {
    ProductCategoryMapper INTANCE = Mappers.getMapper(ProductCategoryMapper.class);

    ProductCategoryDto toDto(ProductCategory productCategory);
    ProductCategory toEntity(ProductCategoryDto productCategoryDto);
}
