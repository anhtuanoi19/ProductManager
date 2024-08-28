package com.example.productmanager.mapper;

import com.example.productmanager.dto.request.ImageDto;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ImageMapper {
    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    Product toEntity(ImageDto imageDto);
    ImageDto toDto(Images images);
}
