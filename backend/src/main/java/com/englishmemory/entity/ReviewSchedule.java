package com.englishmemory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW_SCHEDULES")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_REVIEW_SCHEDULES", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSchedule extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VOCABULARY_WORD_ID", nullable = false, unique = true)
    private VocabularyWord vocabularyWord;

    @Column(name = "EASE_FACTOR", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal easeFactor = new BigDecimal("2.5");

    @Column(name = "REPETITIONS", nullable = false)
    @Builder.Default
    private Integer repetitions = 0;

    @Column(name = "INTERVAL_DAYS", nullable = false)
    @Builder.Default
    private Integer intervalDays = 1;

    @Column(name = "NEXT_REVIEW_DATE", nullable = false)
    @Builder.Default
    private LocalDate nextReviewDate = LocalDate.now();

    @Column(name = "LAST_REVIEWED_AT")
    private LocalDateTime lastReviewedAt;

    @Column(name = "CORRECT_COUNT", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "INCORRECT_COUNT", nullable = false)
    @Builder.Default
    private Integer incorrectCount = 0;
}
