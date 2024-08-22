package com.example.productmanager.dto.response;

import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String productCode;
    private Long quantity;
    private String status;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private List<CategoryDto> categories;
    private List<Images> images;
}
