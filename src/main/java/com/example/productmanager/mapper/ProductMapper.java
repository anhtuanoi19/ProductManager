package com.example.productmanager.mapper;

import com.example.productmanager.dto.request.ImageDto;
import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.Product;
import com.example.productmanager.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ProductMapper {

    ProductMapper INTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "categories", expression = "java(mapCategories(product.getProductCategories()))")
    ProductDto toDto(Product product);

    Product toEntityProduct(ProductRequest productRequest);

    Product toEntityUpdate(ProductUpdate productUpdate);


    default List<CategoryDto> mapCategories(Set<ProductCategory> productCategories) {
        if (productCategories == null) {
            return Collections.emptyList();
        }
        return productCategories.stream()
                .map(productCategory -> CategoryMapper.INTANCE.toDto(productCategory.getCategory()))
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    void updateProductFromDto(ProductUpdate dto, @MappingTarget Product product);



}
