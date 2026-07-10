package com.englishmemory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartOfSpeech {
    NOUN("Substantivo"),
    VERB("Verbo"),
    ADJECTIVE("Adjetivo"),
    ADVERB("Advérbio"),
    PREPOSITION("Preposição"),
    CONJUNCTION("Conjunção"),
    INTERJECTION("Interjeição"),
    PHRASAL_VERB("Phrasal Verb"),
    EXPRESSION("Expressão"),
    OTHER("Outro");

    private final String description;
}
