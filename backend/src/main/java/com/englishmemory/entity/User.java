package com.englishmemory.entity;

import com.englishmemory.enums.CefrLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "USERS")
@SequenceGenerator(name = "default_seq", sequenceName = "SEQ_USERS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "PASSWORD_HASH", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "CEFR_LEVEL", length = 2)
    private CefrLevel cefrLevel;

    @Column(name = "TIMEZONE", length = 50)
    private String timezone;

    @Column(name = "STREAK_DAYS", nullable = false)
    @Builder.Default
    private Integer streakDays = 0;

    @Column(name = "LAST_STUDY_DATE")
    private LocalDate lastStudyDate;
}
