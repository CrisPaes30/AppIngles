package com.englishmemory.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopMistakeWordResponse {
    private Long   wordId;
    private String word;
    private String translation;
    private String partOfSpeech;
    private int    incorrectCount;
    private int    correctCount;
    private int    totalReviews;
    private double accuracyPct;
}
