package com.englishmemory.service.impl;

import com.englishmemory.dto.request.ReviewAnswerRequest;
import com.englishmemory.dto.response.ReviewCardResponse;
import com.englishmemory.dto.response.ReviewResultResponse;
import com.englishmemory.entity.*;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.repository.*;
import com.englishmemory.service.ReviewService;
import com.englishmemory.enums.SessionType;
import com.englishmemory.util.JsonListConverter;
import com.englishmemory.util.Sm2Algorithm;
import com.englishmemory.util.Sm2Algorithm.Sm2Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final int    MAX_REVIEWS_PER_SESSION = 50;
    private static final String FEEDBACK_AGAIN    = "Continue praticando! A repetição vai fixar essa palavra.";
    private static final String FEEDBACK_HARD     = "Você lembrou! Com mais revisões vai ficar mais fácil.";
    private static final String FEEDBACK_GOOD     = "Ótimo trabalho! Continue assim.";
    private static final String FEEDBACK_EASY     = "Excelente! Você está dominando essa palavra.";
    private static final String FEEDBACK_PERFECT  = "Perfeito! Você domina completamente essa palavra.";

    private final VocabularyWordRepository vocabularyRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ProgressRepository       progressRepository;
    private final UserRepository           userRepository;
    private final StudySessionRepository   studySessionRepository;

    @Override
    public List<ReviewCardResponse> getTodayReviews(Long userId) {
        LocalDate today   = LocalDate.now();
        var       pageable = PageRequest.of(0, MAX_REVIEWS_PER_SESSION);

        return reviewScheduleRepository
                .findDueForReview(userId, today, pageable)
                .stream()
                .map(schedule -> toReviewCard(userId, schedule))
                .toList();
    }

    @Override
    public long countTodayReviews(Long userId) {
        return reviewScheduleRepository.countDueForReview(userId, LocalDate.now());
    }

    @Override
    @Transactional
    public ReviewResultResponse submitAnswer(Long userId, Long wordId, ReviewAnswerRequest request) {
        VocabularyWord word = vocabularyRepository.findByIdAndUserIdAndActiveTrue(wordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Palavra", wordId));

        ReviewSchedule schedule = reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(wordId)
                .orElseThrow(() -> new BusinessException(
                        "Nenhum agendamento de revisão encontrado para a palavra id=" + wordId));

        int     quality   = request.getQuality();
        boolean isCorrect = quality >= Sm2Algorithm.QUALITY_THRESHOLD;

        Sm2Result result = Sm2Algorithm.calculate(
                schedule.getEaseFactor().doubleValue(),
                schedule.getRepetitions(),
                schedule.getIntervalDays(),
                quality
        );

        updateSchedule(schedule, result, isCorrect);

        Progress progress = updateProgress(userId, word, schedule, isCorrect);

        updateUserStreak(userId);

        updateStudySession(userId, isCorrect);

        log.info("Revisão da palavra '{}' (id={}) pelo usuário {}: quality={}, isCorrect={}, nextReview={}",
                word.getWord(), wordId, userId, quality, isCorrect, schedule.getNextReviewDate());

        return ReviewResultResponse.builder()
                .vocabularyWordId(wordId)
                .word(word.getWord())
                .isCorrect(isCorrect)
                .quality(quality)
                .newRepetitions(result.repetitions())
                .newIntervalDays(result.intervalDays())
                .newEaseFactor(result.easeFactor())
                .masteryLevel(progress.getMasteryLevel())
                .nextReviewDate(schedule.getNextReviewDate())
                .feedbackMessage(buildFeedback(quality))
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void updateStudySession(Long userId, boolean isCorrect) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to   = today.atTime(23, 59, 59);

        StudySession session = studySessionRepository
                .findAllByUserIdAndStartedAtBetweenAndActiveTrue(userId, from, to)
                .stream()
                .filter(s -> SessionType.REVIEW.equals(s.getSessionType()))
                .findFirst()
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
                    return StudySession.builder()
                            .user(user)
                            .sessionType(SessionType.REVIEW)
                            .startedAt(LocalDateTime.now())
                            .build();
                });

        session.setWordsReviewed(session.getWordsReviewed() + 1);
        if (isCorrect) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        } else {
            session.setIncorrectAnswers(session.getIncorrectAnswers() + 1);
        }

        studySessionRepository.save(session);
    }

    private void updateSchedule(ReviewSchedule schedule, Sm2Result result, boolean isCorrect) {
        schedule.setEaseFactor(BigDecimal.valueOf(result.easeFactor()));
        schedule.setRepetitions(result.repetitions());
        schedule.setIntervalDays(result.intervalDays());
        schedule.setLastReviewedAt(LocalDateTime.now());

        if (isCorrect) {
            schedule.setCorrectCount(schedule.getCorrectCount() + 1);
            schedule.setNextReviewDate(LocalDate.now().plusDays(result.intervalDays()));
        } else {
            schedule.setIncorrectCount(schedule.getIncorrectCount() + 1);
            // Falhou: agendar para revisão no mesmo dia
            schedule.setNextReviewDate(LocalDate.now());
        }

        reviewScheduleRepository.save(schedule);
    }

    private Progress updateProgress(Long userId, VocabularyWord word, ReviewSchedule schedule, boolean isCorrect) {
        Progress progress = progressRepository
                .findByUserIdAndVocabularyWordIdAndActiveTrue(userId, word.getId())
                .orElseGet(() -> Progress.builder()
                        .user(word.getUser())
                        .vocabularyWord(word)
                        .totalReviews(0)
                        .correctReviews(0)
                        .masteryLevel(0)
                        .build());

        progress.setTotalReviews(progress.getTotalReviews() + 1);
        if (isCorrect) {
            progress.setCorrectReviews(progress.getCorrectReviews() + 1);
        }

        int mastery = Sm2Algorithm.calculateMastery(
                schedule.getCorrectCount(),
                schedule.getIncorrectCount(),
                schedule.getRepetitions()
        );
        progress.setMasteryLevel(mastery);
        progress.setLastActivityDate(LocalDate.now());

        return progressRepository.save(progress);
    }

    private void updateUserStreak(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            LocalDate today = LocalDate.now();

            if (user.getLastStudyDate() == null) {
                user.setStreakDays(1);
            } else if (user.getLastStudyDate().equals(today)) {
                // já estudou hoje — não altera streak
                return;
            } else if (user.getLastStudyDate().equals(today.minusDays(1))) {
                user.setStreakDays(user.getStreakDays() + 1);
            } else {
                // quebrou a sequência
                user.setStreakDays(1);
            }

            user.setLastStudyDate(today);
            userRepository.save(user);
        });
    }

    private ReviewCardResponse toReviewCard(Long userId, ReviewSchedule schedule) {
        VocabularyWord word = schedule.getVocabularyWord();

        int accuracy = 0;
        int total    = schedule.getCorrectCount() + schedule.getIncorrectCount();
        if (total > 0) {
            accuracy = (int) Math.round(schedule.getCorrectCount() * 100.0 / total);
        }

        int masteryLevel = progressRepository
                .findByUserIdAndVocabularyWordIdAndActiveTrue(userId, word.getId())
                .map(Progress::getMasteryLevel)
                .orElse(0);

        return ReviewCardResponse.builder()
                .vocabularyWordId(word.getId())
                .word(word.getWord())
                .translation(word.getTranslation())
                .pronunciation(word.getPronunciation())
                .ipa(word.getIpa())
                .partOfSpeech(word.getPartOfSpeech())
                .cefrLevel(word.getCefrLevel())
                .examples(JsonListConverter.fromJson(word.getExamples()))
                .synonyms(JsonListConverter.fromJson(word.getSynonyms()))
                .categoryName(word.getCategory() != null ? word.getCategory().getName() : null)
                .repetitions(schedule.getRepetitions())
                .intervalDays(schedule.getIntervalDays())
                .correctCount(schedule.getCorrectCount())
                .incorrectCount(schedule.getIncorrectCount())
                .accuracyPercentage(accuracy)
                .masteryLevel(masteryLevel)
                .nextReviewDate(schedule.getNextReviewDate())
                .lastReviewedAt(schedule.getLastReviewedAt())
                .build();
    }

    private String buildFeedback(int quality) {
        return switch (quality) {
            case 0, 1 -> FEEDBACK_AGAIN;
            case 2    -> FEEDBACK_HARD;
            case 3    -> FEEDBACK_GOOD;
            case 4    -> FEEDBACK_EASY;
            default   -> FEEDBACK_PERFECT;
        };
    }
}
