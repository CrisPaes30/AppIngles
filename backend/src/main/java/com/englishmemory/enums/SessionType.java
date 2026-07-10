package com.englishmemory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionType {
    REVIEW("Revisão"),
    EXERCISE("Exercícios"),
    CONVERSATION("Conversação com IA"),
    READING("Leitura"),
    MIXED("Sessão Mista");

    private final String description;
}
