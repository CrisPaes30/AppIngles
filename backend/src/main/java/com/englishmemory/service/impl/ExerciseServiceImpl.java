package com.englishmemory.service.impl;

import com.englishmemory.dto.request.AnswerExerciseRequest;
import com.englishmemory.dto.request.GenerateExerciseRequest;
import com.englishmemory.dto.response.ExerciseAnswerResponse;
import com.englishmemory.dto.response.ExerciseResponse;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.entity.*;
import com.englishmemory.enums.ExerciseType;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.repository.*;
import com.englishmemory.service.ExerciseService;
import com.englishmemory.service.ai.AiExerciseService;
import com.englishmemory.service.ai.model.GeneratedExercise;
import com.englishmemory.util.JsonListConverter;
import com.englishmemory.util.Sm2Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseServiceImpl implements ExerciseService {

    private static final EnumSet<ExerciseType> NO_CORRECT_ANSWER = EnumSet.of(ExerciseType.SENTENCE_BUILDING);
    private static final Random RANDOM = new Random();
    private static final int WEAK_MASTERY_THRESHOLD = 40;
    private static final double WEAK_POOL_PROBABILITY = 0.8;

    private final ExerciseRepository        exerciseRepository;
    private final ExerciseAttemptRepository attemptRepository;
    private final VocabularyWordRepository  vocabularyRepository;
    private final ProgressRepository        progressRepository;
    private final UserRepository            userRepository;
    private final AiExerciseService         aiService;

    @Override
    @Transactional
    public ExerciseResponse generate(Long userId, GenerateExerciseRequest request) {
        VocabularyWord word = resolveWord(userId, request.getVocabularyWordId());
        ExerciseType   type = resolveType(request.getType());

        GeneratedExercise generated = aiService.generateExercise(word, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        Exercise exercise = Exercise.builder()
                .user(user)
                .vocabularyWord(word)
                .type(type)
                .question(generated.question())
                .options(JsonListConverter.toJson(generated.options()))
                .correctAnswer(generated.correctAnswer() != null && !generated.correctAnswer().isBlank()
                        ? generated.correctAnswer()
                        : "N/A")
                .explanation(generated.explanation())
                .build();

        Exercise saved = exerciseRepository.save(exercise);
        log.info("Exercício gerado: tipo={}, palavra='{}', usuário={}", type, word.getWord(), userId);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ExerciseAnswerResponse answer(Long userId, Long exerciseId, AnswerExerciseRequest request) {
        Exercise exercise = exerciseRepository.findByIdAndUserIdAndActiveTrue(exerciseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercício", exerciseId));

        boolean isCorrect = checkAnswer(exercise.getType(), request.getAnswer(), exercise.getCorrectAnswer());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .exercise(exercise)
                .user(user)
                .userAnswer(request.getAnswer())
                .isCorrect(isCorrect)
                .timeSpentSeconds(request.getTimeSpentSeconds() != null ? request.getTimeSpentSeconds() : 0)
                .build();
        attemptRepository.save(attempt);

        Integer masteryLevel = updateProgressIfWordBased(userId, exercise, isCorrect);

        log.info("Resposta registrada: exercícioId={}, correto={}, usuário={}", exerciseId, isCorrect, userId);

        return ExerciseAnswerResponse.builder()
                .exerciseId(exerciseId)
                .type(exercise.getType())
                .isCorrect(isCorrect)
                .userAnswer(request.getAnswer())
                .correctAnswer(exercise.getCorrectAnswer())
                .explanation(exercise.getExplanation())
                .masteryLevel(masteryLevel)
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .build();
    }

    @Override
    public PageResponse<ExerciseResponse> findAll(Long userId, Pageable pageable) {
        return PageResponse.from(
                exerciseRepository.findAllByUserIdAndActiveTrue(userId, pageable)
                        .map(this::toResponse)
        );
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private VocabularyWord resolveWord(Long userId, Long wordId) {
        if (wordId != null) {
            return vocabularyRepository.findByIdAndUserIdAndActiveTrue(wordId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Palavra", wordId));
        }

        List<VocabularyWord> pool = RANDOM.nextDouble() < WEAK_POOL_PROBABILITY
                ? vocabularyRepository.findGenuinelyWeakWordsByUserId(
                        userId, WEAK_MASTERY_THRESHOLD, PageRequest.of(0, 10))
                : List.of();

        if (pool.isEmpty()) {
            pool = vocabularyRepository.findAllByUserIdAndActiveTrue(userId, PageRequest.of(0, 10)).getContent();
        }

        if (pool.isEmpty()) {
            throw new BusinessException(
                    "Nenhuma palavra encontrada. Cadastre palavras para gerar exercícios.");
        }

        return pool.get(RANDOM.nextInt(pool.size()));
    }

    private ExerciseType resolveType(ExerciseType requested) {
        if (requested != null) return requested;
        ExerciseType[] types = ExerciseType.values();
        return types[RANDOM.nextInt(types.length)];
    }

    private boolean checkAnswer(ExerciseType type, String userAnswer, String correctAnswer) {
        if (NO_CORRECT_ANSWER.contains(type)) {
            return !userAnswer.isBlank();
        }

        String normalizedUser    = normalize(userAnswer);
        String normalizedCorrect = normalize(correctAnswer);

        return switch (type) {
            case WORD_ORDER    -> normalizeWords(normalizedUser).equals(normalizeWords(normalizedCorrect));
            case TRANSLATION   -> normalizedUser.contains(normalizedCorrect)
                               || normalizedCorrect.contains(normalizedUser);
            default            -> normalizedUser.equals(normalizedCorrect);
        };
    }

    private Integer updateProgressIfWordBased(Long userId, Exercise exercise, boolean isCorrect) {
        if (exercise.getVocabularyWord() == null) return null;

        Long wordId = exercise.getVocabularyWord().getId();

        return progressRepository.findByUserIdAndVocabularyWordIdAndActiveTrue(userId, wordId)
                .map(progress -> {
                    progress.setTotalReviews(progress.getTotalReviews() + 1);
                    if (isCorrect) {
                        progress.setCorrectReviews(progress.getCorrectReviews() + 1);
                    }
                    int mastery = Sm2Algorithm.calculateMastery(
                            progress.getCorrectReviews(),
                            progress.getTotalReviews() - progress.getCorrectReviews(),
                            0
                    );
                    progress.setMasteryLevel(mastery);
                    progress.setLastActivityDate(LocalDate.now());
                    return progressRepository.save(progress).getMasteryLevel();
                })
                .orElse(null);
    }

    private ExerciseResponse toResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .type(exercise.getType())
                .question(exercise.getQuestion())
                .options(JsonListConverter.fromJson(exercise.getOptions()))
                .vocabularyWordId(exercise.getVocabularyWord() != null
                        ? exercise.getVocabularyWord().getId() : null)
                .createdAt(exercise.getCreatedAt())
                .build();
    }

    private static final Pattern DIACRITICS  = Pattern.compile("\\p{M}+");
    private static final Pattern PUNCTUATION = Pattern.compile("[.,!?;:'\"“”‘’«»¿¡()\\[\\]{}]");

    private String normalize(String s) {
        if (s == null) return "";
        String withoutDiacritics = DIACRITICS.matcher(
                Normalizer.normalize(s, Normalizer.Form.NFD)
        ).replaceAll("");
        String withoutPunctuation = PUNCTUATION.matcher(withoutDiacritics).replaceAll("");
        return withoutPunctuation.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String normalizeWords(String s) {
        return String.join(" ", s.split("\\s+"));
    }
}
