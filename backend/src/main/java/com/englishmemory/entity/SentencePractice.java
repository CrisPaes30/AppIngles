package com.englishmemory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SENTENCE_PRACTICES")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_SENTENCE_PRACTICES", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentencePractice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOCABULARY_WORD_ID")
    private VocabularyWord vocabularyWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_SESSION_ID")
    private StudySession studySession;

    @Lob
    @Column(name = "ORIGINAL_SENTENCE", nullable = false)
    private String originalSentence;

    @Lob
    @Column(name = "CORRECTED_SENTENCE")
    private String correctedSentence;

    @Lob
    @Column(name = "AI_FEEDBACK")
    private String aiFeedback;

    @Lob
    @Column(name = "GRAMMAR_EXPLANATION")
    private String grammarExplanation;

    @Lob
    @Column(name = "SUGGESTED_SENTENCES")
    private String suggestedSentences;

    @Lob
    @Column(name = "NEW_VOCABULARY_FOUND")
    private String newVocabularyFound;

    @Column(name = "SCORE")
    private Integer score;
}
