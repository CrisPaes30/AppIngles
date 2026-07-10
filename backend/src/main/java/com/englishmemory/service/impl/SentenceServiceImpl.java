package com.englishmemory.service.impl;

import com.englishmemory.dto.request.AnalyzeSentenceRequest;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.dto.response.SentencePracticeResponse;
import com.englishmemory.entity.SentencePractice;
import com.englishmemory.entity.User;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.repository.SentencePracticeRepository;
import com.englishmemory.repository.UserRepository;
import com.englishmemory.repository.VocabularyWordRepository;
import com.englishmemory.service.SentenceService;
import com.englishmemory.service.ai.AiExerciseService;
import com.englishmemory.service.ai.model.SentenceAnalysis;
import com.englishmemory.util.JsonListConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentenceServiceImpl implements SentenceService {

    private final SentencePracticeRepository sentenceRepository;
    private final UserRepository             userRepository;
    private final VocabularyWordRepository   vocabularyRepository;
    private final AiExerciseService          aiService;

    @Override
    @Transactional
    public SentencePracticeResponse analyze(Long userId, AnalyzeSentenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));

        VocabularyWord word = null;
        String wordContext  = null;

        if (request.getVocabularyWordId() != null) {
            word = vocabularyRepository.findByIdAndUserIdAndActiveTrue(
                            request.getVocabularyWordId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Palavra", request.getVocabularyWordId()));
            wordContext = word.getWord();
        }

        SentenceAnalysis analysis = aiService.analyzeSentence(request.getSentence(), wordContext);

        SentencePractice entity = SentencePractice.builder()
                .user(user)
                .vocabularyWord(word)
                .originalSentence(request.getSentence())
                .correctedSentence(analysis.correctedSentence())
                .aiFeedback(analysis.aiFeedback())
                .grammarExplanation(analysis.grammarExplanation())
                .suggestedSentences(JsonListConverter.toJson(analysis.suggestedSentences()))
                .newVocabularyFound(JsonListConverter.toJson(analysis.newVocabularyFound()))
                .score(analysis.score())
                .build();

        SentencePractice saved = sentenceRepository.save(entity);
        log.info("Frase analisada (score={}) para usuário {}", analysis.score(), userId);

        return toResponse(saved);
    }

    @Override
    public PageResponse<SentencePracticeResponse> findHistory(Long userId, Pageable pageable) {
        return PageResponse.from(
                sentenceRepository
                        .findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId, pageable)
                        .map(this::toResponse)
        );
    }

    // -------------------------------------------------------------------------

    private SentencePracticeResponse toResponse(SentencePractice entity) {
        return SentencePracticeResponse.builder()
                .id(entity.getId())
                .originalSentence(entity.getOriginalSentence())
                .correctedSentence(entity.getCorrectedSentence())
                .aiFeedback(entity.getAiFeedback())
                .grammarExplanation(entity.getGrammarExplanation())
                .suggestedSentences(JsonListConverter.fromJson(entity.getSuggestedSentences()))
                .newVocabularyFound(JsonListConverter.fromJson(entity.getNewVocabularyFound()))
                .score(entity.getScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
