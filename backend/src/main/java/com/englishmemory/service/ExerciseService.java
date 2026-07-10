package com.englishmemory.service;

import com.englishmemory.dto.request.AnswerExerciseRequest;
import com.englishmemory.dto.request.GenerateExerciseRequest;
import com.englishmemory.dto.response.ExerciseAnswerResponse;
import com.englishmemory.dto.response.ExerciseResponse;
import com.englishmemory.dto.response.PageResponse;

import org.springframework.data.domain.Pageable;

public interface ExerciseService {

    ExerciseResponse generate(Long userId, GenerateExerciseRequest request);

    ExerciseAnswerResponse answer(Long userId, Long exerciseId, AnswerExerciseRequest request);

    PageResponse<ExerciseResponse> findAll(Long userId, Pageable pageable);
}
