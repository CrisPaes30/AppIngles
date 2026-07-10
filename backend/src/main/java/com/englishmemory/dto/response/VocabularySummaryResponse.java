package com.englishmemory.dto.response;

import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VocabularySummaryResponse {

    private Long id;
    private String word;
    private String translation;
    private CefrLevel cefrLevel;
    private PartOfSpeech partOfSpeech;
    private Integer difficulty;
    private String categoryName;
    private Integer masteryLevel;
    private LocalDate nextReviewDate;
}
