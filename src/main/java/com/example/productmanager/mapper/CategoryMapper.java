package com.example.productmanager.mapper;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryMapper {
    CategoryMapper INTANCE = Mappers.getMapper(CategoryMapper.class);
    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto categoryDto);
    Category toRequestEntity(CategoryRequest categoryRequest);
    CategoryRequest toRequest(Category category);
}
