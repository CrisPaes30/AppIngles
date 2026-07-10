package com.englishmemory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class AnswerExerciseRequest {

    @NotBlank(message = "A resposta não pode ser vazia")
    private String answer;

    @PositiveOrZero
    private Integer timeSpentSeconds = 0;
}
