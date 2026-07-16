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
import com.englishmemory.service.ai.model.SentenceAnalysis;
import com.englishmemory.service.impl.ExerciseServiceImpl;
import com.englishmemory.service.speech.SpeechProvider;
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
import static org.mockito.Mockito.never;
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
    @Mock private SpeechProvider            speechProvider;

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

        @Test
        @DisplayName("LISTENING (dictation) ignora pontuação e apóstrofo, igual FILL_BLANK")
        void answer_listening_ignoresPunctuationAndApostrophe() {
            exerciseOfType(ExerciseType.LISTENING, "I'm going to the store.");

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("im going to the store");

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

    @Nested
    @DisplayName("generate — LISTENING (TTS)")
    class Listening {

        @Test
        @DisplayName("chama o SpeechProvider e preenche audioDataUri com o áudio codificado em base64")
        void generate_listening_callsSpeechProviderAndPopulatesAudioDataUri() {
            VocabularyWord word = new VocabularyWord();
            word.setId(5L);
            word.setWord("store");
            word.setUser(user);

            GenerateExerciseRequest request = new GenerateExerciseRequest();
            request.setType(ExerciseType.LISTENING);
            request.setVocabularyWordId(5L);

            when(vocabularyRepository.findByIdAndUserIdAndActiveTrue(5L, 1L))
                    .thenReturn(Optional.of(word));

            when(aiService.generateExercise(any(), any()))
                    .thenReturn(new GeneratedExercise(
                            "Ouça o áudio e escreva a frase que você ouviu.", null,
                            "I'm going to the store.", "expl"));

            when(exerciseRepository.save(any())).thenAnswer(invocation -> {
                Exercise ex = invocation.getArgument(0);
                ex.setId(300L);
                return ex;
            });

            byte[] fakeAudio = new byte[] {10, 20, 30};
            when(speechProvider.synthesizeSpeech("I'm going to the store.")).thenReturn(fakeAudio);

            var response = exerciseService.generate(1L, request);

            assertThat(response.getAudioDataUri()).startsWith("data:audio/mpeg;base64,");
            String base64Part = response.getAudioDataUri()
                    .substring("data:audio/mpeg;base64,".length());
            assertThat(java.util.Base64.getDecoder().decode(base64Part)).isEqualTo(fakeAudio);
        }

        @Test
        @DisplayName("outros tipos não chamam o SpeechProvider")
        void generate_nonListeningType_doesNotCallSpeechProvider() {
            VocabularyWord word = new VocabularyWord();
            word.setId(5L);
            word.setWord("store");
            word.setUser(user);

            GenerateExerciseRequest request = new GenerateExerciseRequest();
            request.setType(ExerciseType.FILL_BLANK);
            request.setVocabularyWordId(5L);

            when(vocabularyRepository.findByIdAndUserIdAndActiveTrue(5L, 1L))
                    .thenReturn(Optional.of(word));

            when(aiService.generateExercise(any(), any()))
                    .thenReturn(new GeneratedExercise("q", null, "store", "expl"));

            when(exerciseRepository.save(any())).thenAnswer(invocation -> {
                Exercise ex = invocation.getArgument(0);
                ex.setId(301L);
                return ex;
            });

            var response = exerciseService.generate(1L, request);

            assertThat(response.getAudioDataUri()).isNull();
            verify(speechProvider, never()).synthesizeSpeech(any());
        }
    }

    @Nested
    @DisplayName("answer — SENTENCE_BUILDING (validação via IA)")
    class SentenceBuilding {

        private Exercise exerciseWithTargetWord(String word) {
            VocabularyWord vw = new VocabularyWord();
            vw.setId(7L);
            vw.setWord(word);
            vw.setUser(user);

            Exercise ex = Exercise.builder()
                    .user(user)
                    .type(ExerciseType.SENTENCE_BUILDING)
                    .question("q")
                    .vocabularyWord(vw)
                    .explanation("Um exemplo de frase seria: ...")
                    .build();
            ex.setId(100L);
            when(exerciseRepository.findByIdAndUserIdAndActiveTrue(100L, 1L))
                    .thenReturn(Optional.of(ex));
            return ex;
        }

        @Test
        @DisplayName("frase com erro gramatical e nota baixa é marcada incorreta, mesmo usando a palavra pedida")
        void answer_lowScoreSentence_isIncorrectDespiteUsingTargetWord() {
            exerciseWithTargetWord("each other");

            when(aiService.analyzeSentence(eq("When your meet each other"), eq("each other")))
                    .thenReturn(new SentenceAnalysis(
                            "When you meet each other.",
                            "Sua frase tem um erro de gramática.",
                            "'Your' deveria ser 'you' aqui.",
                            List.of(), List.of(), 40));

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("When your meet each other");

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isFalse();
            assertThat(response.getExplanation()).contains("Sugestão").contains("When you meet each other.");
        }

        @Test
        @DisplayName("frase com nota alta (erros leves) é considerada correta e ainda traz sugestão")
        void answer_highScoreSentenceWithMinorIssue_isCorrectAndSuggestsImprovement() {
            exerciseWithTargetWord("store");

            when(aiService.analyzeSentence(eq("I go to the store yesterday"), eq("store")))
                    .thenReturn(new SentenceAnalysis(
                            "I went to the store yesterday.",
                            "Muito bem! Só um pequeno deslize de tempo verbal.",
                            "Use o passado 'went' em vez de 'go' para uma ação ontem.",
                            List.of(), List.of(), 78));

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("I go to the store yesterday");

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isTrue();
            assertThat(response.getExplanation()).contains("Sugestão").contains("I went to the store yesterday.");
        }

        @Test
        @DisplayName("frase que não usa a palavra pedida é incorreta sem nem chamar a análise de nota")
        void answer_sentenceWithoutTargetWord_isIncorrect() {
            exerciseWithTargetWord("party");

            AnswerExerciseRequest request = new AnswerExerciseRequest();
            request.setAnswer("I like ice cream");

            ExerciseAnswerResponse response = exerciseService.answer(1L, 100L, request);

            assertThat(response.getIsCorrect()).isFalse();
            assertThat(response.getExplanation()).contains("party");
            verify(aiService, never()).analyzeSentence(any(), any());
        }
    }
}
