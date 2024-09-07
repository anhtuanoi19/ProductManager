package com.example.productmanager.mapper;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CategoryMapper {
    CategoryMapper INTANCE = Mappers.getMapper(CategoryMapper.class);
    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto categoryDto);
    Category toRequestEntity(CategoryRequest categoryRequest);
    Category toEntityUpdate(CategoryUpdate categoryUpdate);

    List<CategoryDto> toListDto(List<Category> list);
    @Mapping(target = "id", ignore = true)  // Bỏ qua ID để không ghi đè
    void updateCategoryFromDto(CategoryUpdate dto, @MappingTarget Category entity);
}
