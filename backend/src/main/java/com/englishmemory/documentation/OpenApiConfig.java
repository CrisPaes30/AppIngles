package com.englishmemory.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("English Memory AI API")
                        .description("Plataforma inteligente para aprendizagem de inglês")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("English Memory AI")
                                .email("contato@englishmemory.ai"))
                        .license(new License().name("Private")))
                .servers(List.of(
                        new Server().url("/api").description("Servidor atual (" + activeProfile + ")")
                ));
    }
}
