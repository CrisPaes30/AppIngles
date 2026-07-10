package com.englishmemory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalyzeSentenceRequest {

    @NotBlank(message = "A frase é obrigatória")
    @Size(max = 2000, message = "A frase deve ter no máximo 2000 caracteres")
    private String sentence;

    private Long vocabularyWordId;
}
