package com.englishmemory.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonListConverter — testes unitários")
class JsonListConverterTest {

    @Test
    @DisplayName("toJson: lista com elementos gera JSON válido")
    void toJson_withElements_returnsJsonArray() {
        List<String> input = List.of("hello", "world", "english");
        String json = JsonListConverter.toJson(input);

        assertThat(json).isNotBlank();
        assertThat(json).contains("hello", "world", "english");
        assertThat(json).startsWith("[").endsWith("]");
    }

    @Test
    @DisplayName("toJson: lista vazia retorna array JSON vazio")
    void toJson_withEmptyList_returnsEmptyArray() {
        String json = JsonListConverter.toJson(List.of());
        assertThat(json).isEqualTo("[]");
    }

    @Test
    @DisplayName("toJson: null retorna null")
    void toJson_withNull_returnsNull() {
        assertThat(JsonListConverter.toJson(null)).isNull();
    }

    @Test
    @DisplayName("fromJson: JSON válido retorna lista correta")
    void fromJson_withValidJson_returnsList() {
        String json = "[\"apple\",\"banana\",\"cherry\"]";
        List<String> result = JsonListConverter.fromJson(json);

        assertThat(result).hasSize(3).containsExactly("apple", "banana", "cherry");
    }

    @Test
    @DisplayName("fromJson: JSON vazio retorna lista vazia")
    void fromJson_withEmptyArray_returnsEmptyList() {
        List<String> result = JsonListConverter.fromJson("[]");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fromJson: null retorna lista vazia")
    void fromJson_withNull_returnsEmptyList() {
        List<String> result = JsonListConverter.fromJson(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fromJson: string em branco retorna lista vazia")
    void fromJson_withBlankString_returnsEmptyList() {
        List<String> result = JsonListConverter.fromJson("  ");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("roundtrip: toJson → fromJson preserva elementos originais")
    void roundtrip_toJsonThenFromJson_preservesElements() {
        List<String> original = List.of("serendipity", "ephemeral", "melancholy");
        String json = JsonListConverter.toJson(original);
        List<String> restored = JsonListConverter.fromJson(json);

        assertThat(restored).containsExactlyElementsOf(original);
    }

    @Test
    @DisplayName("roundtrip: preserva strings com caracteres especiais")
    void roundtrip_withSpecialCharacters_preservesContent() {
        List<String> input = List.of("It's great!", "café", "naïve approach");
        List<String> restored = JsonListConverter.fromJson(JsonListConverter.toJson(input));

        assertThat(restored).containsExactlyElementsOf(input);
    }
}
