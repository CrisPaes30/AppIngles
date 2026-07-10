package com.englishmemory.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Recusa subir em produção com o segredo JWT padrão de desenvolvimento ou
 * curto demais para HS256 (mínimo 256 bits exigido por jjwt). Falhar no
 * startup é preferível a assinar tokens com uma chave fraca ou conhecida.
 */
@Slf4j
@Component
@Profile("prod")
public class ProdStartupGuard {

    private static final String DEV_DEFAULT_SECRET = "dev-secret-key-mude-em-producao-minimo-256bits-xpto";
    private static final int MIN_SECRET_BYTES = 32;

    private final String jwtSecret;

    public ProdStartupGuard(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @PostConstruct
    public void validate() {
        if (jwtSecret == null || jwtSecret.isBlank() || DEV_DEFAULT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException(
                    "JWT_SECRET não configurado ou usando o valor padrão de desenvolvimento. "
                            + "Gere um novo com: openssl rand -base64 48");
        }
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET curto demais (" + jwtSecret.getBytes(StandardCharsets.UTF_8).length
                            + " bytes). São exigidos no mínimo " + MIN_SECRET_BYTES + " bytes (256 bits).");
        }
        log.info("JWT_SECRET validado para produção.");
    }
}
