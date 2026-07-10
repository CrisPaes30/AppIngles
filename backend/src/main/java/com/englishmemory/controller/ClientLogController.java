package com.englishmemory.controller;

import com.englishmemory.dto.request.ClientLogRequest;
import com.englishmemory.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/client-log")
@Tag(name = "Client Log", description = "Recebe diagnósticos de erro do frontend para depuração remota")
public class ClientLogController {

    @PostMapping
    @Operation(summary = "Registra um erro do cliente (frontend) nos logs do backend")
    public ResponseEntity<ApiResponse<Void>> log(@Valid @RequestBody ClientLogRequest request) {
        log.warn("[CLIENT] context={} url={} userAgent={} message={}",
                request.getContext(), request.getUrl(), request.getUserAgent(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(null, "Log registrado"));
    }
}
