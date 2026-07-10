package com.englishmemory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReviewResultResponse {

    private Long    vocabularyWordId;
    private String  word;
    private boolean isCorrect;
    private int     quality;
    private int     newRepetitions;
    private int     newIntervalDays;
    private double  newEaseFactor;
    private int     masteryLevel;
    private LocalDate nextReviewDate;
    private String  feedbackMessage;
}
