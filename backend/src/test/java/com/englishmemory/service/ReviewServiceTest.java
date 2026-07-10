package com.englishmemory.service;

import com.englishmemory.dto.request.ReviewAnswerRequest;
import com.englishmemory.entity.Progress;
import com.englishmemory.entity.ReviewSchedule;
import com.englishmemory.entity.User;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.repository.ProgressRepository;
import com.englishmemory.repository.ReviewScheduleRepository;
import com.englishmemory.repository.StudySessionRepository;
import com.englishmemory.repository.UserRepository;
import com.englishmemory.repository.VocabularyWordRepository;
import com.englishmemory.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService — testes unitários")
class ReviewServiceTest {

    @Mock private ReviewScheduleRepository reviewScheduleRepository;
    @Mock private VocabularyWordRepository  vocabularyRepository;
    @Mock private ProgressRepository        progressRepository;
    @Mock private UserRepository            userRepository;
    @Mock private StudySessionRepository    studySessionRepository;

    @InjectMocks private ReviewServiceImpl reviewService;

    private User           user;
    private VocabularyWord word;
    private ReviewSchedule schedule;
    private Progress       progress;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setStreakDays(5);
        user.setLastStudyDate(LocalDate.now().minusDays(1));

        word = new VocabularyWord();
        word.setId(10L);
        word.setWord("ephemeral");
        word.setTranslation("efêmero");
        word.setUser(user);

        schedule = new ReviewSchedule();
        schedule.setId(20L);
        schedule.setVocabularyWord(word);
        schedule.setEaseFactor(new BigDecimal("2.5"));
        schedule.setRepetitions(3);
        schedule.setIntervalDays(6);
        schedule.setNextReviewDate(LocalDate.now());
        schedule.setCorrectCount(6);
        schedule.setIncorrectCount(1);

        progress = new Progress();
        progress.setId(30L);
        progress.setVocabularyWord(word);
        progress.setUser(user);
        progress.setMasteryLevel(70);
    }

    @Nested
    @DisplayName("getTodayReviews")
    class GetTodayReviews {

        @Test
        @DisplayName("retorna palavras com revisão pendente para hoje ou antes")
        void getTodayReviews_returnsDueSchedules() {
            when(reviewScheduleRepository.findDueForReview(anyLong(), any(LocalDate.class), any()))
                    .thenReturn(List.of(schedule));
            when(progressRepository.findByUserIdAndVocabularyWordIdAndActiveTrue(anyLong(), anyLong()))
                    .thenReturn(Optional.of(progress));

            reviewService.getTodayReviews(1L);

            verify(reviewScheduleRepository).findDueForReview(eq(1L), eq(LocalDate.now()), any());
        }
    }

    @Nested
    @DisplayName("submitAnswer — lógica de streak")
    class SubmitAnswerStreak {

        @BeforeEach
        void mockDependencies() {
            when(vocabularyRepository.findByIdAndUserIdAndActiveTrue(10L, 1L))
                    .thenReturn(Optional.of(word));
            when(reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(10L))
                    .thenReturn(Optional.of(schedule));
            when(progressRepository.findByUserIdAndVocabularyWordIdAndActiveTrue(1L, 10L))
                    .thenReturn(Optional.of(progress));
            when(progressRepository.save(any())).thenReturn(progress);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(studySessionRepository.findAllByUserIdAndStartedAtBetweenAndActiveTrue(anyLong(), any(), any()))
                    .thenReturn(List.of());
        }

        @Test
        @DisplayName("último estudo ontem → streak incrementa")
        void submitAnswer_lastStudiedYesterday_incrementsStreak() {
            user.setLastStudyDate(LocalDate.now().minusDays(1));
            user.setStreakDays(5);

            ReviewAnswerRequest request = new ReviewAnswerRequest();
            request.setQuality(4);

            reviewService.submitAnswer(1L, 10L, request);

            assertThat(user.getStreakDays()).isEqualTo(6);
            assertThat(user.getLastStudyDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("último estudo hoje → streak não muda")
        void submitAnswer_lastStudiedToday_streakUnchanged() {
            user.setLastStudyDate(LocalDate.now());
            user.setStreakDays(5);

            ReviewAnswerRequest request = new ReviewAnswerRequest();
            request.setQuality(5);

            reviewService.submitAnswer(1L, 10L, request);

            assertThat(user.getStreakDays()).isEqualTo(5);
        }

        @Test
        @DisplayName("último estudo há 2+ dias → streak reseta para 1")
        void submitAnswer_lastStudied2DaysAgo_resetsStreak() {
            user.setLastStudyDate(LocalDate.now().minusDays(2));
            user.setStreakDays(10);

            ReviewAnswerRequest request = new ReviewAnswerRequest();
            request.setQuality(3);

            reviewService.submitAnswer(1L, 10L, request);

            assertThat(user.getStreakDays()).isEqualTo(1);
            assertThat(user.getLastStudyDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("resposta correta (quality >= 3) → incrementa correctCount")
        void submitAnswer_correctQuality_incrementsCorrectCount() {
            schedule.setCorrectCount(6);

            ReviewAnswerRequest request = new ReviewAnswerRequest();
            request.setQuality(4);

            reviewService.submitAnswer(1L, 10L, request);

            assertThat(schedule.getCorrectCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("resposta errada (quality < 3) → incrementa incorrectCount e reseta SM-2")
        void submitAnswer_incorrectQuality_incrementsIncorrectCountAndResets() {
            schedule.setRepetitions(3);
            schedule.setIncorrectCount(1);

            ReviewAnswerRequest request = new ReviewAnswerRequest();
            request.setQuality(0);

            reviewService.submitAnswer(1L, 10L, request);

            assertThat(schedule.getIncorrectCount()).isEqualTo(2);
            assertThat(schedule.getRepetitions()).isEqualTo(0);
            assertThat(schedule.getNextReviewDate()).isEqualTo(LocalDate.now());
        }
    }

    @Nested
    @DisplayName("submitAnswer — erros")
    class SubmitAnswerErrors {

        @Test
        @DisplayName("palavra não encontrada lança ResourceNotFoundException")
        void submitAnswer_wordNotFound_throwsException() {
            when(vocabularyRepository.findByIdAndUserIdAndActiveTrue(999L, 1L))
                    .thenReturn(Optional.empty());

            ReviewAnswerRequest request = new ReviewAnswerRequest();
            request.setQuality(5);

            assertThatThrownBy(() -> reviewService.submitAnswer(1L, 999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
