package com.englishmemory.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReviewScheduleResponse {

    private Long id;
    private Double easeFactor;
    private Integer repetitions;
    private Integer intervalDays;
    private LocalDate nextReviewDate;
    private LocalDateTime lastReviewedAt;
    private Integer correctCount;
    private Integer incorrectCount;
    private Integer accuracyPercentage;
}
