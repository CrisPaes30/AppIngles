package com.englishmemory.service.impl;

import com.englishmemory.dto.request.CreateCategoryRequest;
import com.englishmemory.dto.request.UpdateCategoryRequest;
import com.englishmemory.dto.response.CategoryResponse;
import com.englishmemory.entity.Category;
import com.englishmemory.entity.User;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.mapper.CategoryMapper;
import com.englishmemory.repository.CategoryRepository;
import com.englishmemory.repository.UserRepository;
import com.englishmemory.repository.VocabularyWordRepository;
import com.englishmemory.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final VocabularyWordRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> findAll(Long userId) {
        return categoryRepository.findAllByUserIdAndActiveTrueOrderByNameAsc(userId)
                .stream()
                .map(cat -> enrichWithWordCount(userId, cat))
                .toList();
    }

    @Override
    public CategoryResponse findById(Long userId, Long id) {
        Category category = findCategoryOrThrow(userId, id);
        return enrichWithWordCount(userId, category);
    }

    @Override
    @Transactional
    public CategoryResponse create(Long userId, CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndActiveTrue(request.getName(), userId)) {
            throw new BusinessException("Já existe uma categoria com o nome '" + request.getName() + "'.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        Category category = categoryMapper.toEntity(request);
        category.setUser(user);

        Category saved = categoryRepository.save(category);
        log.info("Categoria '{}' criada para usuário {}", saved.getName(), userId);

        return enrichWithWordCount(userId, saved);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long userId, Long id, UpdateCategoryRequest request) {
        Category category = findCategoryOrThrow(userId, id);

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndActiveTrue(request.getName(), userId)) {
                throw new BusinessException("Já existe uma categoria com o nome '" + request.getName() + "'.");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().isBlank() ? null : request.getDescription());
        }
        if (request.getColor() != null)       category.setColor(request.getColor());
        if (request.getIcon() != null)        category.setIcon(request.getIcon());

        Category saved = categoryRepository.save(category);
        log.info("Categoria '{}' atualizada para usuário {}", saved.getName(), userId);

        return enrichWithWordCount(userId, saved);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Category category = findCategoryOrThrow(userId, id);

        long wordCount = vocabularyRepository.countByUserIdAndCategoryIdAndActiveTrue(userId, id);
        if (wordCount > 0) {
            throw new BusinessException(
                    "Não é possível remover a categoria '" + category.getName() +
                    "' pois ela possui " + wordCount + " palavra(s) ativa(s). " +
                    "Remova ou reatribua as palavras antes de deletar a categoria."
            );
        }

        category.setActive(false);
        categoryRepository.save(category);
        log.info("Categoria '{}' removida (soft delete) para usuário {}", category.getName(), userId);
    }

    // -------------------------------------------------------------------------

    private Category findCategoryOrThrow(Long userId, Long id) {
        return categoryRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    private CategoryResponse enrichWithWordCount(Long userId, Category category) {
        CategoryResponse response = categoryMapper.toResponse(category);
        long count = vocabularyRepository.countByUserIdAndCategoryIdAndActiveTrue(userId, category.getId());
        response.setWordCount(count);
        return response;
    }
}
