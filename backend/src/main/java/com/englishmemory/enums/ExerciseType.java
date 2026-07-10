package com.englishmemory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExerciseType {
    MULTIPLE_CHOICE("Múltipla Escolha"),
    FILL_BLANK("Completar Lacuna"),
    WORD_ORDER("Ordenar Palavras"),
    TRANSLATION("Tradução"),
    TRUE_FALSE("Verdadeiro ou Falso"),
    SENTENCE_BUILDING("Construção de Frases");

    private final String description;
}
