package com.englishmemory.service;

import com.englishmemory.dto.request.CreateCategoryRequest;
import com.englishmemory.dto.response.CategoryResponse;
import com.englishmemory.entity.Category;
import com.englishmemory.entity.User;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.mapper.CategoryMapper;
import com.englishmemory.repository.CategoryRepository;
import com.englishmemory.repository.UserRepository;
import com.englishmemory.repository.VocabularyWordRepository;
import com.englishmemory.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService — testes unitários")
class CategoryServiceTest {

    @Mock private CategoryRepository     categoryRepository;
    @Mock private UserRepository         userRepository;
    @Mock private CategoryMapper         categoryMapper;
    @Mock private VocabularyWordRepository vocabularyWordRepository;

    @InjectMocks private CategoryServiceImpl categoryService;

    private User    user;
    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("dev@englishmemory.ai");

        category = new Category();
        category.setId(10L);
        category.setName("Phrasal Verbs");
        category.setColor("#6366f1");
        category.setIcon("book");
        category.setUser(user);

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(10L);
        categoryResponse.setName("Phrasal Verbs");
    }

    @Test
    @DisplayName("findAll: retorna todas as categorias do usuário")
    void findAll_returnsUserCategories() {
        when(categoryRepository.findAllByUserIdAndActiveTrueOrderByNameAsc(1L))
                .thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.findAll(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Phrasal Verbs");
    }

    @Test
    @DisplayName("findAll: usuário sem categorias retorna lista vazia")
    void findAll_noCategories_returnsEmptyList() {
        when(categoryRepository.findAllByUserIdAndActiveTrueOrderByNameAsc(1L)).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.findAll(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("create: salva categoria e retorna DTO")
    void create_validRequest_savesAndReturns() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Grammar");
        request.setColor("#10b981");
        request.setIcon("tag");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdAndActiveTrue("Grammar", 1L))
                .thenReturn(false);
        when(categoryMapper.toEntity(any())).thenReturn(new Category());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.create(1L, request);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("create: nome duplicado lança BusinessException")
    void create_duplicateName_throwsBusinessException() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Phrasal Verbs");
        request.setColor("#6366f1");
        request.setIcon("book");

        when(categoryRepository.existsByNameIgnoreCaseAndUserIdAndActiveTrue("Phrasal Verbs", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Phrasal Verbs");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: usuário não encontrado lança ResourceNotFoundException")
    void create_userNotFound_throwsResourceNotFoundException() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Idioms");
        request.setColor("#f59e0b");
        request.setIcon("star");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: soft-deletes categoria existente")
    void delete_existingCategory_softDeletes() {
        when(categoryRepository.findByIdAndUserIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(category));

        categoryService.delete(1L, 10L);

        assertThat(category.getActive()).isFalse();
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("delete: categoria não encontrada lança ResourceNotFoundException")
    void delete_categoryNotFound_throwsResourceNotFoundException() {
        when(categoryRepository.findByIdAndUserIdAndActiveTrue(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }
}
