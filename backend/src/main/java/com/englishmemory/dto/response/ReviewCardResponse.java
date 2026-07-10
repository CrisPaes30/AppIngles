package com.englishmemory.dto.response;

import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewCardResponse {

    private Long   vocabularyWordId;
    private String word;
    private String translation;
    private String pronunciation;
    private String ipa;
    private PartOfSpeech partOfSpeech;
    private CefrLevel    cefrLevel;
    private List<String> examples;
    private List<String> synonyms;
    private String       categoryName;

    private Integer repetitions;
    private Integer intervalDays;
    private Integer correctCount;
    private Integer incorrectCount;
    private Integer accuracyPercentage;
    private Integer masteryLevel;
    private LocalDate    nextReviewDate;
    private LocalDateTime lastReviewedAt;
}
