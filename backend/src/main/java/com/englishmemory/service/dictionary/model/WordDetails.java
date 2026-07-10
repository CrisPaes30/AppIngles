package com.englishmemory.service.dictionary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordDetails {

    private String word;
    private String translation;
    private String pronunciation;
    private String ipa;
    private String meaning;
    private String partOfSpeech;
    private String cefrLevel;
    private Integer difficulty;
    @Builder.Default private List<String> examples           = new ArrayList<>();
    @Builder.Default private List<String> synonyms           = new ArrayList<>();
    @Builder.Default private List<String> antonyms           = new ArrayList<>();
    @Builder.Default private List<String> collocations       = new ArrayList<>();
    @Builder.Default private List<String> relatedPhrasalVerbs = new ArrayList<>();
    @Builder.Default private List<String> commonErrors       = new ArrayList<>();
    @Builder.Default private List<String> usageTips          = new ArrayList<>();

    /** Preenchido pelo serviço, não pela IA */
    private boolean alreadyExists;
}
