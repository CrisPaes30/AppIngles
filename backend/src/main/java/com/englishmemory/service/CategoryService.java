package com.englishmemory.service;

import com.englishmemory.dto.request.CreateCategoryRequest;
import com.englishmemory.dto.request.UpdateCategoryRequest;
import com.englishmemory.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAll(Long userId);

    CategoryResponse findById(Long userId, Long id);

    CategoryResponse create(Long userId, CreateCategoryRequest request);

    CategoryResponse update(Long userId, Long id, UpdateCategoryRequest request);

    void delete(Long userId, Long id);
}
