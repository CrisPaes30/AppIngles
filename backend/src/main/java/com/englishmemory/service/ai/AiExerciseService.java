package com.englishmemory.service.ai;

import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.enums.ExerciseType;
import com.englishmemory.service.ai.model.GeneratedExercise;
import com.englishmemory.service.ai.model.SentenceAnalysis;

/**
 * Contrato da camada de IA.
 *
 * Implementação ativa (selecionada via app.ai.provider):
 *   openai → OpenAiExerciseService
 *
 * As regras de negócio em ExerciseServiceImpl e SentenceServiceImpl
 * nunca dependem de qual provider está ativo.
 */
public interface AiExerciseService {

    GeneratedExercise generateExercise(VocabularyWord word, ExerciseType type);

    SentenceAnalysis analyzeSentence(String sentence, String wordContext);
}
