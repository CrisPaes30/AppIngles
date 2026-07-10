package com.englishmemory.controller;

import com.englishmemory.dto.request.CreateCategoryRequest;
import com.englishmemory.dto.request.UpdateCategoryRequest;
import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.CategoryResponse;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Gerenciamento de categorias do vocabulário")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna todas as categorias do usuário com contagem de palavras")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAll() {
        List<CategoryResponse> categories = categoryService.findAll(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(@PathVariable Long id) {
        CategoryResponse response = categoryService.findById(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Criar categoria")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.create(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Categoria criada com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza os campos fornecidos (campos nulos são ignorados)")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse response = categoryService.update(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Categoria atualizada com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover categoria", description = "Remove a categoria. Não é permitido remover categorias com palavras ativas.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Categoria removida com sucesso"));
    }
}
