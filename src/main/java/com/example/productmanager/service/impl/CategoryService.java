package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.ImagesCategory;
import com.example.productmanager.entity.ProductCategory;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ConstraintViolationExceptionCustom;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.mapper.CategoryMapper;
import com.example.productmanager.repository.CategoryRepository;
import com.example.productmanager.repository.ImagesCategoryRepository;
import com.example.productmanager.repository.ProductCategoryRepo;
import com.example.productmanager.service.ICategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductCategoryRepo productCategoryRepo;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ImagesCategoryRepository imagesCategoryRepository;


    @Transactional
    @Override
    public ApiResponse<Page<CategoryDto>> getAll(Pageable pageable) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<Page<CategoryDto>> apiResponse = new ApiResponse<>();
        Page<Category> categories = categoryRepository.findAll(pageable);
        Page<CategoryDto> listDto = categories.map(CategoryMapper.INTANCE::toDto);

        if (listDto != null) {
            apiResponse.setResult(listDto);
            apiResponse.setMessage(messageSource.getMessage("success.getAllCategory", null, locale));
        } else {
            throw new AppException(ErrorCode.CATEGORY_LIST_NOT_FOUND);
        }
        return apiResponse;
    }

    @Transactional
    @Override
    public ApiResponse<List<CategoryDto>> findAll() {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<List<CategoryDto>> apiResponse = new ApiResponse<>();
        List<Category> listCategory = categoryRepository.findAll();
        List<CategoryDto> dtoList = CategoryMapper.INTANCE.toListDto(listCategory);

        apiResponse.setResult(dtoList);
        apiResponse.setMessage(messageSource.getMessage("success.getAllCategory", null, locale));
        return apiResponse;
    }

    @Transactional

    @Override
    public List<CategoryDto> findAllE() {
        List<Category> category = categoryRepository.findAll();
        List<CategoryDto> categoryDto = CategoryMapper.INTANCE.toListDto(category);
        return categoryDto;
    }

    @Transactional
    public ApiResponse<CategoryDto> createCategory(String data, List<MultipartFile> images) throws JsonProcessingException {
        Locale locale = LocaleContextHolder.getLocale();

        // Chuyển đổi dữ liệu từ chuỗi JSON thành đối tượng CategoryRequest
        ObjectMapper objectMapper = new ObjectMapper();
        CategoryRequest dto = objectMapper.readValue(data, CategoryRequest.class);
        dto.setCreatedBy("admin");
        // Sử dụng Validator để validate các trường trong DTO
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        // Thực hiện kiểm tra ràng buộc (constraint) trên dto
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            // Ném ra ngoại lệ nếu có lỗi ràng buộc
            throw new ConstraintViolationExceptionCustom(violations);
        }

        // Kiểm tra xem mã danh mục có tồn tại không
        if (categoryRepository.existsByCategoryCode(dto.getCategoryCode())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTS);
        }

        // Tạo đối tượng Category từ DTO
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setCategoryCode(dto.getCategoryCode());
        category.setStatus(dto.getStatus());
        category.setCreatedDate(new Date());
        category.setModifiedDate(new Date());
        category.setModifiedBy("admin");
        category.setCreatedBy("admin");

        // Lưu category vào database trước để lấy ID
        category = categoryRepository.save(category);

        // Nếu có file ảnh, lưu file và tạo đối tượng ImagesCategory
        if (images != null && !images.isEmpty()) {
            List<ImagesCategory> imageEntities = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    String imagePath = saveFileToLocalDirectory(image); // Lưu file ảnh vào thư mục cục bộ
                    ImagesCategory imageCategory = new ImagesCategory();
                    imageCategory.setImagePath(imagePath);
                    imageCategory.setCategory(category);
                    imageCategory.setStatus(1);
                    imageEntities.add(imageCategory);
                } catch (IOException e) {
                    throw new RuntimeException("Error saving image: " + e.getMessage(), e);
                }
            }
            imagesCategoryRepository.saveAll(imageEntities);
        }

        // Chuyển đổi Category entity sang CategoryDto
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        // Tạo đối tượng ApiResponse để trả về
        ApiResponse<CategoryDto> response = new ApiResponse<>();
        response.setResult(categoryDto);
        response.setMessage(messageSource.getMessage("success.create", null, locale));

        return response;
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
    @Override
    public ApiResponse<CategoryDto> findById(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();

        // Truy vấn để lấy tất cả các hình ảnh (không lọc theo status)
        Category category = categoryRepository.findCategoryWithActiveImages(id);

        // Lọc hình ảnh có status = 1
        List<ImagesCategory> activeImages = category.getImages().stream()
                .filter(image -> image.getStatus() == 1)
                .collect(Collectors.toList());

        // Cập nhật danh sách hình ảnh đã lọc vào DTO
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        categoryDto.setImages(activeImages);

        apiResponse.setResult(categoryDto);
        apiResponse.setMessage(messageSource.getMessage("success.search", null, locale));

        return apiResponse;
    }



    @Transactional
    @Override
    public ApiResponse<CategoryDto> update(Long id, String data, List<MultipartFile> images) throws JsonProcessingException {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();

        // Chuyển đổi dữ liệu từ chuỗi JSON thành đối tượng CategoryUpdate
        ObjectMapper objectMapper = new ObjectMapper();
        CategoryUpdate dto = objectMapper.readValue(data, CategoryUpdate.class);
        dto.setModifiedBy("admin");
        dto.setModifiedDate(new Date());

        // Sử dụng Validator để validate các trường trong DTO
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        // Thực hiện kiểm tra ràng buộc (constraint) trên DTO
        Set<ConstraintViolation<CategoryUpdate>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationExceptionCustom(violations);
        }

        // Lấy đối tượng Category từ database
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTS));

        // Cập nhật thông tin của category từ DTO
        CategoryMapper.INTANCE.updateCategoryFromDto(dto, category);


        // Xử lý ảnh cũ dựa trên imagesIds
        if (dto.getImgaesIds() != null && !dto.getImgaesIds().isEmpty()) {
            List<ImagesCategory> existingImages = imagesCategoryRepository.findByCategoryId(id);
            for (ImagesCategory image : existingImages) {
                if (!dto.getImgaesIds().contains(image.getId())) {
                    image.setStatus(0);
                }
            }
        }

        // Nếu có file ảnh mới, lưu file và tạo đối tượng ImagesCategory mới
        if (images != null && !images.isEmpty()) {
            List<ImagesCategory> newImages = new ArrayList<>();
            for (MultipartFile file : images) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    ImagesCategory imageCategory = new ImagesCategory();
                    imageCategory.setImagePath(imagePath);
                    imageCategory.setCategory(category);
                    imageCategory.setStatus(1);
                    newImages.add(imageCategory);
                } catch (IOException e) {
                    throw new RuntimeException("Error saving image: " + e.getMessage(), e);
                }
            }
            imagesCategoryRepository.saveAll(newImages);
        }

        // Lưu thông tin category cập nhật vào cơ sở dữ liệu
        category = categoryRepository.save(category);

        // Chuyển đổi Category entity sang CategoryDto
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        apiResponse.setMessage(messageSource.getMessage("success.update", null, locale));
        apiResponse.setResult(categoryDto);

        return apiResponse;
    }


    @Override
    @Transactional
    public ApiResponse<Boolean> deleteMem(Long courseId) {
        Locale locale = LocaleContextHolder.getLocale();

        ApiResponse<Boolean> response = new ApiResponse<>();
        Category course = categoryRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_EXISTS));

        if (course.getStatus().equals("1")){
            course.setStatus("0");
            course.setModifiedBy("admin");
            course.setModifiedDate(new Date());
            categoryRepository.save(course);

            List<ProductCategory> studentCategorys = categoryRepository.findProductCategoryByIdCategory(courseId);

            studentCategorys.forEach(sc -> sc.setStatus("0"));
            studentCategorys.forEach(sc -> sc.setModifiedBy("Admin"));
            studentCategorys.forEach(sc -> sc.setModifiedDate(new Date()));
            productCategoryRepo.saveAll(studentCategorys);

            // Tạo phản hồi API
            response.setMessage(messageSource.getMessage("success.soft.delete", null, locale));
            response.setResult(true);
        }else {
            throw new AppException(ErrorCode.CATEGORY_EXISTS);
        }

        return response;
    }

    @Transactional
    @Override
    public ApiResponse<CategoryDto> open(Long id) {
        Locale locale = LocaleContextHolder.getLocale();

        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();
        Category category = categoryRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.CATEGORY_EXISTS));
        category.setStatus("0");
        category.setModifiedBy("admin");
        category.setModifiedDate(new Date());

        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        apiResponse.setMessage(messageSource.getMessage("success.update", null, locale));

        apiResponse.setResult(categoryDto);
        return apiResponse;
    }

    @Transactional

    public ByteArrayInputStream exportCategoriesToExcel(List<CategoryDto> categories) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Categories");

            // Create header
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Category Code", "Name", "Description", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Add category data
            int rowIdx = 1;
            for (CategoryDto category : categories) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(category.getId());
                row.createCell(1).setCellValue(category.getCategoryCode());
                row.createCell(2).setCellValue(category.getName());
                row.createCell(3).setCellValue(category.getDescription());
                row.createCell(4).setCellValue(category.getStatus() == "1" ? "Active" : "Inactive");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public ApiResponse<List<CategoryDto>> findCategoriesByProductIdAndStatus(Long productId) {
        Locale locale = LocaleContextHolder.getLocale();

        ApiResponse<List<CategoryDto>> apiResponse = new ApiResponse<>();
        List<Category> category = categoryRepository.findCategoriesByProductIdAndStatus(productId);
        List<CategoryDto> categoryDto = CategoryMapper.INTANCE.toListDto(category);
        apiResponse.setMessage(messageSource.getMessage("success.search", null, locale));
        apiResponse.setResult(categoryDto);
        return apiResponse;
    }

    @Transactional
    @Override
    public ApiResponse<Page<CategoryDto>> findByName(String name, String status, String categoryCode,LocalDate startDate, LocalDate endDate, int page, int size) {
        Locale locale = LocaleContextHolder.getLocale();
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.getAll(name,status,categoryCode,startDate,endDate, pageable);

        Page<CategoryDto> categoryDtoPage = categoryPage.map(category -> CategoryMapper.INTANCE.toDto(category));

        ApiResponse<Page<CategoryDto>> apiResponse = new ApiResponse<>();
        if (categoryPage.hasContent()) {
            apiResponse.setMessage(messageSource.getMessage("success.search", null, locale));
        } else {
            apiResponse.setMessage(messageSource.getMessage("error.search", null, locale));
        }
        apiResponse.setResult(categoryDtoPage);

        return apiResponse;
    }

}
