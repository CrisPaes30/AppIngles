package com.englishmemory.service;

import com.englishmemory.dto.request.ReviewAnswerRequest;
import com.englishmemory.dto.response.ReviewCardResponse;
import com.englishmemory.dto.response.ReviewResultResponse;

import java.util.List;

public interface ReviewService {

    List<ReviewCardResponse> getTodayReviews(Long userId);

    long countTodayReviews(Long userId);

    ReviewResultResponse submitAnswer(Long userId, Long wordId, ReviewAnswerRequest request);
}
