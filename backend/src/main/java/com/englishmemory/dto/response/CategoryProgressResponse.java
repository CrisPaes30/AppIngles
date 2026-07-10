package com.englishmemory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProgressResponse {

    private Long categoryId;
    private String categoryName;
    private String color;
    private long totalWords;
    private double averageMastery;
}
