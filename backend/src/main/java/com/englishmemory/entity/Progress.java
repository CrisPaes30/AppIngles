package com.englishmemory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "PROGRESS")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_PROGRESS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VOCABULARY_WORD_ID", nullable = false)
    private VocabularyWord vocabularyWord;

    @Column(name = "MASTERY_LEVEL", nullable = false)
    @Builder.Default
    private Integer masteryLevel = 0;

    @Column(name = "TOTAL_REVIEWS", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "CORRECT_REVIEWS", nullable = false)
    @Builder.Default
    private Integer correctReviews = 0;

    @Column(name = "LAST_ACTIVITY_DATE")
    @Builder.Default
    private LocalDate lastActivityDate = LocalDate.now();
}
