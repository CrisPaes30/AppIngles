package com.englishmemory.service.ai.model;

import java.util.List;

/**
 * Modelo interno retornado pela camada de IA.
 * Não é exposto diretamente na API — o service converte para ExerciseResponse/ExerciseAnswerResponse.
 */
public record GeneratedExercise(
        String question,
        List<String> options,       // null para tipos sem alternativas
        String correctAnswer,       // null para SENTENCE_BUILDING
        String explanation
) {}
