package com.englishmemory.dto.response;

import com.englishmemory.enums.ExerciseType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseAnswerResponse {

    private Long         exerciseId;
    private ExerciseType type;
    private Boolean      isCorrect;
    private String       userAnswer;
    private String       correctAnswer;
    private String       explanation;
    private Integer      masteryLevel;
    private Integer      timeSpentSeconds;
}
