package com.englishmemory.dto.response;

import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VocabularyResponse {

    private Long id;
    private String word;
    private String translation;
    private String pronunciation;
    private String ipa;
    private PartOfSpeech partOfSpeech;
    private CefrLevel cefrLevel;
    private Integer difficulty;
    private String meaning;
    private String notes;
    private String personalMemory;
    private List<String> examples;
    private List<String> synonyms;
    private List<String> antonyms;
    private List<String> collocations;
    private List<String> relatedPhrasalVerbs;
    private List<String> commonErrors;
    private List<String> usageTips;
    private String imageUrl;
    private String audioUrl;
    private CategoryResponse category;
    private ReviewScheduleResponse reviewSchedule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
