package com.example.productmanager.dto.response;

import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.ImagesCategory;
import com.example.productmanager.entity.ProductCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;
@Data
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private String categoryCode;
    private String status;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private List<ImagesCategory> images;
}
