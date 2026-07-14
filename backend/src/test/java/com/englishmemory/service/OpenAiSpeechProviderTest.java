package com.englishmemory.service;

import com.englishmemory.exception.BusinessException;
import com.englishmemory.service.speech.OpenAiSpeechProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiSpeechProvider — testes unitários")
class OpenAiSpeechProviderTest {

    @Mock private RestTemplate restTemplate;

    private OpenAiSpeechProvider provider;

    @BeforeEach
    void setUp() {
        provider = new OpenAiSpeechProvider(restTemplate);
        ReflectionTestUtils.setField(provider, "apiKey", "test-key");
        ReflectionTestUtils.setField(provider, "model", "tts-1");
        ReflectionTestUtils.setField(provider, "voice", "alloy");
    }

    @Test
    @DisplayName("caso feliz: devolve os bytes de áudio retornados pela OpenAI")
    void synthesizeSpeech_returnsAudioBytes() {
        byte[] fakeAudio = new byte[] {1, 2, 3, 4};

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(fakeAudio));

        byte[] result = provider.synthesizeSpeech("I am going to the store.");

        assertThat(result).isEqualTo(fakeAudio);
    }

    @Test
    @DisplayName("erro HTTP da OpenAI vira BusinessException")
    void synthesizeSpeech_httpError_throwsBusinessException() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null));

        assertThatThrownBy(() -> provider.synthesizeSpeech("I am going to the store."))
                .isInstanceOf(BusinessException.class);
    }
}
