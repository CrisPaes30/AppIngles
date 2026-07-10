package com.englishmemory.controller;

import com.englishmemory.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Verificação de saúde da aplicação")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check da aplicação")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "UP", "application", "English Memory AI"),
                "Aplicação rodando com sucesso"
        ));
    }
}
