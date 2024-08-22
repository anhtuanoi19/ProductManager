package com.example.productmanager.service.impl;

import com.example.productmanager.dto.request.CategoryRequest;
import com.example.productmanager.dto.response.ApiResponse;
import com.example.productmanager.dto.response.CategoryDto;
import com.example.productmanager.entity.Category;
import com.example.productmanager.exception.AppException;
import com.example.productmanager.exception.ErrorCode;
import com.example.productmanager.mapper.CategoryMapper;
import com.example.productmanager.repository.CategoryRepository;
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

        if (listDto != null){
            apiResponse.setResult(listDto);
            apiResponse.setMessage(messageSource.getMessage("success.getAllCategory", null,locale));
        }else {
            throw new AppException(ErrorCode.CATEGORY_LIST_NOT_FOUND);
        }
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
        if (categoryDto != null){
            apiResponse.setMessage(messageSource.getMessage("success.create", null, locale));
            apiResponse.setResult(categoryDto);
        }else {
            throw new AppException(ErrorCode.ERROR_ADD_CATEGORY);
        }
        return apiResponse;
    }
}
