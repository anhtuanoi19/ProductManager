package com.example.productmanager.dto.request;

import com.example.productmanager.entity.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class ImageDto {
    private Long id;
    private String url;
}
