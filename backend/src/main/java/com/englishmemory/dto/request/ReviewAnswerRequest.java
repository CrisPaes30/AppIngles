package com.englishmemory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Resposta de revisão. Use os valores: 0=Errei (AGAIN), 3=Difícil (HARD), 4=Bom (GOOD), 5=Fácil (EASY)")
public class ReviewAnswerRequest {

    @NotNull(message = "A qualidade da resposta é obrigatória")
    @Min(value = 0, message = "Qualidade mínima é 0 (AGAIN)")
    @Max(value = 5, message = "Qualidade máxima é 5 (EASY)")
    @Schema(
            description = "Qualidade da resposta no algoritmo SM-2",
            example = "4",
            allowableValues = {"0", "1", "2", "3", "4", "5"}
    )
    private Integer quality;
}
