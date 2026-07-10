package com.englishmemory.dto.request;

import com.englishmemory.enums.ExerciseType;
import lombok.Data;

@Data
public class GenerateExerciseRequest {

    private Long vocabularyWordId;

    private ExerciseType type;
}
