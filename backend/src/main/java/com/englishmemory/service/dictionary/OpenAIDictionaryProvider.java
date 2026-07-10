package com.englishmemory.service.dictionary;

import com.englishmemory.exception.BusinessException;
import com.englishmemory.service.dictionary.model.WordDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.dictionary.provider", havingValue = "openai", matchIfMissing = true)
@RequiredArgsConstructor
public class OpenAIDictionaryProvider implements DictionaryProvider {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private static final Set<String> VALID_POS = Set.of(
            "NOUN", "VERB", "ADJECTIVE", "ADVERB", "PREPOSITION",
            "CONJUNCTION", "INTERJECTION", "PHRASAL_VERB", "EXPRESSION", "OTHER"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    private static final String SYSTEM_PROMPT =
            "You are an English language expert API. Given any English word or phrase, return ONLY a valid JSON object with no explanation, no markdown, no extra text.\n\n" +
            "Required JSON fields:\n" +
            "- word: the word/phrase as provided\n" +
            "- translation: Portuguese translation (use comma if multiple meanings)\n" +
            "- pronunciation: phonetic guide in English syllables (e.g., 'ôl-THO')\n" +
            "- ipa: IPA notation (e.g., '/ɔːlˈðoʊ/')\n" +
            "- meaning: main definition in English (1-2 sentences)\n" +
            "- partOfSpeech: one of NOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION, CONJUNCTION, INTERJECTION, PHRASAL_VERB, EXPRESSION, OTHER\n" +
            "- cefrLevel: one of A1, A2, B1, B2, C1, C2\n" +
            "- difficulty: integer 1-5 (1=elementary, 5=advanced)\n" +
            "- examples: array of exactly 3 natural, diverse example sentences\n" +
            "- synonyms: array of synonyms (may be empty)\n" +
            "- antonyms: array of antonyms (may be empty)\n" +
            "- collocations: array of common collocations (4-6 items)\n" +
            "- relatedPhrasalVerbs: array of related phrasal verbs (may be empty)\n" +
            "- commonErrors: array of strings describing common learner mistakes, format: '❌ wrong → ✅ correct'\n" +
            "- usageTips: array of 2-3 practical usage tips\n\n" +
            "Return ONLY the JSON object.";

    @Override
    public WordDetails enrich(String word) {
        log.info("[OpenAIDictionary] Enriquecendo palavra: '{}'", word);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", "Enrich the English word or phrase: \"" + word + "\"")
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, entity, String.class);

            JsonNode root    = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                String apiError = root.path("error").path("message").asText("resposta inválida da OpenAI");
                throw new RuntimeException("OpenAI error: " + apiError);
            }
            String content = choices.get(0).path("message").path("content").asText();

            WordDetails details = objectMapper.readValue(content, WordDetails.class);

            // normalize enums to uppercase
            if (details.getPartOfSpeech() != null) {
                String pos = details.getPartOfSpeech().toUpperCase().replace(" ", "_");
                if (!VALID_POS.contains(pos)) {
                    log.warn("[OpenAIDictionary] partOfSpeech '{}' não reconhecido, usando OTHER", pos);
                    pos = "OTHER";
                }
                details.setPartOfSpeech(pos);
            }
            if (details.getCefrLevel() != null) {
                details.setCefrLevel(details.getCefrLevel().toUpperCase());
            }
            if (details.getDifficulty() == null) {
                details.setDifficulty(3);
            }

            log.info("[OpenAIDictionary] Palavra '{}' enriquecida com sucesso", word);
            return details;

        } catch (Exception e) {
            log.error("[OpenAIDictionary] Falha ao enriquecer '{}': {}", word, e.getMessage());
            throw new BusinessException("Não foi possível obter informações sobre a palavra. Tente novamente.");
        }
    }
}
