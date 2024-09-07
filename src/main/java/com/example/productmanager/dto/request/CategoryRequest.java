package com.example.productmanager.dto.request;

import com.example.productmanager.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
@Data
public class CategoryRequest {
    private Long id;
    @NotBlank(message = "NotBlank.name")
    @Size(min = 3, max = 100, message = "Size.name")
    private String name;

    @Size(max = 255, message = "Size.description")
    private String description;

    @NotBlank(message = "NotBlank.categoryCode")
    @Size(min = 3,max = 50, message = "Size.categoryCode")
    private String categoryCode;

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

    private List<MultipartFile> images;

}
