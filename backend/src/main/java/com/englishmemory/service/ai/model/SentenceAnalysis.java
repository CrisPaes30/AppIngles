package com.englishmemory.service.ai.model;

import java.util.List;

/**
 * Resultado da análise de frase retornado pela camada de IA.
 * Não é exposto diretamente na API.
 */
public record SentenceAnalysis(
        String correctedSentence,
        String aiFeedback,
        String grammarExplanation,
        List<String> suggestedSentences,
        List<String> newVocabularyFound,
        int score
) {}
