package com.englishmemory.entity;

import com.englishmemory.enums.ExerciseType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EXERCISES")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_EXERCISES", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOCABULARY_WORD_ID")
    private VocabularyWord vocabularyWord;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 30)
    private ExerciseType type;

    @Lob
    @Column(name = "QUESTION", nullable = false)
    private String question;

    @Lob
    @Column(name = "OPTIONS")
    private String options;

    @Lob
    @Column(name = "CORRECT_ANSWER", nullable = false)
    private String correctAnswer;

    @Lob
    @Column(name = "EXPLANATION")
    private String explanation;
}
