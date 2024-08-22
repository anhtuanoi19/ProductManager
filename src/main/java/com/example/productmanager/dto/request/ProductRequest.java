package com.example.productmanager.dto.request;

import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.ProductCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class ProductRequest {

    @NotEmpty(message = "product.name.notEmpty")
    @Size(max = 100, message = "product.name.size")
    private String name;

    @Size(max = 255, message = "product.description.size")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "product.price.decimalMin")
    private Double price;

    private String productCode;

    @Min(value = 0, message = "product.quantity.min")
    private Long quantity;

    @Pattern(regexp = "^(active|inactive)$", message = "product.status.pattern")
    private String status;

    private Date createdDate;

    private Date modifiedDate;

    private String createdBy;

    private String modifiedBy;

    @Valid
    private List<@Valid CategoryDto> categories;

    private List<Images> images;
}
