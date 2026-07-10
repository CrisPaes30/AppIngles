package com.englishmemory.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyProgressResponse {

    private LocalDate date;
    private int wordsReviewed;
    private int exercisesCompleted;
    private int studyMinutes;
}
