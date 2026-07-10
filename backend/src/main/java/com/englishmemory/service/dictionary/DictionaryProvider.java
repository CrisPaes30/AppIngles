package com.englishmemory.service.dictionary;

import com.englishmemory.service.dictionary.model.WordDetails;

/**
 * Contrato para enriquecimento automático de palavras.
 *
 * Implementações (selecionadas via app.dictionary.provider):
 *   mock   → MockDictionaryProvider   (sem chamada externa)
 *   openai → OpenAIDictionaryProvider (GPT-4o-mini)
 */
public interface DictionaryProvider {
    WordDetails enrich(String word);
}
