package com.englishmemory.controller;

import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.ProgressSummaryResponse;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Acompanhamento da evolução do aprendizado")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    @Operation(
            summary = "Resumo do progresso",
            description = "Retorna visão consolidada do aprendizado: mastery médio, " +
                          "distribuição por nível CEFR e por categoria"
    )
    public ResponseEntity<ApiResponse<ProgressSummaryResponse>> getSummary() {
        ProgressSummaryResponse response = progressService.getSummary(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
