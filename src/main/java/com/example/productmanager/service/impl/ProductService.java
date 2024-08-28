package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.Images;
import com.example.productmanager.entity.Product;
import com.example.productmanager.entity.ProductCategory;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.mapper.CategoryMapper;
import com.example.productmanager.mapper.ProductMapper;
import com.example.productmanager.repository.CategoryRepository;
import com.example.productmanager.repository.ImagesRepository;
import com.example.productmanager.repository.ProductCategoryRepo;
import com.example.productmanager.repository.ProductRepository;
import com.example.productmanager.service.IProductService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public ByteArrayInputStream exportProductsToExcel(List<Product> products) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Products");

            // Tạo header
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Description", "Price", "Product Code", "Quantity", "Status", "Created Date", "Modified Date"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Thêm dữ liệu sản phẩm
            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription());
                row.createCell(3).setCellValue(product.getPrice());
                row.createCell(4).setCellValue(product.getProductCode());
                row.createCell(5).setCellValue(product.getQuantity());
                row.createCell(6).setCellValue(product.getStatus());
                row.createCell(7).setCellValue(product.getCreatedDate().toString());
                row.createCell(8).setCellValue(product.getModifiedDate().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public ApiResponse<ProductDto> updateProductAndCategories(ProductUpdate dto) {
        Locale locale = LocaleContextHolder.getLocale();

        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_EXISTS));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus());
        product.setCreatedDate(dto.getCreatedDate());
        product.setModifiedDate(dto.getModifiedDate());
        product.setCreatedBy(dto.getCreatedBy());
        product.setModifiedBy(dto.getModifiedBy());

        if (product.getStatus().equals("0")) {
            productRepository.save(product);

            List<ProductCategory> productCategories = productCategoryRepo.findByProductId(product.getId());
            productCategories.forEach(pc -> pc.setStatus("0"));
            productCategoryRepo.saveAll(productCategories);
        } else {
            productRepository.save(product);
        }

        // Xử lý cập nhật danh mục (Category)
        List<Long> currentCategoryIds = productCategoryRepo.findCategoryIdsByProductId(product.getId());

        Set<Long> newCategoryIds = new HashSet<>(dto.getCategoryIds());

        // Xử lý các danh mục mới
        if (dto.getListCategory() != null && !dto.getListCategory().isEmpty()) {
            // Sử dụng CategoryMapper để ánh xạ từ CategoryDto sang Category
            List<Category> categoriesToSave = dto.getListCategory().stream()
                    .filter(categoryDto -> categoryDto.getId() == null) // Chỉ xử lý những category chưa có ID (mới)
                    .map(categoryDto -> {
                        Category newCategory = CategoryMapper.INTANCE.toEntity(categoryDto); // Ánh xạ sang thực thể Category

                        // Thiết lập các giá trị mặc định
                        newCategory.setCreatedDate(new Date()); // Ngày tạo mới
                        newCategory.setModifiedDate(new Date()); // Ngày sửa mới
                        newCategory.setModifiedBy("admin"); // Người sửa là 'admin'
                        newCategory.setCreatedBy("admin"); // Người sửa là 'admin'

                        return newCategory;
                    })
                    .collect(Collectors.toList());

            List<Category> savedCategories = categoryRepository.saveAll(categoriesToSave);

            savedCategories.forEach(category -> newCategoryIds.add(category.getId()));
        }

        Set<Long> categoriesToDeactivate = currentCategoryIds.stream()
                .filter(id -> !newCategoryIds.contains(id))
                .collect(Collectors.toSet());

        if (!categoriesToDeactivate.isEmpty()) {
            productCategoryRepo.updateStatusByProductIdAndCategoryIds(product.getId(), categoriesToDeactivate);
        }

        List<Category> categories = categoryRepository.findAllById(new ArrayList<>(newCategoryIds));

        for (Category category : categories) {
            if (category.getStatus().equals("0")) {
                throw new AppException(ErrorCode.CATEGORY_EXISTS);
            }
        }

        Set<ProductCategory> newProductCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = productCategoryRepo.findByProductIdAndCategoryId(product.getId(), category.getId())
                            .orElseGet(() -> new ProductCategory());
                    productCategory.setProduct(product);
                    productCategory.setCategory(category);
                    productCategory.setStatus(dto.getStatus());
                    productCategory.setModifiedBy("admin");
                    productCategory.setModifiedDate(new Date());
                    productCategory.setCreatedBy("admin");
                    productCategory.setCreatedDate(new Date());
                    productCategory.setStatus(dto.getStatus());
                    return productCategory;
                })
                .collect(Collectors.toSet());

        productCategoryRepo.saveAll(newProductCategories);

        // Xử lý cập nhật hình ảnh (Images)
        if (dto.getListImages() != null && !dto.getListImages().isEmpty()) {
            List<Images> imagesToSave = dto.getListImages().stream()
                    .map(imageDto -> {
                        Images image = new Images();
                        image.setUrl(imageDto.getUrl());
                        image.setProduct(product);
                        return image;
                    })
                    .collect(Collectors.toList());

            imagesRepository.saveAll(imagesToSave);
        }

        // Tạo phản hồi API
        ProductDto updatedProductDto = ProductMapper.INTANCE.toDto(product);
        ApiResponse<ProductDto> response = new ApiResponse<>();
        response.setResult(updatedProductDto);
        response.setMessage(messageSource.getMessage("success.update", null, locale));

        return response;
    }

}
