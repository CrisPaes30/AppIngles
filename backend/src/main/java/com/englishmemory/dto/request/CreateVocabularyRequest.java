package com.englishmemory.dto.request;

import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateVocabularyRequest {

    @NotBlank(message = "A palavra é obrigatória")
    @Size(max = 200, message = "A palavra deve ter no máximo 200 caracteres")
    private String word;

    @NotBlank(message = "A tradução é obrigatória")
    @Size(max = 500, message = "A tradução deve ter no máximo 500 caracteres")
    private String translation;

    @Size(max = 300, message = "A pronúncia deve ter no máximo 300 caracteres")
    private String pronunciation;

    @Size(max = 200, message = "O IPA deve ter no máximo 200 caracteres")
    private String ipa;

    private PartOfSpeech partOfSpeech;

    private Long categoryId;

    private CefrLevel cefrLevel;

    @Min(value = 1, message = "A dificuldade mínima é 1")
    @Max(value = 5, message = "A dificuldade máxima é 5")
    private Integer difficulty = 3;

    @Size(max = 2000)
    private String meaning;

    @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres")
    private String notes;

    @Size(max = 2000, message = "Minha forma de lembrar deve ter no máximo 2000 caracteres")
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
