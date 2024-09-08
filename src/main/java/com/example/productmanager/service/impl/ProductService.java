package com.example.productmanager.service.impl;

import com.example.productmanager.entity.*;
import com.example.productmanager.exception.ConstraintViolationExceptionCustom;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import com.example.productmanager.dto.request.ProductRequest;
import com.example.productmanager.dto.request.ProductUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.dto.response.GetAllProduct;
import com.example.productmanager.dto.response.ProductDto;
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
import java.util.function.Function;
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
    public ApiResponse<ProductDto> create(String data, List<MultipartFile> files) throws JsonProcessingException {
        Locale locale = LocaleContextHolder.getLocale();

        // Chuyển đổi dữ liệu từ chuỗi JSON thành đối tượng ProductRequest
        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest productRequest = objectMapper.readValue(data, ProductRequest.class);

        // Set thông tin cơ bản cho sản phẩm
        productRequest.setCreatedBy("admin");
        productRequest.setModifiedDate(new Date());
        productRequest.setModifiedBy("admin");
        productRequest.setCreatedDate(new Date());

        // Sử dụng Validator để validate các trường trong ProductRequest
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(productRequest);
        if (!violations.isEmpty()) {
            // Ném ra ngoại lệ nếu có lỗi ràng buộc
            throw new ConstraintViolationExceptionCustom(violations);
        }

        // Kiểm tra xem mã sản phẩm có tồn tại không
        if (productRepository.existsByProductCode(productRequest.getProductCode())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }

        // Tạo đối tượng Product từ ProductRequest
        Product product = ProductMapper.INTANCE.toEntityProduct(productRequest);

        // Xử lý lưu ảnh vào thư mục và lấy đường dẫn
        List<Images> images = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    Images image = new Images();
                    image.setImagePath(imagePath);
                    image.setProduct(product); // Đặt sản phẩm cho ảnh
                    image.setStatus(1);
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

        // Lấy tất cả categoryIds từ ProductRequest
        List<Long> categoryIds = productRequest.getCategories().stream()
                .map(CategoryDto::getId)
                .filter(Objects::nonNull) // Lọc ra các category đã tồn tại
                .collect(Collectors.toList());

        // Truy vấn tất cả các Category bằng 1 lần query
        List<Category> existingCategories = categoryRepository.findAllByIdIn(categoryIds);

        // Tìm các Category mới từ ProductRequest
        List<Category> newCategories = productRequest.getCategories().stream()
                .filter(categoryDto -> categoryDto.getId() == null) // Lọc các category mới (không có id)
                .map(categoryDto -> {
                    Category newCategory = CategoryMapper.INTANCE.toEntity(categoryDto);
                    if (categoryRepository.existsByCategoryCode(categoryDto.getCategoryCode())){
                        throw new AppException(ErrorCode.CATEGORY_NOT_EXISTS);
                    }
                    newCategory.setCreatedDate(new Date());
                    newCategory.setModifiedDate(new Date());
                    newCategory.setCreatedBy("admin");
                    newCategory.setModifiedBy("admin");
                    return newCategory;
                })
                .collect(Collectors.toList());

        // Lưu danh sách các Category mới 1 lần thay vì từng cái
        if (!newCategories.isEmpty()) {
            categoryRepository.saveAll(newCategories);
        }

        // Kết hợp danh sách category đã tồn tại và category mới
        List<Category> allCategories = new ArrayList<>(existingCategories);
        allCategories.addAll(newCategories);

        // Tạo các ProductCategory và gán category cho product
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

        // Lưu tất cả các ProductCategory trong 1 lần query
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
    public ApiResponse<ProductDto> updateProductAndCategories(String data, List<MultipartFile> files) throws JsonProcessingException {
        Locale locale = LocaleContextHolder.getLocale();

        // Chuyển đổi dữ liệu từ chuỗi JSON thành đối tượng ProductUpdate
        ObjectMapper objectMapper = new ObjectMapper();
        ProductUpdate dto = objectMapper.readValue(data, ProductUpdate.class);

        // Sử dụng Validator để validate các trường trong ProductUpdate
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ProductUpdate>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationExceptionCustom(violations);
        }

        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_EXISTS));

        ProductMapper.INTANCE.updateProductFromDto(dto, product);
        product.setModifiedDate(new Date());
        product.setModifiedBy("admin");
        productRepository.save(product);

        // Lấy danh mục hiện tại và các danh mục mới cần thêm
        List<Long> currentCategoryIds = productCategoryRepo.findCategoryIdsByProductId(product.getId());
        Set<Long> newCategoryIds = new HashSet<>(dto.getCategoryIds());

        // Lọc danh mục mới từ listCategory và kiểm tra trùng lặp code
        Set<String> newCategoryCodes = dto.getListCategory().stream()
                .filter(categoryDto -> categoryDto.getId() == null)
                .map(CategoryDto::getCategoryCode)
                .collect(Collectors.toSet());

        if (!newCategoryCodes.isEmpty()) {
            // Kiểm tra trùng lặp code trong cơ sở dữ liệu
            if (categoryRepository.existsByCategoryCode(dto.getProductCode())) {
                throw new AppException(ErrorCode.CATEGORY_NOT_EXISTS);
            }

            // Tạo và lưu các danh mục mới
            List<Category> categoriesToSave = dto.getListCategory().stream()
                    .filter(categoryDto -> categoryDto.getId() == null)
                    .map(categoryDto -> {
                        Category newCategory = CategoryMapper.INTANCE.toEntity(categoryDto);
                        newCategory.setCreatedDate(new Date());
                        newCategory.setModifiedDate(new Date());
                        newCategory.setModifiedBy("admin");
                        newCategory.setCreatedBy("admin");
                        return newCategory;
                    })
                    .collect(Collectors.toList());

            if (!categoriesToSave.isEmpty()) {
                categoryRepository.saveAll(categoriesToSave);
                categoriesToSave.forEach(category -> newCategoryIds.add(category.getId()));
            }
        }

        // Xử lý các danh mục cần vô hiệu hóa
        Set<Long> categoriesToDeactivate = new HashSet<>(currentCategoryIds);
        categoriesToDeactivate.removeAll(newCategoryIds);

        if (!categoriesToDeactivate.isEmpty()) {
            productCategoryRepo.updateStatusByProductIdAndCategoryIds(product.getId(), categoriesToDeactivate);
        }

        // Xử lý các danh mục mới 1 ở đây
        List<Category> categories = categoryRepository.findAllByIdIn(new ArrayList<>(newCategoryIds));
        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        Set<ProductCategory> newProductCategories = categories.stream()
                .map(category -> {
                    return productCategoryRepo.findByProductIdAndCategoryId(product.getId(), category.getId())
                            .map(pc -> {
                                pc.setStatus(dto.getStatus());
                                pc.setModifiedBy("admin");
                                pc.setModifiedDate(new Date());
                                return pc;
                            })
                            .orElseGet(() -> {
                                ProductCategory pc = new ProductCategory();
                                pc.setProduct(product);
                                pc.setCategory(category);
                                pc.setStatus(dto.getStatus());
                                pc.setModifiedBy("admin");
                                pc.setModifiedDate(new Date());
                                pc.setCreatedBy("admin");
                                pc.setCreatedDate(new Date());
                                return pc;
                            });
                })
                .collect(Collectors.toSet());

        productCategoryRepo.saveAll(newProductCategories);

        // Xử lý cập nhật hình ảnh (Images)
        Set<Long> imagesIdsToKeep = new HashSet<>(dto.getImagesIds());
        List<Images> existingImages = imagesRepository.findByProductId(product.getId());

        // Vô hiệu hóa ảnh không còn trong danh sách
        existingImages.stream()
                .filter(image -> !imagesIdsToKeep.contains(image.getId()))
                .forEach(image -> {
                    image.setStatus(0);
                    imagesRepository.save(image);
                });

        // Xử lý ảnh mới từ tệp tin
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    Images newImage = new Images();
                    newImage.setImagePath(imagePath);
                    newImage.setProduct(product);
                    newImage.setStatus(1);
                    imagesRepository.save(newImage);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
                }
            }
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
        ApiResponse<ProductDto> apiResponse = new ApiResponse<>();

        // Truy vấn để lấy tất cả các hình ảnh (không lọc theo status)
        Product product = productRepository.findByIdProduct(id);

        // Lọc hình ảnh có status = 1
        List<Images> activeImages = product.getImages().stream()
                .filter(image -> image.getStatus() == 1)
                .collect(Collectors.toList());

        // Cập nhật danh sách hình ảnh đã lọc vào DTO
        ProductDto productDto = ProductMapper.INTANCE.toDto(product);
        productDto.setImages(activeImages);

        apiResponse.setResult(productDto);
        apiResponse.setMessage(messageSource.getMessage("success.search", null, locale));

        return apiResponse;
    }

}
