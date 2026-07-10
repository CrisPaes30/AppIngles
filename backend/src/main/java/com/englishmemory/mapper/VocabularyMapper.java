package com.englishmemory.mapper;

import com.englishmemory.dto.request.CreateVocabularyRequest;
import com.englishmemory.dto.response.VocabularyResponse;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.util.JsonListConverter;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ReviewScheduleMapper.class})
public interface VocabularyMapper {

    @Mapping(target = "examples",            expression = "java(toList(entity.getExamples()))")
    @Mapping(target = "synonyms",            expression = "java(toList(entity.getSynonyms()))")
    @Mapping(target = "antonyms",            expression = "java(toList(entity.getAntonyms()))")
    @Mapping(target = "collocations",        expression = "java(toList(entity.getCollocations()))")
    @Mapping(target = "relatedPhrasalVerbs", expression = "java(toList(entity.getRelatedPhrasalVerbs()))")
    @Mapping(target = "commonErrors",        expression = "java(toList(entity.getCommonErrors()))")
    @Mapping(target = "usageTips",           expression = "java(toList(entity.getUsageTips()))")
    @Mapping(target = "reviewSchedule",      ignore = true)
    VocabularyResponse toResponse(VocabularyWord entity);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "user",                ignore = true)
    @Mapping(target = "category",            ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    @Mapping(target = "active",              ignore = true)
    @Mapping(target = "examples",            expression = "java(toJson(request.getExamples()))")
    @Mapping(target = "synonyms",            expression = "java(toJson(request.getSynonyms()))")
    @Mapping(target = "antonyms",            expression = "java(toJson(request.getAntonyms()))")
    @Mapping(target = "collocations",        expression = "java(toJson(request.getCollocations()))")
    @Mapping(target = "relatedPhrasalVerbs", expression = "java(toJson(request.getRelatedPhrasalVerbs()))")
    @Mapping(target = "commonErrors",        expression = "java(toJson(request.getCommonErrors()))")
    @Mapping(target = "usageTips",           expression = "java(toJson(request.getUsageTips()))")
    VocabularyWord toEntity(CreateVocabularyRequest request);

    default List<String> toList(String json) {
        return JsonListConverter.fromJson(json);
    }

    default String toJson(List<String> list) {
        return JsonListConverter.toJson(list);
    }
}
