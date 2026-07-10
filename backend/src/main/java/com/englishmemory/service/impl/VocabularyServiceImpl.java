package com.englishmemory.service.impl;

import com.englishmemory.dto.request.CreateVocabularyRequest;
import com.englishmemory.dto.request.UpdateVocabularyRequest;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.dto.response.VocabularyResponse;
import com.englishmemory.dto.response.VocabularySummaryResponse;
import com.englishmemory.entity.*;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.mapper.ReviewScheduleMapper;
import com.englishmemory.mapper.VocabularyMapper;
import com.englishmemory.repository.*;
import com.englishmemory.service.VocabularyService;
import com.englishmemory.service.dictionary.DictionaryProvider;
import com.englishmemory.service.dictionary.model.WordDetails;
import com.englishmemory.util.JsonListConverter;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyWordRepository vocabularyRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ProgressRepository progressRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;
    private final ReviewScheduleMapper reviewScheduleMapper;
    private final DictionaryProvider dictionaryProvider;

    @Override
    public PageResponse<VocabularySummaryResponse> findAll(Long userId,
                                                           String search,
                                                           CefrLevel cefrLevel,
                                                           PartOfSpeech partOfSpeech,
                                                           Long categoryId,
                                                           Pageable pageable) {
        Page<VocabularyWord> page;

        if (search != null && !search.isBlank()) {
            page = vocabularyRepository.searchByWordContaining(userId, search.trim(), pageable);
        } else if (categoryId != null) {
            page = vocabularyRepository.findAllByUserIdAndCategoryIdAndActiveTrue(userId, categoryId, pageable);
        } else if (cefrLevel != null) {
            page = vocabularyRepository.findAllByUserIdAndCefrLevelAndActiveTrue(userId, cefrLevel, pageable);
        } else if (partOfSpeech != null) {
            page = vocabularyRepository.findAllByUserIdAndPartOfSpeechAndActiveTrue(userId, partOfSpeech, pageable);
        } else {
            page = vocabularyRepository.findAllByUserIdAndActiveTrue(userId, pageable);
        }

        return PageResponse.from(page.map(word -> toSummary(userId, word)));
    }

    @Override
    public VocabularyResponse findById(Long userId, Long id) {
        VocabularyWord word = findWordOrThrow(userId, id);
        VocabularyResponse response = vocabularyMapper.toResponse(word);

        reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(id)
                .ifPresent(rs -> response.setReviewSchedule(reviewScheduleMapper.toResponse(rs)));

        return response;
    }

    @Override
    @Transactional
    public VocabularyResponse create(Long userId, CreateVocabularyRequest request) {
        if (vocabularyRepository.existsByWordAndUserIdAndActiveTrue(request.getWord(), userId)) {
            throw new BusinessException("A palavra '" + request.getWord() + "' já está cadastrada.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        VocabularyWord word = vocabularyMapper.toEntity(request);
        word.setUser(user);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserIdAndActiveTrue(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.getCategoryId()));
            word.setCategory(category);
        }

        VocabularyWord saved = vocabularyRepository.save(word);
        log.info("Palavra '{}' criada para usuário {}", saved.getWord(), userId);

        createInitialReviewSchedule(saved);
        createInitialProgress(user, saved);

        VocabularyResponse response = vocabularyMapper.toResponse(saved);
        reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(saved.getId())
                .ifPresent(rs -> response.setReviewSchedule(reviewScheduleMapper.toResponse(rs)));

        return response;
    }

    @Override
    @Transactional
    public VocabularyResponse update(Long userId, Long id, UpdateVocabularyRequest request) {
        VocabularyWord word = findWordOrThrow(userId, id);

        applyUpdates(word, request, userId);

        VocabularyWord saved = vocabularyRepository.save(word);
        log.info("Palavra '{}' atualizada para usuário {}", saved.getWord(), userId);

        VocabularyResponse response = vocabularyMapper.toResponse(saved);
        reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(id)
                .ifPresent(rs -> response.setReviewSchedule(reviewScheduleMapper.toResponse(rs)));

        return response;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        VocabularyWord word = findWordOrThrow(userId, id);

        word.setActive(false);
        vocabularyRepository.save(word);

        reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(id)
                .ifPresent(rs -> {
                    rs.setActive(false);
                    reviewScheduleRepository.save(rs);
                });

        progressRepository.findByUserIdAndVocabularyWordIdAndActiveTrue(userId, id)
                .ifPresent(p -> {
                    p.setActive(false);
                    progressRepository.save(p);
                });

        log.info("Palavra id={} removida (soft delete) para usuário {}", id, userId);
    }

    @Override
    public List<VocabularySummaryResponse> listWeak(Long userId) {
        return vocabularyRepository
                .findWeakWordsByUserId(userId, PageRequest.of(0, 20))
                .stream()
                .map(word -> toSummary(userId, word))
                .toList();
    }

    @Override
    public WordDetails enrich(Long userId, String word) {
        WordDetails details = dictionaryProvider.enrich(word.trim());
        details.setAlreadyExists(
                vocabularyRepository.existsByWordAndUserIdAndActiveTrue(word.trim(), userId)
        );
        return details;
    }

    @Override
    public List<VocabularySummaryResponse> listDueToday(Long userId) {
        return reviewScheduleRepository
                .findDueForReview(userId, LocalDate.now(), PageRequest.of(0, 50))
                .stream()
                .map(rs -> toSummary(userId, rs.getVocabularyWord()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private VocabularyWord findWordOrThrow(Long userId, Long id) {
        return vocabularyRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Palavra", id));
    }

    private void createInitialReviewSchedule(VocabularyWord word) {
        ReviewSchedule schedule = ReviewSchedule.builder()
                .vocabularyWord(word)
                .easeFactor(new BigDecimal("2.5"))
                .repetitions(0)
                .intervalDays(1)
                .nextReviewDate(LocalDate.now())
                .correctCount(0)
                .incorrectCount(0)
                .build();
        reviewScheduleRepository.save(schedule);
    }

    private void createInitialProgress(User user, VocabularyWord word) {
        Progress progress = Progress.builder()
                .user(user)
                .vocabularyWord(word)
                .masteryLevel(0)
                .totalReviews(0)
                .correctReviews(0)
                .lastActivityDate(LocalDate.now())
                .build();
        progressRepository.save(progress);
    }

    private void applyUpdates(VocabularyWord word, UpdateVocabularyRequest request, Long userId) {
        if (request.getTranslation() != null)       word.setTranslation(request.getTranslation());
        if (request.getPronunciation() != null)     word.setPronunciation(request.getPronunciation());
        if (request.getIpa() != null)               word.setIpa(request.getIpa());
        if (request.getPartOfSpeech() != null)      word.setPartOfSpeech(request.getPartOfSpeech());
        if (request.getCefrLevel() != null)         word.setCefrLevel(request.getCefrLevel());
        if (request.getDifficulty() != null)        word.setDifficulty(request.getDifficulty());
        if (request.getNotes() != null)
            word.setNotes(request.getNotes().isBlank() ? null : request.getNotes());
        if (request.getImageUrl() != null)          word.setImageUrl(request.getImageUrl());
        if (request.getAudioUrl() != null)          word.setAudioUrl(request.getAudioUrl());
        if (request.getExamples() != null)          word.setExamples(JsonListConverter.toJson(request.getExamples()));
        if (request.getSynonyms() != null)          word.setSynonyms(JsonListConverter.toJson(request.getSynonyms()));
        if (request.getAntonyms() != null)          word.setAntonyms(JsonListConverter.toJson(request.getAntonyms()));
        if (request.getCollocations() != null)      word.setCollocations(JsonListConverter.toJson(request.getCollocations()));
        if (request.getRelatedPhrasalVerbs() != null) {
            word.setRelatedPhrasalVerbs(JsonListConverter.toJson(request.getRelatedPhrasalVerbs()));
        }
        if (request.getCommonErrors() != null) {
            word.setCommonErrors(JsonListConverter.toJson(request.getCommonErrors()));
        }
        if (request.getUsageTips() != null) {
            word.setUsageTips(JsonListConverter.toJson(request.getUsageTips()));
        }
        if (request.getMeaning() != null)
            word.setMeaning(request.getMeaning().isBlank() ? null : request.getMeaning());
        if (request.getPersonalMemory() != null)
            word.setPersonalMemory(request.getPersonalMemory().isBlank() ? null : request.getPersonalMemory());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserIdAndActiveTrue(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.getCategoryId()));
            word.setCategory(category);
        }
    }

    private VocabularySummaryResponse toSummary(Long userId, VocabularyWord word) {
        VocabularySummaryResponse summary = new VocabularySummaryResponse();
        summary.setId(word.getId());
        summary.setWord(word.getWord());
        summary.setTranslation(word.getTranslation());
        summary.setCefrLevel(word.getCefrLevel());
        summary.setPartOfSpeech(word.getPartOfSpeech());
        summary.setDifficulty(word.getDifficulty());

        if (word.getCategory() != null) {
            summary.setCategoryName(word.getCategory().getName());
        }

        progressRepository.findByUserIdAndVocabularyWordIdAndActiveTrue(userId, word.getId())
                .ifPresent(p -> summary.setMasteryLevel(p.getMasteryLevel()));

        reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(word.getId())
                .ifPresent(rs -> summary.setNextReviewDate(rs.getNextReviewDate()));

        return summary;
    }
}
