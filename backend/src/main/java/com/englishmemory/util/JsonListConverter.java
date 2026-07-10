package com.englishmemory.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public final class JsonListConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    private JsonListConverter() {}

    public static String toJson(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Falha ao converter lista para JSON: {}", e.getMessage());
            return null;
        }
    }

    public static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json, LIST_TYPE);
        } catch (JsonProcessingException e) {
            log.warn("Falha ao converter JSON para lista: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
