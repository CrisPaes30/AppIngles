package com.englishmemory.mapper;

import com.englishmemory.dto.request.CreateCategoryRequest;
import com.englishmemory.dto.response.CategoryResponse;
import com.englishmemory.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "wordCount", ignore = true)
    CategoryResponse toResponse(Category category);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Category toEntity(CreateCategoryRequest request);
}
