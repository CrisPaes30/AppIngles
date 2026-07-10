package com.englishmemory.service.dictionary;

import com.englishmemory.service.dictionary.model.WordDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.dictionary.provider", havingValue = "mock")
public class MockDictionaryProvider implements DictionaryProvider {

    @Override
    public WordDetails enrich(String word) {
        log.debug("[MockDictionary] Enriquecendo palavra: '{}'", word);

        return WordDetails.builder()
                .word(word)
                .translation("(mock) tradução de " + word)
                .pronunciation("pro-NUN-si-EI-shon")
                .ipa("/ˈprɒnʌnsiˌeɪʃən/")
                .meaning("(mock) The main definition of '" + word + "' in English.")
                .partOfSpeech("NOUN")
                .cefrLevel("B1")
                .difficulty(3)
                .examples(List.of(
                        "I use the word '" + word + "' every day.",
                        "She learned '" + word + "' in her English class.",
                        "The teacher explained what '" + word + "' means."
                ))
                .synonyms(List.of("synonym1", "synonym2", "synonym3"))
                .antonyms(List.of("antonym1"))
                .collocations(List.of(word + " structure", word + " pattern", "common " + word))
                .relatedPhrasalVerbs(List.of())
                .commonErrors(List.of(
                        "❌ Wrong usage of '" + word + "' → ✅ Correct usage in context"
                ))
                .usageTips(List.of(
                        "Tip 1: Use '" + word + "' in formal and informal contexts.",
                        "Tip 2: Combine with adjectives for nuance."
                ))
                .build();
    }
}
