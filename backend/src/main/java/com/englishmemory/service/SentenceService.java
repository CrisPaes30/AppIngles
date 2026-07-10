package com.englishmemory.service;

import com.englishmemory.dto.request.AnalyzeSentenceRequest;
import com.englishmemory.dto.response.PageResponse;
import com.englishmemory.dto.response.SentencePracticeResponse;
import org.springframework.data.domain.Pageable;

public interface SentenceService {

    SentencePracticeResponse analyze(Long userId, AnalyzeSentenceRequest request);

    PageResponse<SentencePracticeResponse> findHistory(Long userId, Pageable pageable);
}
