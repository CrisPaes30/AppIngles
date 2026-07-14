package com.englishmemory.service;

import com.englishmemory.dto.request.AnswerExerciseRequest;
import com.englishmemory.dto.request.GenerateExerciseRequest;
import com.englishmemory.dto.response.ExerciseAnswerResponse;
import com.englishmemory.entity.Exercise;
import com.englishmemory.entity.User;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.enums.ExerciseType;
import com.englishmemory.repository.ExerciseAttemptRepository;
import com.englishmemory.repository.ExerciseRepository;
import com.englishmemory.repository.ProgressRepository;
import com.englishmemory.repository.UserRepository;
import com.englishmemory.repository.VocabularyWordRepository;
import com.englishmemory.service.ai.AiExerciseService;
import com.englishmemory.service.ai.model.GeneratedExercise;
import com.englishmemory.service.impl.ExerciseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseService — testes unitários")
class ExerciseServiceTest {

    @Mock private ExerciseRepository        exerciseRepository;
    @Mock private ExerciseAttemptRepository attemptRepository;
    @Mock private VocabularyWordRepository  vocabularyRepository;
    @Mock private ProgressRepository        progressRepository;
    @Mock private UserRepository            userRepository;
    @Mock private AiExerciseService         aiService;

    @InjectMocks private ExerciseServiceImpl exerciseService;

    private User user;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        lenient().when(attemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Exercise exerciseOfType(ExerciseType type, String correctAnswer) {
        Exercise ex = Exercise.builder()
                .user(user)
                .type(type)
                .question("q")
                .correctAnswer(correctAnswer)
                .build();
        ex.setId(100L);
        when(exerciseRepository.findByIdAndUserIdAndActiveTrue(100L, 1L))
                .thenReturn(Optional.of(ex));
        return ex;
    }

    @Nested
    @DisplayName("answer — WORD_ORDER")
    class WordOrder {

        @Test
        @DisplayName("resposta montada a partir dos tiles (sem pontuação) é considerada correta mesmo com correctAnswer pontuado")
        void answer_wordOrderWithoutPunctuation_matchesPunctuatedCorrectAnswer() {
            exerciseOfType(ExerciseType.WORD_ORDER, "I am at the park.");

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("I am at the park");

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isTrue();
        }

        @Test
        @DisplayName("ordem errada continua incorreta")
        void answer_wordOrderWrongOrder_isIncorrect() {
            exerciseOfType(ExerciseType.WORD_ORDER, "I am at the park.");

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("park the at am I");

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isFalse();
        }
    }

    @Nested
    @DisplayName("answer — normalização geral")
    class Normalization {

        @ParameterizedTest(name = "correctAnswer=''{0}'' userAnswer=''{1}''")
        @CsvSource({
                "'Hello, world!',    hello world",
                "Do not stop.,       do not stop",
                "café,               cafe",
                "It's fine.,         Its fine"
        })
        @DisplayName("FILL_BLANK ignora pontuação, acentos e caixa")
        void answer_fillBlank_ignoresPunctuationAndDiacritics(String correctAnswer, String userAnswer) {
            exerciseOfType(ExerciseType.FILL_BLANK, correctAnswer);

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer(userAnswer);

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isTrue();
        }

        @Test
        @DisplayName("TRANSLATION ignora pontuação e acentos na comparação bidirecional")
        void answer_translation_ignoresPunctuationAndDiacritics() {
            exerciseOfType(ExerciseType.TRANSLATION, "Eu estou no parque.");

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("eu estou no parque");

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isTrue();
        }
    }

    @Nested
    @DisplayName("generate — resolveWord (seleção automática de palavra)")
    class ResolveWord {

        @Test
        @DisplayName("quando o pool de fracas está vazio, cai no pool geral sem lançar exceção")
        void generate_fallsBackToGeneralPoolWhenWeakPoolIsEmpty() {
            VocabularyWord word = new VocabularyWord();
            word.setId(5L);
            word.setWord("run");
            word.setUser(user);

            GenerateExerciseRequest request = new GenerateExerciseRequest();
            request.setType(ExerciseType.FILL_BLANK);

            lenient().when(vocabularyRepository.findGenuinelyWeakWordsByUserId(eq(1L), anyInt(), any()))
                    .thenReturn(List.of());
            when(vocabularyRepository.findAllByUserIdAndActiveTrue(eq(1L), any()))
                    .thenReturn(new PageImpl<>(List.of(word)));

            when(aiService.generateExercise(any(), any()))
                    .thenReturn(new GeneratedExercise("q", null, "a", "expl"));
            when(exerciseRepository.save(any())).thenAnswer(invocation -> {
                Exercise ex = invocation.getArgument(0);
                ex.setId(200L);
                return ex;
            });

            assertThatCode(() -> exerciseService.generate(1L, request)).doesNotThrowAnyException();

            verify(vocabularyRepository).findAllByUserIdAndActiveTrue(eq(1L), any());
        }
    }
}
