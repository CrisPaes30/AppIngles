package com.englishmemory.service.impl;

import com.englishmemory.dto.response.DailyProgressResponse;
import com.englishmemory.dto.response.DashboardResponse;
import com.englishmemory.dto.response.TopMistakeWordResponse;
import com.englishmemory.entity.ReviewSchedule;
import com.englishmemory.entity.StudySession;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.repository.*;
import com.englishmemory.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final VocabularyWordRepository vocabularyRepository;
    private final ProgressRepository       progressRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final StudySessionRepository   studySessionRepository;
    private final UserRepository           userRepository;

    private static final Map<String, String> POS_PT = Map.ofEntries(
            Map.entry("NOUN",         "Substantivos"),
            Map.entry("VERB",         "Verbos"),
            Map.entry("ADJECTIVE",    "Adjetivos"),
            Map.entry("ADVERB",       "Advérbios"),
            Map.entry("PREPOSITION",  "Preposições"),
            Map.entry("CONJUNCTION",  "Conjunções"),
            Map.entry("INTERJECTION", "Interjeições"),
            Map.entry("PHRASAL_VERB", "Phrasal Verbs"),
            Map.entry("EXPRESSION",   "Expressões"),
            Map.entry("OTHER",        "Outros")
    );

    @Override
    public DashboardResponse getDashboard(Long userId) {
        LocalDate today = LocalDate.now();

        long totalWords    = vocabularyRepository.countByUserIdAndActiveTrue(userId);
        long learnedWords  = progressRepository.countLearnedWordsByUserId(userId);
        long weakWords     = progressRepository.countWeakWordsByUserId(userId);
        long learningWords = Math.max(0, totalWords - learnedWords - weakWords);
        long toReview      = reviewScheduleRepository.countDueForReview(userId, today);

        Double avgMastery      = progressRepository.findAverageMasteryByUserId(userId);
        Long totalStudyMinutes = studySessionRepository.sumDurationMinutesByUserId(userId);

        int streakDays = userRepository.findById(userId)
                .map(u -> u.getStreakDays() != null ? u.getStreakDays() : 0)
                .orElse(0);

        List<DailyProgressResponse> weeklyChart = buildWeeklyChart(userId, today);

        // ── Métricas avançadas ──────────────────────────────────────────────
        long newWordsThisWeek = vocabularyRepository.countNewWordsSince(
                userId, today.minusDays(6).atStartOfDay());

        long wordsReviewedThisWeek = weeklyChart.stream()
                .mapToLong(DailyProgressResponse::getWordsReviewed)
                .sum();

        long totalCorrect   = reviewScheduleRepository.sumCorrectCountByUserId(userId);
        long totalIncorrect = reviewScheduleRepository.sumIncorrectCountByUserId(userId);
        long totalAttempts  = totalCorrect + totalIncorrect;
        double accuracyPct  = totalAttempts > 0 ? (totalCorrect * 100.0 / totalAttempts) : 0.0;

        List<ReviewSchedule> topMistakes = reviewScheduleRepository
                .findTopMistakeWords(userId, PageRequest.of(0, 5));
        List<TopMistakeWordResponse> topMistakeWords = topMistakes.stream()
                .map(rs -> {
                    VocabularyWord vw = rs.getVocabularyWord();
                    int total = rs.getCorrectCount() + rs.getIncorrectCount();
                    double acc = total > 0 ? (rs.getCorrectCount() * 100.0 / total) : 0.0;
                    return TopMistakeWordResponse.builder()
                            .wordId(vw.getId())
                            .word(vw.getWord())
                            .translation(vw.getTranslation())
                            .partOfSpeech(vw.getPartOfSpeech() != null ? vw.getPartOfSpeech().name() : null)
                            .incorrectCount(rs.getIncorrectCount())
                            .correctCount(rs.getCorrectCount())
                            .totalReviews(total)
                            .accuracyPct(acc)
                            .build();
                })
                .toList();

        List<Object[]> weakPos = reviewScheduleRepository
                .findWeakestPartOfSpeech(userId, PageRequest.of(0, 1));
        String weakestPartOfSpeech = null;
        if (!weakPos.isEmpty() && weakPos.get(0)[0] != null) {
            weakestPartOfSpeech = POS_PT.getOrDefault(weakPos.get(0)[0].toString(),
                    weakPos.get(0)[0].toString());
        }

        // ── Montar resposta ─────────────────────────────────────────────────
        DashboardResponse response = new DashboardResponse();
        response.setTotalWords(totalWords);
        response.setLearnedWords(learnedWords);
        response.setLearningWords(learningWords);
        response.setWeakWords(weakWords);
        response.setWordsToReviewToday(toReview);
        response.setAverageMastery(avgMastery != null ? avgMastery : 0.0);
        response.setTotalStudyMinutes(totalStudyMinutes != null ? totalStudyMinutes : 0L);
        response.setStreakDays(streakDays);
        response.setWeeklyChart(weeklyChart);
        response.setNewWordsThisWeek(newWordsThisWeek);
        response.setWordsReviewedThisWeek(wordsReviewedThisWeek);
        response.setOverallAccuracyPct(accuracyPct);
        response.setWeakestPartOfSpeech(weakestPartOfSpeech);
        response.setTopMistakeWords(topMistakeWords);

        return response;
    }

    private List<DailyProgressResponse> buildWeeklyChart(Long userId, LocalDate today) {
        LocalDateTime from = today.minusDays(6).atStartOfDay();
        LocalDateTime to   = today.atTime(23, 59, 59);

        List<StudySession> sessions =
                studySessionRepository.findAllByUserIdAndStartedAtBetweenAndActiveTrue(userId, from, to);

        Map<LocalDate, List<StudySession>> byDay = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getStartedAt().toLocalDate()));

        List<DailyProgressResponse> chart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day         = today.minusDays(i);
            List<StudySession> ds = byDay.getOrDefault(day, Collections.emptyList());

            DailyProgressResponse point = new DailyProgressResponse();
            point.setDate(day);
            point.setWordsReviewed(ds.stream().mapToInt(StudySession::getWordsReviewed).sum());
            point.setExercisesCompleted(ds.stream().mapToInt(StudySession::getExercisesCompleted).sum());
            point.setStudyMinutes(ds.stream()
                    .mapToInt(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0).sum());
            chart.add(point);
        }

        return chart;
    }
}
