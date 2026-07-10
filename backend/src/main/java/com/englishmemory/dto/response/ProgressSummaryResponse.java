package com.englishmemory.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ProgressSummaryResponse {

    private double averageMastery;
    private long totalWords;
    private long learnedWords;
    private long learningWords;
    private long weakWords;
    private List<CefrProgressResponse> byLevel;
    private List<CategoryProgressResponse> byCategory;
}
