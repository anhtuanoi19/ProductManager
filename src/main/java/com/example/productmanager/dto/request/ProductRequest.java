package com.example.productmanager.dto.request;

import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.ProductCategory;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class ProductRequest {
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
    private List<ProductCategory> productCategories;
    private List<Images> images;
}
