package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.ImagesCategory;
import com.example.productmanager.entity.ProductCategory;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.mapper.CategoryMapper;
import com.example.productmanager.repository.CategoryRepository;
import com.example.productmanager.repository.ImagesCategoryRepository;
import com.example.productmanager.repository.ProductCategoryRepo;
import com.example.productmanager.service.ICategoryService;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    public ApiResponse<CategoryDto> createCategory(CategoryRequest dto) {
        Locale locale = LocaleContextHolder.getLocale();

        // Tạo đối tượng Category từ DTO
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setCategoryCode(dto.getCategoryCode());
        category.setStatus(dto.getStatus());
        category.setCreatedDate(new Date());
        category.setModifiedDate(new Date());
        category.setCreatedBy("admin");
        category.setModifiedBy("admin");

        // Lưu category vào database trước để lấy ID
        categoryRepository.save(category);

        // Lưu hình ảnh và tạo đối tượng ImagesCategory
        List<ImagesCategory> images = new ArrayList<>();
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (MultipartFile file : dto.getImages()) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    ImagesCategory imageCategory = new ImagesCategory();
                    imageCategory.setImagePath(imagePath);
                    imageCategory.setCategory(category);
                    images.add(imageCategory);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
                }
            }
            imagesCategoryRepository.saveAll(images);
        }

        // Chuyển đổi entity sang DTO để trả về response
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        ApiResponse<CategoryDto> response = new ApiResponse<>();
        response.setResult(categoryDto);
        response.setMessage("Category created successfully!");

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
        Category category = categoryRepository.findCategoryWithImages(id);
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        categoryDto.setImages(category.getImages());
        apiResponse.setResult(categoryDto);
        apiResponse.setMessage(messageSource.getMessage("success.search", null, locale));

        return apiResponse;
    }

    @Transactional
    @Override
    public ApiResponse<CategoryDto> update(Long id, CategoryUpdate request) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();

        request.setModifiedBy("admin");
        request.setModifiedDate(new Date());
        // Lấy đối tượng Category từ database
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_EXISTS));

        // Cập nhật thông tin của category từ DTO
        CategoryMapper.INTANCE.updateCategoryFromDto(request, category);


        // Xử lý ảnh
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Xóa các ảnh cũ
            List<ImagesCategory> existingImages = imagesCategoryRepository.findByCategoryId(id);
            for (ImagesCategory image : existingImages) {
                File file = new File(image.getImagePath());
                if (file.exists()) {
                    file.delete(); // Xóa file khỏi hệ thống tệp
                }
                imagesCategoryRepository.deleteById(image.getId()); // Xóa ảnh khỏi cơ sở dữ liệu
            }

            // Upload các ảnh mới
            List<ImagesCategory> images = new ArrayList<>();
            for (MultipartFile file : request.getImages()) {
                try {
                    String imagePath = saveFileToLocalDirectory(file);
                    ImagesCategory imageCategory = new ImagesCategory();
                    imageCategory.setImagePath(imagePath);
                    imageCategory.setCategory(category);
                    images.add(imageCategory);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
                }
            }
            imagesCategoryRepository.saveAll(images);
        }

        // Lưu thông tin category cập nhật vào cơ sở dữ liệu
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

    @Transactional
    @Override
    public ApiResponse<Page<CategoryDto>> findByName(String title, int page, int size) {
        Locale locale = LocaleContextHolder.getLocale();
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.searchByName(title, pageable);

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
