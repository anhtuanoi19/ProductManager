package com.example.productmanager.dto.request;

import com.example.productmanager.dto.response.CategoryDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class ProductUpdate {
    private Long id;

    @NotBlank(message = "NotBlank.name")
    @Size(min = 3, max = 100, message = "Size.name")
    private String name;

    @Size(max = 255, message = "Size.description")
    private String description;

    @Pattern(regexp = "^(1|0)$", message = "Pattern.status")
    private String status;

    @PastOrPresent(message = "PastOrPresent.createdDate")
    private Date createdDate;

    @PastOrPresent(message = "PastOrPresent.modifiedDate")
    private Date modifiedDate;

    @NotBlank(message = "NotBlank.createdBy")
    @Size(max = 100, message = "Size.createdBy")
    private String createdBy;

    @Size(max = 100, message = "Size.modifiedBy")
    private String modifiedBy;

    private List<CategoryDto> listCategory;
    private Set<Long> categoryIds;

    private List<ImageDto> listImages;  // Thêm phần danh sách hình ảnh
}

