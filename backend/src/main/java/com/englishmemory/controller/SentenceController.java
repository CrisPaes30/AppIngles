package com.englishmemory.controller;

import com.englishmemory.dto.request.AnalyzeSentenceRequest;
import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.dto.response.SentencePracticeResponse;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.SentenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sentence")
@RequiredArgsConstructor
@Tag(name = "Sentence", description = "Prática de escrita com análise e correção via IA")
public class SentenceController {

    private final SentenceService sentenceService;

    @PostMapping("/analyze")
    @Operation(
            summary = "Analisar frase",
            description = """
                    Envia uma frase para análise pela IA.

                    A IA retorna:
                    - Versão corrigida da frase
                    - Feedback geral
                    - Explicação gramatical detalhada
                    - Sugestões de frases alternativas
                    - Vocabulário novo identificado

                    O campo `vocabularyWordId` é opcional — quando fornecido,
                    a análise leva em conta o contexto da palavra estudada.
                    """
    )
    public ResponseEntity<ApiResponse<SentencePracticeResponse>> analyze(
            @Valid @RequestBody AnalyzeSentenceRequest request
    ) {
        SentencePracticeResponse response = sentenceService.analyze(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Frase analisada com sucesso"));
    }

    @GetMapping("/history")
    @Operation(summary = "Histórico de práticas de escrita", description = "Retorna o histórico paginado de frases analisadas, da mais recente para a mais antiga")
    public ResponseEntity<ApiResponse<PageResponse<SentencePracticeResponse>>> history(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<SentencePracticeResponse> result = sentenceService.findHistory(
                SecurityUtils.getCurrentUserId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
