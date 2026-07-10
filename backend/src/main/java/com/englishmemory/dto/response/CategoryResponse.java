package com.englishmemory.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Long wordCount;
    private LocalDateTime createdAt;
}
