package com.englishmemory.controller;

import com.englishmemory.dto.request.GoogleAuthRequest;
import com.englishmemory.dto.response.ApiResponse;
import com.englishmemory.dto.response.AuthResponse;
import com.englishmemory.entity.User;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.repository.UserRepository;
import com.englishmemory.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil        jwtUtil;
    private final RestTemplate   restTemplate;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request) {

        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();

        @SuppressWarnings("unchecked")
        Map<String, String> payload;
        try {
            payload = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.warn("Falha ao validar token Google: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google inválido");
        }

        if (payload == null || payload.get("email") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google não contém e-mail");
        }

        if (!"true".equals(payload.get("email_verified"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail Google não verificado");
        }

        // Sem checar o "aud", qualquer ID token Google válido (de qualquer app OAuth,
        // não só o nosso) seria aceito aqui — a chamada ao tokeninfo só prova que o
        // token é autêntico, não que foi emitido para ESTE aplicativo.
        if (googleClientId == null || googleClientId.isBlank() || "configure-no-env".equals(googleClientId)) {
            log.error("GOOGLE_CLIENT_ID não configurado — login Google recusado por segurança");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login com Google não configurado");
        }
        if (!googleClientId.equals(payload.get("aud"))) {
            log.warn("Token Google rejeitado: aud não corresponde ao client-id configurado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google não pertence a este aplicativo");
        }

        String email = payload.get("email");
        String name  = payload.getOrDefault("name", email);

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseGet(() -> {
                    User novo = User.builder()
                            .email(email)
                            .name(name)
                            .cefrLevel(CefrLevel.A1)
                            .timezone("America/Sao_Paulo")
                            .streakDays(0)
                            .build();
                    User salvo = userRepository.save(novo);
                    log.info("Novo usuário criado via Google OAuth: {}", email);
                    return salvo;
                });

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        AuthResponse authResponse = new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login realizado com sucesso"));
    }
}
