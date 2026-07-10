package com.englishmemory.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {

    private long totalWords;
    private long learnedWords;
    private long learningWords;
    private long weakWords;
    private long wordsToReviewToday;
    private int  streakDays;
    private long totalStudyMinutes;
    private double averageMastery;
    private List<DailyProgressResponse> weeklyChart;

    // Métricas avançadas
    private long   newWordsThisWeek;
    private long   wordsReviewedThisWeek;
    private double overallAccuracyPct;
    private String weakestPartOfSpeech;
    private List<TopMistakeWordResponse> topMistakeWords;
}
