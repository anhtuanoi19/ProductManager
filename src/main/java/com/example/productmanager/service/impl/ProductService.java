package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.Product;
import com.example.productmanager.entity.ProductCategory;
import com.example.productmanager.mapper.CategoryMapper;
import com.example.productmanager.mapper.ProductMapper;
import com.example.productmanager.repository.CategoryRepository;
import com.example.productmanager.repository.ImagesRepository;
import com.example.productmanager.repository.ProductCategoryRepo;
import com.example.productmanager.repository.ProductRepository;
import com.example.productmanager.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepo productCategoryRepo;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private MessageSource messageSource;

    @Transactional
    @Override
    public ApiResponse<ProductDto> create(ProductRequest productRequest) {
        Locale locale = LocaleContextHolder.getLocale();

        // Tạo đối tượng Product từ ProductRequest
        Product product = ProductMapper.INTANCE.toEntityProduct(productRequest);
        product.setCreatedDate(new Date());
        product.setModifiedDate(new Date());

        // Tạo danh sách Images từ ProductRequest
        List<Images> images = productRequest.getImages().stream()
                .map(imageDto -> {
                    Images image = new Images();
                    image.setUrl(imageDto.getUrl());
                    image.setProduct(product); // 'product' here is effectively final
                    return image;
                }).collect(Collectors.toList());

        // Thiết lập danh sách Images cho Product
        product.setImages(new HashSet<>(images));

        // Lưu Product trước để có ID của Product cho các Images
        productRepository.save(product);

        // Xử lý danh sách các thể loại
        List<Long> categoryIds = productRequest.getCategories().stream()
                .map(CategoryDto::getId)
                .collect(Collectors.toList());

        List<Category> existingCategories = categoryRepository.findAllByIds(categoryIds);

        Set<Long> existingCategoryIds = existingCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        List<Category> newCategories = productRequest.getCategories().stream()
                .filter(categoryDto -> categoryDto.getId() == null || !existingCategoryIds.contains(categoryDto.getId()))
                .map(categoryDto -> {
                    Category newCategory = CategoryMapper.INTANCE.toEntity(categoryDto);
                    newCategory.setCreatedDate(new Date());
                    newCategory.setModifiedDate(new Date());
                    return categoryRepository.save(newCategory);
                }).collect(Collectors.toList());

        List<Category> allCategories = new ArrayList<>(existingCategories);
        allCategories.addAll(newCategories);

        List<ProductCategory> productCategories = allCategories.stream()
                .map(category -> {
                    ProductCategory productCategory = new ProductCategory();
                    productCategory.setProduct(product);
                    productCategory.setCategory(category);
                    productCategory.setCreatedDate(new Date());
                    productCategory.setModifiedDate(new Date());
                    productCategory.setCreatedBy(product.getCreatedBy());
                    productCategory.setModifiedBy(product.getModifiedBy());
                    productCategory.setStatus("1");
                    return productCategory;
                }).collect(Collectors.toList());


        // Lưu tất cả các ProductCategory
        productCategoryRepo.saveAll(productCategories);

        // Chuyển đổi Product thành ProductDto và set danh sách categories
        ProductDto productDto = ProductMapper.INTANCE.toDto(product);
        List<CategoryDto> categoryDtos = productCategories.stream()
                .map(pc -> CategoryMapper.INTANCE.toDto(pc.getCategory()))
                .collect(Collectors.toList());
        productDto.setCategories(categoryDtos);
        // Tạo ApiResponse và trả kết quả
        ApiResponse<ProductDto> apiResponse = new ApiResponse<>();
        apiResponse.setResult(productDto);
        apiResponse.setMessage(messageSource.getMessage("success.product.create", null, locale));

        return apiResponse;
    }


    @Transactional
    public ApiResponse<Page<GetAllProduct>> getPagedProductDetails(Pageable pageable) {
        Locale locale = LocaleContextHolder.getLocale();

        // Gọi phương thức repository để lấy kết quả phân trang
        Page<Object[]> result = productRepository.findProductDetailsWithCategories(pageable);

        // Chuyển đổi kết quả từ Object[] thành GetAllProduct
        List<GetAllProduct> products = result.getContent().stream().map(record -> {
            GetAllProduct product = new GetAllProduct();
            product.setProductName((String) record[0]);
            product.setProductCode((String) record[1]);
            product.setCategoryName((String) record[2]);
            product.setStatus((String) record[3]);
            product.setCreateDate((Date) record[4]);
            product.setModifiedDate((Date) record[5]);
            product.setQuantity(((Number) record[6]).longValue());
            product.setDescription((String) record[7]);
            product.setPrice((Double) record[8]);
            return product;
        }).collect(Collectors.toList());

        // Tạo ApiResponse và set kết quả
        ApiResponse<Page<GetAllProduct>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(new PageImpl<>(products, pageable, result.getTotalElements()));
        apiResponse.setMessage(messageSource.getMessage("success.products.getAll", null, locale));

        return apiResponse;
    }

}
