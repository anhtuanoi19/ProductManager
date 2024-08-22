package com.example.productmanager.dto.response;

import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.Product;
import lombok.Data;

import java.util.Date;
@Data
public class ProductCategoryDto {
    private Long id;
    private Product product;
    private Category category;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;
}
