package com.englishmemory.entity;

import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "VOCABULARY_WORDS")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_VOCABULARY_WORDS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyWord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @Column(name = "WORD", nullable = false, length = 200)
    private String word;

    @Column(name = "TRANSLATION", nullable = false, length = 500)
    private String translation;

    @Column(name = "PRONUNCIATION", length = 300)
    private String pronunciation;

    @Column(name = "IPA", length = 200)
    private String ipa;

    @Enumerated(EnumType.STRING)
    @Column(name = "PART_OF_SPEECH", length = 20)
    private PartOfSpeech partOfSpeech;

    @Enumerated(EnumType.STRING)
    @Column(name = "CEFR_LEVEL", length = 2)
    private CefrLevel cefrLevel;

    @Column(name = "DIFFICULTY", nullable = false)
    @Builder.Default
    private Integer difficulty = 3;

    @Column(name = "MEANING", length = 2000)
    private String meaning;

    @Column(name = "NOTES", length = 2000)
    private String notes;

    @Column(name = "PERSONAL_MEMORY", length = 2000)
    private String personalMemory;

    @Lob
    @Column(name = "EXAMPLES")
    private String examples;

    @Lob
    @Column(name = "SYNONYMS")
    private String synonyms;

    @Lob
    @Column(name = "ANTONYMS")
    private String antonyms;

    @Lob
    @Column(name = "COLLOCATIONS")
    private String collocations;

    @Lob
    @Column(name = "RELATED_PHRASAL_VERBS")
    private String relatedPhrasalVerbs;

    @Lob
    @Column(name = "COMMON_ERRORS")
    private String commonErrors;

    @Lob
    @Column(name = "USAGE_TIPS")
    private String usageTips;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "AUDIO_URL", length = 500)
    private String audioUrl;
}
