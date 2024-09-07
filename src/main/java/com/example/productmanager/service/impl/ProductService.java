package com.example.productmanager.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
    public ApiResponse<ProductDto> create(ProductRequest productRequest) {
        Locale locale = LocaleContextHolder.getLocale();
        Random random = new Random();

        // Set thông tin cơ bản cho sản phẩm
        productRequest.setCreatedBy("admin");
        productRequest.setProductCode("SP" + random.nextInt(10000));
        productRequest.setModifiedDate(new Date());
        productRequest.setModifiedBy("admin");
        productRequest.setCreatedDate(new Date());

        // Tạo đối tượng Product từ ProductRequest
        Product product = ProductMapper.INTANCE.toEntityProduct(productRequest);

        // Xử lý lưu ảnh vào thư mục và lấy đường dẫn
        List<Images> images = new ArrayList<>();
        if (productRequest.getImages() != null) {
            for (MultipartFile file : productRequest.getImages()) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    Images image = new Images();
                    image.setImagePath(imagePath);
                    image.setProduct(product); // Đặt sản phẩm cho ảnh
                    images.add(image);
                } catch (IOException e) {
                    // Xử lý lỗi khi lưu ảnh
                    throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
                }
            }
        }

        // Lưu Product trước để có ID của Product cho các Images
        product.setImages(new HashSet<>(images)); // Thiết lập danh sách Images cho Product
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
                    newCategory.setCreatedBy("admin");
                    newCategory.setModifiedBy("admin");
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
                    productCategory.setCreatedBy("admin");
                    productCategory.setModifiedBy("admin");
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


    // Phương thức chuyển đổi MultipartFile thành base64 encoding
    private String convertToBase64(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }


    private String saveFileToLocalDirectory(MultipartFile file) throws IOException {
        String directory = "/Users/anhtuanle/Desktop/ProductManager/UploadImage"; // Thay đổi đường dẫn theo nhu cầu của bạn
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(directory, fileName);

        // Lưu tệp vào thư mục
        Files.write(path, file.getBytes());

        return path.toString(); // Trả về đường dẫn đầy đủ của tệp
    }


    @Transactional
    public ApiResponse<Page<GetAllProduct>> getPagedProductDetails(Pageable pageable, String name, String productCode, String status, LocalDate startDate, LocalDate endDate) {
        Locale locale = LocaleContextHolder.getLocale();

        // Gọi phương thức repository để lấy kết quả phân trang với các tham số tìm kiếm
        Page<Object[]> result = productRepository.findProductDetailsWithCategories(name, productCode, status, startDate, endDate, pageable);

        // Chuyển đổi kết quả từ Object[] thành GetAllProduct
        List<GetAllProduct> products = result.getContent().stream().map(record -> {
            GetAllProduct product = new GetAllProduct();
            product.setId(((Number) record[0]).longValue()); // Chuyển đổi id thành Long
            product.setProductName((String) record[1]);
            product.setProductCode((String) record[2]);
            product.setCategoryName((String) record[3]);
            product.setStatus((String) record[4]);
            product.setCreateDate((Date) record[5]);
            product.setModifiedDate((Date) record[6]);

            // Check for null and provide a default value if necessary
            product.setQuantity(record[7] != null ? ((Number) record[7]).longValue() : 0L);
            product.setDescription((String) record[8]);
            product.setPrice(record[9] != null ? ((Number) record[9]).doubleValue() : 0.0);

            return product;
        }).collect(Collectors.toList());

        // Tạo ApiResponse và set kết quả
        ApiResponse<Page<GetAllProduct>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(new PageImpl<>(products, pageable, result.getTotalElements()));
        apiResponse.setMessage(messageSource.getMessage("success.products.getAll", null, locale));

        return apiResponse;
    }



    @Transactional
    public ApiResponse<ProductDto> deleteMem(Long id) {
        Locale locale = LocaleContextHolder.getLocale();

        ApiResponse<ProductDto> apiResponse = new ApiResponse<>();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_EXISTS));

        if (product.getStatus().equals("1")) {
            product.setStatus("0");
            product.setModifiedBy("admin");
            product.setModifiedDate(new Date());
            productRepository.save(product);

            List<ProductCategory> productCourses = productRepository.findProductCategoryByIdProduct(product.getId());
            productCourses.forEach(sc -> sc.setStatus("0"));
            productCourses.forEach(sc -> sc.setModifiedDate(new Date()));
            productCourses.forEach(sc -> sc.setModifiedBy("admin"));

            productCategoryRepo.saveAll(productCourses);

            ProductDto productDto = ProductMapper.INTANCE.toDto(product);
            apiResponse.setMessage(messageSource.getMessage("success.soft.delete", null, locale));
            apiResponse.setResult(productDto);
        } else {
            throw new AppException(ErrorCode.PRODUCT_EXISTS);
        }
        return apiResponse;
    }


    @Transactional
    public ApiResponse<Page<GetAllProduct>> getPagedProductDetailsCategory(String categoryName, Pageable pageable) {
        Locale locale = LocaleContextHolder.getLocale();

        // Gọi phương thức repository với tham số tìm kiếm
        Page<Object[]> result = productRepository.findProductDetailsWithCategories(
                categoryName != null && !categoryName.isEmpty() ? categoryName : null,
                pageable
        );

        // Chuyển đổi kết quả từ Object[] thành GetAllProduct
        List<GetAllProduct> products = result.getContent().stream().map(record -> {
            GetAllProduct product = new GetAllProduct();
            product.setId(((Number) record[0]).longValue());
            product.setProductName((String) record[1]);
            product.setProductCode((String) record[2]);
            product.setCategoryName((String) record[3]);
            product.setStatus((String) record[4]);
            product.setCreateDate((Date) record[5]);
            product.setModifiedDate((Date) record[6]);
            product.setQuantity(record[7] != null ? ((Number) record[7]).longValue() : 0L);
            product.setDescription((String) record[8]);
            product.setPrice(record[9] != null ? ((Number) record[9]).doubleValue() : 0.0);
            return product;
        }).collect(Collectors.toList());

        // Tạo ApiResponse và set kết quả
        ApiResponse<Page<GetAllProduct>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(new PageImpl<>(products, pageable, result.getTotalElements()));
        apiResponse.setMessage(messageSource.getMessage("success.products.getAll", null, locale));

        return apiResponse;
    }


    @Transactional
    public ApiResponse<Page<GetAllProduct>> getPagedProductDetails(String productName, Pageable pageable) {
        Locale locale = LocaleContextHolder.getLocale();

        // Gọi phương thức repository với tham số tìm kiếm
        Page<Object[]> result = productRepository.findProductDetailsWithCategories(
                productName != null && !productName.isEmpty() ? productName : null,
                pageable
        );

        // Chuyển đổi kết quả từ Object[] thành GetAllProduct
        List<GetAllProduct> products = result.getContent().stream().map(record -> {
            GetAllProduct product = new GetAllProduct();
            product.setId(((Number) record[0]).longValue());
            product.setProductName((String) record[1]);
            product.setProductCode((String) record[2]);
            product.setCategoryName((String) record[3]);
            product.setStatus((String) record[4]);
            product.setCreateDate((Date) record[5]);
            product.setModifiedDate((Date) record[6]);
            product.setQuantity(record[7] != null ? ((Number) record[7]).longValue() : 0L);
            product.setDescription((String) record[8]);
            product.setPrice(record[9] != null ? ((Number) record[9]).doubleValue() : 0.0);
            return product;
        }).collect(Collectors.toList());

        // Tạo ApiResponse và set kết quả
        ApiResponse<Page<GetAllProduct>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(new PageImpl<>(products, pageable, result.getTotalElements()));
        apiResponse.setMessage(messageSource.getMessage("success.products.getAll", null, locale));

        return apiResponse;
    }

    @Transactional

    public ByteArrayInputStream exportProductsToExcel(List<GetAllProduct> products) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Products");

            // Create header
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Product Code", "Category Names", "Price", "Quantity", "Status", "Created Date", "Modified Date", "Description"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Add product data
            int rowIdx = 1;
            for (GetAllProduct product : products) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getProductName());
                row.createCell(2).setCellValue(product.getProductCode());
                row.createCell(3).setCellValue(product.getCategoryName());
                row.createCell(4).setCellValue(product.getPrice());
                row.createCell(5).setCellValue(product.getQuantity());
                row.createCell(6).setCellValue(product.getStatus());
                row.createCell(7).setCellValue(product.getCreateDate().toString());
                row.createCell(8).setCellValue(product.getModifiedDate().toString());
                row.createCell(9).setCellValue(product.getDescription());
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
    @Override
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
        List<Images> images = new ArrayList<>();
        if (dto.getImages() != null) {
            List<Images> existingImages = imagesRepository.findByProductId(product.getId());
            for (Images image : existingImages) {
                // Delete image file from the file system
                File file = new File(image.getImagePath());
                if (file.exists()) {
                    file.delete();
                }
                imagesRepository.deleteById(image.getId());
            }
            imagesRepository.deleteAllByProductId(dto.getId());

            for (MultipartFile file : dto.getImages()) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    Images image = new Images();
                    image.setImagePath(imagePath);
                    image.setProduct(product); // Đặt sản phẩm cho ảnh
                    images.add(image);
                } catch (IOException e) {
                    // Xử lý lỗi khi lưu ảnh
                    throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
                }
            }

            // Lưu danh sách ảnh mới vào cơ sở dữ liệu
            imagesRepository.saveAll(images);
        }


        // Tạo phản hồi API
        ProductDto updatedProductDto = ProductMapper.INTANCE.toDto(product);
        ApiResponse<ProductDto> response = new ApiResponse<>();
        response.setResult(updatedProductDto);
        response.setMessage(messageSource.getMessage("success.update", null, locale));

        return response;
    }

    @Transactional
    @Override
    public ApiResponse<ProductDto> findById(Long id) {
        Locale locale = LocaleContextHolder.getLocale();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_EXISTS));
        ProductDto productDto = ProductMapper.INTANCE.toDto(product);
        ApiResponse<ProductDto> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(messageSource.getMessage("success.products.getAll", null, locale));
        apiResponse.setResult(productDto);

        return apiResponse;
    }

}
