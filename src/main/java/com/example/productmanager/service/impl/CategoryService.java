package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.request.CategoryUpdate;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.ProductCategory;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.mapper.CategoryMapper;
import com.example.productmanager.repository.CategoryRepository;
import com.example.productmanager.repository.ProductCategoryRepo;
import com.example.productmanager.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryService implements ICategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MessageSource messageSource;

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
    public ApiResponse<CategoryDto> create(CategoryRequest categoryRequest) {
        Locale locale = LocaleContextHolder.getLocale();
        categoryRequest.setCreatedDate(new Date());
        categoryRequest.setModifiedDate(new Date());
        Category category = CategoryMapper.INTANCE.toRequestEntity(categoryRequest);
        categoryRepository.save(category);
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();
        if (categoryDto != null) {
            apiResponse.setMessage(messageSource.getMessage("success.create", null, locale));
            apiResponse.setResult(categoryDto);
        } else {
            throw new AppException(ErrorCode.ERROR_ADD_CATEGORY);
        }
        return apiResponse;
    }

    @Override
    public ApiResponse<CategoryDto> findById(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();
        Category category = categoryRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.CATEGORY_EXISTS));
        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        apiResponse.setResult(categoryDto);
        apiResponse.setMessage(messageSource.getMessage("success.search", null, locale));

        return apiResponse;
    }

    @Transactional
    @Override
    public ApiResponse<CategoryDto> update(Long id, CategoryUpdate request) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<CategoryDto> apiResponse = new ApiResponse<>();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_EXISTS));

        CategoryMapper.INTANCE.updateCategoryFromDto(request, category);
        category.setModifiedBy("admin");
        category.setModifiedDate(new Date());

        if ("0".equals(category.getStatus())) {
            List<ProductCategory> productCategories = categoryRepository.findProductCategoryByCategoryId(category.getId());
            productCategories.forEach(sc -> sc.setStatus("0"));
        }

        CategoryDto categoryDto = CategoryMapper.INTANCE.toDto(category);
        apiResponse.setMessage(messageSource.getMessage("success.update", null, locale));
        apiResponse.setResult(categoryDto);

        return apiResponse;
    }


    @Transactional
    @Override
    public ApiResponse<Boolean> deleteMem(Long categoryId) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new AppException(ErrorCode.CATEGORY_EXISTS));
        category.setStatus("0");
        category.setModifiedBy("admin");
        category.setModifiedDate(new Date());
        apiResponse.setMessage(messageSource.getMessage("success.update", null, locale));
        apiResponse.setResult(true);
        return apiResponse;
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

    @Override
    public ApiResponse<Page<CategoryDto>> findByName(String id, int page, int size) {
        return null;
    }
}
