package com.englishmemory.controller;

import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.DashboardResponse;
import com.englishmemory.security.SecurityUtils;
import com.englishmemory.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Visão geral do progresso do usuário")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(
            summary = "Obter dados do dashboard",
            description = "Retorna estatísticas gerais, progresso semanal (últimos 7 dias), " +
                          "contagem de palavras por status e streak de estudos"
    )
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse response = dashboardService.getDashboard(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
