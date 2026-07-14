package com.englishmemory.service.ai;

import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.enums.ExerciseType;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.service.ai.model.GeneratedExercise;
import com.englishmemory.service.ai.model.SentenceAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
@RequiredArgsConstructor
public class OpenAiExerciseService implements AiExerciseService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    private static final String EXERCISE_SYSTEM_PROMPT = """
            You are an English language teacher creating exercises for Brazilian Portuguese speakers learning English.
            Return ONLY a valid JSON object with no explanation, no markdown, no extra text.

            JSON structure (always include all fields):
            {
              "question": "...",
              "options": [...] or null,
              "correctAnswer": "..." or null,
              "explanation": "..."
            }

            Rules per exercise type:
            - MULTIPLE_CHOICE: question asks for Portuguese translation, options is exactly 4 Portuguese strings (1 correct + 3 plausible distractors shuffled), correctAnswer is the correct option string.
            - FILL_BLANK: question is a natural English sentence with ___ replacing the target word, options is null, correctAnswer is the missing word.
            - WORD_ORDER: question is a brief instruction in Portuguese, options is an array of the individual words (shuffled) from a complete English sentence that uses the target word, correctAnswer is the full correct sentence.
            - TRANSLATION: question asks (in Portuguese) to translate the Portuguese meaning to English, options is null, correctAnswer is the English word/phrase.
            - TRUE_FALSE: question is an English statement about the word — sometimes true, sometimes false (vary randomly), options is ["TRUE","FALSE"], correctAnswer is "TRUE" or "FALSE".
            - SENTENCE_BUILDING: question instructs (in Portuguese) to write an English sentence using the word, options is null, correctAnswer is null, explanation gives a good example sentence.

            Write questions and explanations in Brazilian Portuguese. Example sentences in English.
            """;

    private static final String ANALYSIS_SYSTEM_PROMPT = """
            You are an English language teacher helping a Brazilian Portuguese speaker practice English.
            Analyze the student's English sentence and return ONLY a valid JSON object with no markdown:
            {
              "correctedSentence": "corrected version, or original if already perfect",
              "aiFeedback": "warm, encouraging feedback in Portuguese (2-3 sentences, mention what was good)",
              "grammarExplanation": "one concise grammar tip in Portuguese relevant to this sentence",
              "suggestedSentences": ["1-2 alternative ways to express the same idea in English"],
              "newVocabularyFound": ["2-4 interesting or advanced words from the sentence worth highlighting"],
              "score": <integer 0-100>
            }
            Scoring: 90-100 perfect, 70-89 minor issues, 50-69 noticeable errors, below 50 major errors.
            """;

    @Override
    public GeneratedExercise generateExercise(VocabularyWord word, ExerciseType type) {
        log.info("[OpenAiExercise] Gerando exercício tipo={} para '{}'", type, word.getWord());
        try {
            String userMessage = String.format(
                    "Create a %s exercise for the English word/phrase: \"%s\" " +
                    "(Portuguese translation: \"%s\", CEFR level: %s).",
                    type.name(),
                    word.getWord(),
                    word.getTranslation(),
                    word.getCefrLevel() != null ? word.getCefrLevel().name() : "unknown"
            );

            String content = callOpenAI(EXERCISE_SYSTEM_PROMPT, userMessage, 0.8);
            JsonNode node  = objectMapper.readTree(content);

            String question     = node.path("question").asText();
            String explanation  = node.path("explanation").asText();
            String correctAnswer = !node.path("correctAnswer").isNull()
                    ? node.path("correctAnswer").asText(null) : null;
            List<String> options = parseStringArray(node.path("options"));

            if (type == ExerciseType.WORD_ORDER && correctAnswer != null) {
                // O modelo às vezes devolve "options" que não são as palavras
                // individuais de correctAnswer (ex: frases inteiras já montadas,
                // ou um conjunto que não reconstrói a frase certa) — tornando o
                // exercício impossível de acertar. Em vez de confiar no que o
                // modelo mandou, derivamos os tiles direto da resposta correta,
                // garantindo que ela sempre seja alcançável.
                options = wordOrderTilesFrom(correctAnswer);
            }

            return new GeneratedExercise(question, options, correctAnswer, explanation);

        } catch (Exception e) {
            log.error("[OpenAiExercise] Falha ao gerar exercício tipo={}: {}", type, e.getMessage());
            return fallback(word, type);
        }
    }

    @Override
    public SentenceAnalysis analyzeSentence(String sentence, String wordContext) {
        log.info("[OpenAiExercise] Analisando frase: '{}'", sentence);
        try {
            String userMessage = "Analyze this English sentence written by a Brazilian student"
                    + (wordContext != null ? " (practicing the word \"" + wordContext + "\")" : "")
                    + ":\n\n\"" + sentence + "\"";

            String content = callOpenAI(ANALYSIS_SYSTEM_PROMPT, userMessage, 0.4);
            JsonNode node  = objectMapper.readTree(content);

            return new SentenceAnalysis(
                    node.path("correctedSentence").asText(sentence),
                    node.path("aiFeedback").asText(),
                    node.path("grammarExplanation").asText(),
                    parseStringArray(node.path("suggestedSentences")),
                    parseStringArray(node.path("newVocabularyFound")),
                    node.path("score").asInt(80)
            );

        } catch (Exception e) {
            log.error("[OpenAiExercise] Falha ao analisar frase: {}", e.getMessage());
            throw new BusinessException("Não foi possível analisar a frase. Tente novamente.");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String callOpenAI(String systemPrompt, String userMessage, double temperature) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", temperature,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", userMessage)
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
        return choices.get(0).path("message").path("content").asText();
    }

    private List<String> parseStringArray(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray()) return null;
        List<String> result = new ArrayList<>();
        node.forEach(n -> result.add(n.asText()));
        return result.isEmpty() ? null : result;
    }

    private List<String> wordOrderTilesFrom(String correctAnswer) {
        List<String> words = new ArrayList<>(Arrays.asList(correctAnswer.trim().split("\\s+")));
        Collections.shuffle(words);
        return words;
    }

    private GeneratedExercise fallback(VocabularyWord word, ExerciseType type) {
        log.warn("[OpenAiExercise] Usando fallback para tipo={}", type);
        return new GeneratedExercise(
                String.format("Qual é a tradução de \"%s\"?", word.getWord()),
                null,
                word.getTranslation(),
                String.format("\"%s\" significa \"%s\" em português.", word.getWord(), word.getTranslation())
        );
    }
}
