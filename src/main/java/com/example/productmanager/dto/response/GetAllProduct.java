package com.example.productmanager.dto.response;

import lombok.Data;

import java.util.Date;
@Data
public class GetAllProduct {
    private Long id;
    private String productName;
    private String productCode;
    private String categoryName;
    private String status;
    private Date createDate;
    private Date modifiedDate;
    private Long quantity;
    private String description;
    private Double price;

}
