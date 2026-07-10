package com.englishmemory.controller;

import com.englishmemory.dto.request.AnswerExerciseRequest;
import com.englishmemory.dto.request.GenerateExerciseRequest;
import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.ExerciseAnswerResponse;
import com.englishmemory.dto.response.ExerciseResponse;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exercise")
@RequiredArgsConstructor
@Tag(name = "Exercise", description = "Geração e resposta de exercícios via IA")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    @Operation(summary = "Listar exercícios gerados", description = "Retorna histórico paginado de exercícios do usuário")
    public ResponseEntity<ApiResponse<PageResponse<ExerciseResponse>>> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ExerciseResponse> result = exerciseService.findAll(
                SecurityUtils.getCurrentUserId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Gerar exercício",
            description = """
                    Gera um exercício via IA (OpenAI).

                    - `vocabularyWordId`: opcional — se omitido, escolhe automaticamente a palavra com menor mastery
                    - `type`: opcional — se omitido, escolhe um tipo aleatório

                    A resposta **não inclui** a resposta correta. Envie o resultado via `/exercise/{id}/answer`.
                    """
    )
    public ResponseEntity<ApiResponse<ExerciseResponse>> generate(
            @RequestBody(required = false) GenerateExerciseRequest request
    ) {
        if (request == null) request = new GenerateExerciseRequest();
        ExerciseResponse response = exerciseService.generate(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Exercício gerado com sucesso"));
    }

    @PostMapping("/{id}/answer")
    @Operation(
            summary = "Responder exercício",
            description = """
                    Registra a resposta do usuário e retorna a correção completa.

                    A resposta correta e a explicação são reveladas apenas neste endpoint
                    (não na geração), garantindo que o exercício seja genuinamente desafiador.

                    Para `SENTENCE_BUILDING`, qualquer resposta não-vazia é aceita —
                    a análise real virá da IA quando integrada.
                    """
    )
    public ResponseEntity<ApiResponse<ExerciseAnswerResponse>> answer(
            @Parameter(description = "ID do exercício") @PathVariable Long id,
            @Valid @RequestBody AnswerExerciseRequest request
    ) {
        ExerciseAnswerResponse response = exerciseService.answer(SecurityUtils.getCurrentUserId(), id, request);
        String message = Boolean.TRUE.equals(response.getIsCorrect()) ? "Resposta correta!" : "Resposta incorreta. Veja a explicação.";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }
}
