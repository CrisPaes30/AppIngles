package com.englishmemory.dto.response;

import com.englishmemory.enums.ExerciseType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExerciseResponse {

    private Long         id;
    private ExerciseType type;
    private String       question;
    private List<String> options;
    private Long         vocabularyWordId;
    private String       audioDataUri;
    private LocalDateTime createdAt;
}
