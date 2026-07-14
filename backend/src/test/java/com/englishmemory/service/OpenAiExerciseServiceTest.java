package com.englishmemory.service;

import com.englishmemory.entity.User;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.ExerciseType;
import com.englishmemory.service.ai.OpenAiExerciseService;
import com.englishmemory.service.ai.model.GeneratedExercise;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiExerciseService — testes unitários")
class OpenAiExerciseServiceTest {

    @Mock private RestTemplate restTemplate;

    private OpenAiExerciseService service;

    private VocabularyWord word;

    @BeforeEach
    void setUp() {
        service = new OpenAiExerciseService(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        ReflectionTestUtils.setField(service, "model", "gpt-4o-mini");

        word = VocabularyWord.builder()
                .word("at")
                .translation("em")
                .cefrLevel(CefrLevel.A1)
                .user(User.builder().build())
                .build();
    }

    @Test
    @DisplayName("WORD_ORDER: ignora as options devolvidas pelo modelo e deriva os tiles do correctAnswer")
    void generateExercise_wordOrder_derivesOptionsFromCorrectAnswer() {
        // A IA devolveu "options" que são frases inteiras já montadas — nenhuma
        // delas reconstrói o correctAnswer, tornando o exercício impossível de
        // acertar (bug real reportado por usuário).
        String openAiResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"question\\":\\"Ordene as palavras\\",\\"options\\":[\\"The party at I am\\",\\"At the party I am\\"],\\"correctAnswer\\":\\"I am at the party.\\",\\"explanation\\":\\"...\\"}"
                      }
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(openAiResponse));

        GeneratedExercise result = service.generateExercise(word, ExerciseType.WORD_ORDER);

        assertThat(result.correctAnswer()).isEqualTo("I am at the party.");
        assertThat(result.options())
                .containsExactlyInAnyOrder("I", "am", "at", "the", "party.");
    }
}
