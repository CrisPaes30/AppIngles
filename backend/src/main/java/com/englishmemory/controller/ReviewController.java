package com.englishmemory.controller;

import com.englishmemory.dto.request.ReviewAnswerRequest;
import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.ReviewCardResponse;
import com.englishmemory.dto.response.ReviewResultResponse;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Sistema de revisão espaçada com algoritmo SM-2")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/today")
    @Operation(
            summary = "Revisões do dia",
            description = "Retorna as cartas de revisão pendentes para hoje, " +
                          "ordenadas pela data de vencimento mais antiga. Máximo de 50 por sessão."
    )
    public ResponseEntity<ApiResponse<List<ReviewCardResponse>>> getTodayReviews() {
        List<ReviewCardResponse> cards = reviewService.getTodayReviews(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(cards,
                cards.size() + " palavra(s) para revisar hoje"));
    }

    @GetMapping("/count")
    @Operation(summary = "Contagem de revisões pendentes", description = "Retorna quantas palavras estão aguardando revisão hoje")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countTodayReviews() {
        long count = reviewService.countTodayReviews(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("pendingReviews", count)));
    }

    @PostMapping("/{wordId}/answer")
    @Operation(
            summary = "Submeter resposta de revisão",
            description = """
                    Registra a resposta do usuário e recalcula o agendamento via SM-2.

                    **Valores de qualidade recomendados:**
                    - `0` — AGAIN: errei completamente
                    - `3` — HARD: acertei, mas com muita dificuldade
                    - `4` — GOOD: acertei com algum esforço
                    - `5` — EASY: acertei facilmente

                    Qualidade < 3 reinicia o ciclo e a palavra volta para revisão hoje.
                    """
    )
    public ResponseEntity<ApiResponse<ReviewResultResponse>> submitAnswer(
            @Parameter(description = "ID da palavra sendo revisada") @PathVariable Long wordId,
            @Valid @RequestBody ReviewAnswerRequest request
    ) {
        ReviewResultResponse result = reviewService.submitAnswer(SecurityUtils.getCurrentUserId(), wordId, request);
        return ResponseEntity.ok(ApiResponse.success(result, result.getFeedbackMessage()));
    }
}
