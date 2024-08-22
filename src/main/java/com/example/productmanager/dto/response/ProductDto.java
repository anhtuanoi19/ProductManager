package com.example.productmanager.dto.response;

import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class ProductDto {
    @NotEmpty(message = "product.name.notEmpty")
    @Size(max = 100, message = "product.name.size")
    private String name;

    @Size(max = 255, message = "product.description.size")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "product.price.decimalMin")
    private Double price;

    @Pattern(regexp = "^[A-Za-z0-9]3,10$", message = "product.productCode.pattern")
    private String productCode;

    @Min(value = 0, message = "product.quantity.min")
    private Long quantity;

    @Pattern(regexp = "^(active|inactive)$", message = "product.status.pattern")
    private String status;

    private Date createdDate;

    private Date modifiedDate;

    private String createdBy;

    private String modifiedBy;

    private List<ProductCategory> productCategories;

    private List<Images> images;
}
