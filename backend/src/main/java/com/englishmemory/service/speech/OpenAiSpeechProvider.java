package com.englishmemory.service.speech;

import com.englishmemory.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.speech.provider", havingValue = "openai", matchIfMissing = true)
@RequiredArgsConstructor
public class OpenAiSpeechProvider implements SpeechProvider {

    private static final String OPENAI_URL = "https://api.openai.com/v1/audio/speech";

    private final RestTemplate restTemplate;

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.tts-model:tts-1}")
    private String model;

    @Value("${app.openai.tts-voice:alloy}")
    private String voice;

    @Override
    public byte[] synthesizeSpeech(String text) {
        log.info("[OpenAiSpeech] Sintetizando áudio para texto de {} caracteres", text.length());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "input", text,
                    "voice", voice,
                    "response_format", "mp3"
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    OPENAI_URL, HttpMethod.POST, entity, byte[].class);

            log.info("[OpenAiSpeech] Áudio sintetizado com sucesso");
            return response.getBody();

        } catch (Exception e) {
            log.error("[OpenAiSpeech] Falha ao sintetizar áudio: {}", e.getMessage());
            throw new BusinessException("Não foi possível gerar o áudio. Tente novamente.");
        }
    }
}
