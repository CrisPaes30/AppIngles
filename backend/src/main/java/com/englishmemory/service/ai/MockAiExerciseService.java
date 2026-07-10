package com.englishmemory.service.ai;

import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.enums.ExerciseType;
import com.englishmemory.service.ai.model.GeneratedExercise;
import com.englishmemory.service.ai.model.SentenceAnalysis;
import com.englishmemory.util.JsonListConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiExerciseService implements AiExerciseService {

    private static final Random RANDOM = new Random();

    private static final List<String> DISTRACTOR_POOL = List.of(
            "happy", "run", "beautiful", "important", "quickly",
            "create", "explain", "difficult", "success", "always"
    );

    private static final List<String> DISTRACTOR_PT_POOL = List.of(
            "feliz", "correr", "lindo", "importante", "rapidamente",
            "criar", "explicar", "difícil", "sucesso", "sempre"
    );

    @Override
    public GeneratedExercise generateExercise(VocabularyWord word, ExerciseType type) {
        log.debug("[MockAI] Gerando exercício tipo={} para palavra='{}'", type, word.getWord());
        return switch (type) {
            case MULTIPLE_CHOICE   -> multipleChoice(word);
            case FILL_BLANK        -> fillBlank(word);
            case WORD_ORDER        -> wordOrder(word);
            case TRANSLATION       -> translation(word);
            case TRUE_FALSE        -> trueFalse(word);
            case SENTENCE_BUILDING -> sentenceBuilding(word);
        };
    }

    @Override
    public SentenceAnalysis analyzeSentence(String sentence, String wordContext) {
        log.debug("[MockAI] Analisando frase: '{}'", sentence);

        String corrected = capitalize(sentence.trim());
        if (!corrected.endsWith(".") && !corrected.endsWith("!") && !corrected.endsWith("?")) {
            corrected += ".";
        }

        boolean hadCorrections = !corrected.equals(sentence.trim());

        String feedback = hadCorrections
                ? "Boa tentativa! Sua frase foi ligeiramente ajustada para melhorar a clareza e a pontuação."
                : "Excelente! Sua frase está muito bem construída. Continue praticando assim!";

        String grammar =
                "Em inglês, frases afirmativas seguem a ordem: Sujeito + Verbo + Complemento. " +
                "Lembre-se de iniciar com letra maiúscula e encerrar com pontuação. " +
                "Verbos na terceira pessoa do singular (he/she/it) recebem '-s' no presente simples.";

        List<String> suggestions = List.of(
                corrected,
                "You could also say: " + corrected.replace(".", " in different words.")
        );

        List<String> newVocab = wordContext != null
                ? List.of(wordContext, "practice", "improve")
                : List.of("practice", "improve");

        int score = hadCorrections ? 80 : 95;

        return new SentenceAnalysis(corrected, feedback, grammar, suggestions, newVocab, score);
    }

    // -------------------------------------------------------------------------
    // Geradores por tipo
    // -------------------------------------------------------------------------

    private GeneratedExercise multipleChoice(VocabularyWord word) {
        List<String> options = buildDistractors(word.getTranslation(), DISTRACTOR_PT_POOL);
        options.add(word.getTranslation());
        Collections.shuffle(options);

        String explanation = String.format(
                "\"%s\" significa \"%s\" em português.", word.getWord(), word.getTranslation());

        return new GeneratedExercise(
                String.format("Qual é a tradução correta de \"%s\"?", word.getWord()),
                options,
                word.getTranslation(),
                explanation
        );
    }

    private GeneratedExercise fillBlank(VocabularyWord word) {
        List<String> examples = JsonListConverter.fromJson(word.getExamples());
        String sentence;

        if (!examples.isEmpty()) {
            String ex = examples.get(0);
            sentence = ex.contains(word.getWord())
                    ? ex.replace(word.getWord(), "___")
                    : ex + " (use: ___)";
        } else {
            sentence = String.format("She decided to ___ every morning. (%s)", word.getTranslation());
        }

        return new GeneratedExercise(
                "Complete a frase com a palavra correta: \"" + sentence + "\"",
                null,
                word.getWord(),
                String.format("A palavra correta é \"%s\" (%s).", word.getWord(), word.getTranslation())
        );
    }

    private GeneratedExercise wordOrder(VocabularyWord word) {
        List<String> examples = JsonListConverter.fromJson(word.getExamples());
        String sentence = examples.isEmpty()
                ? String.format("I use the word %s every day.", word.getWord())
                : examples.get(0);

        List<String> shuffled = new ArrayList<>(Arrays.asList(sentence.split(" ")));
        Collections.shuffle(shuffled);

        return new GeneratedExercise(
                "Ordene as palavras para formar uma frase correta:",
                shuffled,
                sentence,
                "A frase correta é: \"" + sentence + "\""
        );
    }

    private GeneratedExercise translation(VocabularyWord word) {
        return new GeneratedExercise(
                String.format("Traduza para o inglês: \"%s\"", word.getTranslation()),
                null,
                word.getWord(),
                String.format("A tradução de \"%s\" é \"%s\".", word.getTranslation(), word.getWord())
        );
    }

    private GeneratedExercise trueFalse(VocabularyWord word) {
        boolean makeTrue = RANDOM.nextBoolean();
        String question;
        String correctAnswer;
        String explanation;

        if (makeTrue) {
            question    = String.format("\"%s\" significa \"%s\" em português.", word.getWord(), word.getTranslation());
            correctAnswer = "TRUE";
            explanation = String.format("Correto! \"%s\" realmente significa \"%s\".", word.getWord(), word.getTranslation());
        } else {
            String fakeTranslation = DISTRACTOR_PT_POOL.stream()
                    .filter(d -> !d.equalsIgnoreCase(word.getTranslation()))
                    .findFirst().orElse("pular");
            question    = String.format("\"%s\" significa \"%s\" em português.", word.getWord(), fakeTranslation);
            correctAnswer = "FALSE";
            explanation = String.format("Falso! \"%s\" significa \"%s\", não \"%s\".",
                    word.getWord(), word.getTranslation(), fakeTranslation);
        }

        return new GeneratedExercise(question, List.of("TRUE", "FALSE"), correctAnswer, explanation);
    }

    private GeneratedExercise sentenceBuilding(VocabularyWord word) {
        return new GeneratedExercise(
                String.format("Construa uma frase em inglês usando a palavra \"%s\" (%s):",
                        word.getWord(), word.getTranslation()),
                null,
                null,
                String.format("Qualquer frase gramaticalmente correta com \"%s\" é aceita. " +
                              "Tente criar algo do cotidiano!", word.getWord())
        );
    }

    // -------------------------------------------------------------------------

    private List<String> buildDistractors(String correct, List<String> pool) {
        List<String> distractors = new ArrayList<>();
        for (String d : pool) {
            if (!d.equalsIgnoreCase(correct) && distractors.size() < 3) {
                distractors.add(d);
            }
        }
        return new ArrayList<>(distractors);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
