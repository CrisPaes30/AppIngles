package com.englishmemory.service;

import com.englishmemory.dto.request.CreateVocabularyRequest;
import com.englishmemory.dto.request.UpdateVocabularyRequest;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.dto.response.VocabularyResponse;
import com.englishmemory.dto.response.VocabularySummaryResponse;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import com.englishmemory.service.dictionary.model.WordDetails;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VocabularyService {

    PageResponse<VocabularySummaryResponse> findAll(Long userId,
                                                    String search,
                                                    CefrLevel cefrLevel,
                                                    PartOfSpeech partOfSpeech,
                                                    Long categoryId,
                                                    Pageable pageable);

    VocabularyResponse findById(Long userId, Long id);

    VocabularyResponse create(Long userId, CreateVocabularyRequest request);

    VocabularyResponse update(Long userId, Long id, UpdateVocabularyRequest request);

    void delete(Long userId, Long id);

    List<VocabularySummaryResponse> listWeak(Long userId);

    List<VocabularySummaryResponse> listDueToday(Long userId);

    WordDetails enrich(Long userId, String word);
}
