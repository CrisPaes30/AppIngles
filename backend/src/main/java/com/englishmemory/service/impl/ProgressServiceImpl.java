package com.englishmemory.service.impl;

import com.englishmemory.dto.response.CategoryProgressResponse;
import com.englishmemory.dto.response.CefrProgressResponse;
import com.englishmemory.dto.response.ProgressSummaryResponse;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.repository.ProgressRepository;
import com.englishmemory.repository.VocabularyWordRepository;
import com.englishmemory.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final VocabularyWordRepository vocabularyRepository;

    @Override
    public ProgressSummaryResponse getSummary(Long userId) {
        long totalWords   = vocabularyRepository.countByUserIdAndActiveTrue(userId);
        long learnedWords = progressRepository.countLearnedWordsByUserId(userId);
        long weakWords    = progressRepository.countWeakWordsByUserId(userId);
        long learningWords = Math.max(0, totalWords - learnedWords - weakWords);

        Double avgMastery = progressRepository.findAverageMasteryByUserId(userId);

        List<CefrProgressResponse> byLevel    = buildByLevel(userId);
        List<CategoryProgressResponse> byCat  = buildByCategory(userId);

        ProgressSummaryResponse response = new ProgressSummaryResponse();
        response.setAverageMastery(avgMastery != null ? avgMastery : 0.0);
        response.setTotalWords(totalWords);
        response.setLearnedWords(learnedWords);
        response.setLearningWords(learningWords);
        response.setWeakWords(weakWords);
        response.setByLevel(byLevel);
        response.setByCategory(byCat);

        return response;
    }

    private List<CefrProgressResponse> buildByLevel(Long userId) {
        List<Object[]> rows = progressRepository.findProgressGroupedByCefrLevel(userId);
        return rows.stream()
                .map(row -> {
                    CefrLevel level = (CefrLevel) row[0];
                    long count      = ((Number) row[1]).longValue();
                    double avg      = ((Number) row[2]).doubleValue();
                    return new CefrProgressResponse(level, level.getDescription(), count, avg);
                })
                .toList();
    }

    private List<CategoryProgressResponse> buildByCategory(Long userId) {
        List<Object[]> rows = progressRepository.findProgressGroupedByCategory(userId);
        return rows.stream()
                .map(row -> {
                    Long   catId    = ((Number) row[0]).longValue();
                    String catName  = (String) row[1];
                    String catColor = (String) row[2];
                    long   count    = ((Number) row[3]).longValue();
                    double avg      = ((Number) row[4]).doubleValue();
                    return new CategoryProgressResponse(catId, catName, catColor, count, avg);
                })
                .toList();
    }
}
