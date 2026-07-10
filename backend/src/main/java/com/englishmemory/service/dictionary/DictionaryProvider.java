package com.englishmemory.service.dictionary;

import com.englishmemory.service.dictionary.model.WordDetails;

/**
 * Contrato para enriquecimento automático de palavras.
 *
 * Implementação ativa (selecionada via app.dictionary.provider):
 *   openai → OpenAIDictionaryProvider (GPT-4o-mini)
 */
public interface DictionaryProvider {
    WordDetails enrich(String word);
}
