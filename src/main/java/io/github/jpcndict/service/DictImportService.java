package io.github.jpcndict.service;

import io.github.jpcndict.entity.GrammarEntity;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles importing word/grammar JSON data files into the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DictImportService {

    private static final int BATCH_SIZE = 500;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Import words from an uploaded JSON file input stream.
     */
    @Transactional
    public int importWords(InputStream inputStream) throws IOException {
        long existingCount = wordRepository.count();
        if (existingCount > 0) {
            log.info("Words already imported ({} records), skipping", existingCount);
            return 0;
        }
        return importJsonArray(inputStream, (raw) -> {
            WordEntity entity = new WordEntity();
            entity.setWord((String) raw.get("word"));
            entity.setReading((String) raw.get("reading"));
            entity.setPos((String) raw.get("pos"));

            @SuppressWarnings("unchecked")
            List<String> meaning = (List<String>) raw.get("meaning");
            entity.setMeaning(meaning != null ? meaning.toArray(new String[0]) : null);

            @SuppressWarnings("unchecked")
            List<String> notes = (List<String>) raw.get("notes");
            entity.setNotes(notes != null ? notes.toArray(new String[0]) : null);

            entity.setIsManualConfirmed(false);
            return entity;
        }, wordRepository::saveAll);
    }

    /**
     * Import grammars from an uploaded JSON file input stream.
     */
    @Transactional
    public int importGrammars(InputStream inputStream) throws IOException {
        long existingCount = grammarRepository.count();
        if (existingCount > 0) {
            log.info("Grammars already imported ({} records), skipping", existingCount);
            return 0;
        }
        return importJsonArray(inputStream, (raw) -> {
            GrammarEntity entity = new GrammarEntity();
            entity.setWord((String) raw.get("word"));
            entity.setReading((String) raw.get("reading"));

            @SuppressWarnings("unchecked")
            List<String> meaning = (List<String>) raw.get("meaning");
            entity.setMeaning(meaning != null ? meaning.toArray(new String[0]) : null);

            @SuppressWarnings("unchecked")
            List<String> notes = (List<String>) raw.get("notes");
            entity.setNotes(notes != null ? notes.toArray(new String[0]) : null);

            entity.setIsManualConfirmed(false);
            return entity;
        }, grammarRepository::saveAll);
    }

    /**
     * Generic streaming JSON array importer: reads one object at a time,
     * converts to entity via converter, and batch-saves via saver.
     */
    private <T> int importJsonArray(InputStream inputStream,
                                     java.util.function.Function<Map<String, Object>, T> converter,
                                     java.util.function.Consumer<List<T>> saver) throws IOException {
        ObjectReader reader = objectMapper.readerFor(Map.class)
                .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        List<T> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0;

        try (JsonParser parser = objectMapper.createParser(inputStream)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array at root");
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                Map<String, Object> raw = reader.readValue(parser);
                T entity = converter.apply(raw);

                batch.add(entity);
                if (batch.size() >= BATCH_SIZE) {
                    saver.accept(batch);
                    total += batch.size();
                    batch.clear();
                    log.info("Imported {} records so far...", total);
                }
            }
        }

        if (!batch.isEmpty()) {
            saver.accept(batch);
            total += batch.size();
        }

        log.info("Import complete: {} total records", total);
        return total;
    }
}
