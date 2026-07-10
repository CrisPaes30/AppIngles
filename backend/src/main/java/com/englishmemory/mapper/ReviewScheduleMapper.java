package com.englishmemory.mapper;

import com.englishmemory.dto.response.ReviewScheduleResponse;
import com.englishmemory.entity.ReviewSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewScheduleMapper {

    @Mapping(target = "accuracyPercentage", expression = "java(calculateAccuracy(entity))")
    ReviewScheduleResponse toResponse(ReviewSchedule entity);

    default Integer calculateAccuracy(ReviewSchedule entity) {
        int total = entity.getCorrectCount() + entity.getIncorrectCount();
        if (total == 0) return 0;
        return (int) Math.round((entity.getCorrectCount() * 100.0) / total);
    }
}
