package com.englishmemory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EXERCISE_ATTEMPTS")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_EXERCISE_ATTEMPTS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXERCISE_ID", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_SESSION_ID")
    private StudySession studySession;

    @Lob
    @Column(name = "USER_ANSWER", nullable = false)
    private String userAnswer;

    @Column(name = "IS_CORRECT", nullable = false)
    private Boolean isCorrect;

    @Column(name = "TIME_SPENT_SECONDS", nullable = false)
    @Builder.Default
    private Integer timeSpentSeconds = 0;
}
