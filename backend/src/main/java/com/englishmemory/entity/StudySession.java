package com.englishmemory.entity;

import com.englishmemory.enums.SessionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "STUDY_SESSIONS")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_STUDY_SESSIONS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "SESSION_TYPE", nullable = false, length = 20)
    private SessionType sessionType;

    @Column(name = "STARTED_AT", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ENDED_AT")
    private LocalDateTime endedAt;

    @Column(name = "DURATION_MINUTES")
    @Builder.Default
    private Integer durationMinutes = 0;

    @Column(name = "WORDS_REVIEWED", nullable = false)
    @Builder.Default
    private Integer wordsReviewed = 0;

    @Column(name = "EXERCISES_COMPLETED", nullable = false)
    @Builder.Default
    private Integer exercisesCompleted = 0;

    @Column(name = "CORRECT_ANSWERS", nullable = false)
    @Builder.Default
    private Integer correctAnswers = 0;

    @Column(name = "INCORRECT_ANSWERS", nullable = false)
    @Builder.Default
    private Integer incorrectAnswers = 0;
}
