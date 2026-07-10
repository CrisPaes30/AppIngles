package com.englishmemory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SentencePracticeResponse {

    private Long         id;
    private String       originalSentence;
    private String       correctedSentence;
    private String       aiFeedback;
    private String       grammarExplanation;
    private List<String> suggestedSentences;
    private List<String> newVocabularyFound;
    private Integer      score;
    private LocalDateTime createdAt;
}
