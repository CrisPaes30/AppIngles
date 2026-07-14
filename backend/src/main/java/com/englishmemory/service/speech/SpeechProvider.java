package com.englishmemory.service.speech;

/**
 * Contrato para síntese de fala (text-to-speech).
 *
 * Implementação ativa (selecionada via app.speech.provider):
 *   openai → OpenAiSpeechProvider (OpenAI TTS)
 */
public interface SpeechProvider {
    byte[] synthesizeSpeech(String text);
}
