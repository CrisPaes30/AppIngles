package com.englishmemory.controller;

import com.englishmemory.dto.request.CreateVocabularyRequest;
import com.englishmemory.dto.request.EnrichWordRequest;
import com.englishmemory.dto.request.UpdateVocabularyRequest;
import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.dto.response.VocabularyResponse;
import com.englishmemory.dto.response.VocabularySummaryResponse;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.VocabularyService;
import com.englishmemory.service.dictionary.model.WordDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vocabulary")
@RequiredArgsConstructor
@Tag(name = "Vocabulary", description = "Gerenciamento do vocabulário do usuário")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    @Operation(summary = "Listar vocabulário", description = "Retorna a lista paginada de palavras do usuário com suporte a filtros")
    public ResponseEntity<ApiResponse<PageResponse<VocabularySummaryResponse>>> findAll(
            @Parameter(description = "Busca por palavra") @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por nível CEFR") @RequestParam(required = false) CefrLevel cefrLevel,
            @Parameter(description = "Filtrar por classe gramatical") @RequestParam(required = false) PartOfSpeech partOfSpeech,
            @Parameter(description = "Filtrar por categoria") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenação") @RequestParam(defaultValue = "word") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<VocabularySummaryResponse> result = vocabularyService.findAll(
                SecurityUtils.getCurrentUserId(), search, cefrLevel, partOfSpeech, categoryId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/weak")
    @Operation(summary = "Palavras fracas", description = "Retorna as palavras com menor mastery level (máx 20)")
    public ResponseEntity<ApiResponse<List<VocabularySummaryResponse>>> listWeak() {
        return ResponseEntity.ok(ApiResponse.success(
                vocabularyService.listWeak(SecurityUtils.getCurrentUserId())));
    }

    @GetMapping("/due-today")
    @Operation(summary = "Palavras para revisar hoje", description = "Retorna as palavras com revisão pendente para hoje")
    public ResponseEntity<ApiResponse<List<VocabularySummaryResponse>>> listDueToday() {
        return ResponseEntity.ok(ApiResponse.success(
                vocabularyService.listDueToday(SecurityUtils.getCurrentUserId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar palavra por ID", description = "Retorna todos os detalhes de uma palavra incluindo schedule de revisão")
    public ResponseEntity<ApiResponse<VocabularyResponse>> findById(
            @PathVariable Long id
    ) {
        VocabularyResponse response = vocabularyService.findById(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/enrich")
    @Operation(summary = "Enriquecer palavra com IA", description = "Consulta o provider de dicionário (OpenAI) e retorna todos os detalhes da palavra sem salvar")
    public ResponseEntity<ApiResponse<WordDetails>> enrich(
            @Valid @RequestBody EnrichWordRequest request
    ) {
        WordDetails details = vocabularyService.enrich(SecurityUtils.getCurrentUserId(), request.getWord());
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    @PostMapping
    @Operation(summary = "Cadastrar palavra", description = "Cria uma nova palavra e inicializa automaticamente o schedule de revisão (SM-2) e o progresso")
    public ResponseEntity<ApiResponse<VocabularyResponse>> create(
            @Valid @RequestBody CreateVocabularyRequest request
    ) {
        VocabularyResponse response = vocabularyService.create(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Palavra cadastrada com sucesso"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar palavra", description = "Atualiza os campos fornecidos (campos nulos são ignorados)")
    public ResponseEntity<ApiResponse<VocabularyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVocabularyRequest request
    ) {
        VocabularyResponse response = vocabularyService.update(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Palavra atualizada com sucesso"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover palavra", description = "Remove a palavra via soft delete (preserva histórico de revisões)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        vocabularyService.delete(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Palavra removida com sucesso"));
    }
}
