package com.englishmemory.dto.request;

import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateVocabularyRequest {

    @Size(max = 200)
    private String translation;

    @Size(max = 300)
    private String pronunciation;

    @Size(max = 200)
    private String ipa;

    private PartOfSpeech partOfSpeech;

    private Long categoryId;

    private CefrLevel cefrLevel;

    @Min(1) @Max(5)
    private Integer difficulty;

    @Size(max = 2000)
    private String meaning;

    @Size(max = 2000)
    private String notes;

    @Size(max = 2000)
    private String personalMemory;

    private List<String> examples;
    private List<String> synonyms;
    private List<String> antonyms;
    private List<String> collocations;
    private List<String> relatedPhrasalVerbs;
    private List<String> commonErrors;
    private List<String> usageTips;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 500)
    private String audioUrl;
}
