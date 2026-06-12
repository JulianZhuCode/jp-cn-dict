package io.github.jpcndict.service;

import io.github.jpcndict.entity.GrammarEntity;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handles importing word/grammar JSON data files into the database,
 * and exporting them back into id-range zips.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DictImportService {

    private static final int BATCH_SIZE = 500;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectReader mapReader = objectMapper.readerFor(Map.class)
            .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    // ---- Import (batch) ----

    @Transactional
    public int importWords(List<InputStream> inputStreams) throws IOException {
        long existingCount = wordRepository.count();
        if (existingCount > 0) {
            log.info("Words already imported ({} records), skipping", existingCount);
            return 0;
        }
        int total = 0;
        for (InputStream is : inputStreams) {
            total += importJsonArray(is, this::toWordEntity, wordRepository::saveAll);
        }
        return total;
    }

    @Transactional
    public int importGrammars(List<InputStream> inputStreams) throws IOException {
        long existingCount = grammarRepository.count();
        if (existingCount > 0) {
            log.info("Grammars already imported ({} records), skipping", existingCount);
            return 0;
        }
        int total = 0;
        for (InputStream is : inputStreams) {
            total += importJsonArray(is, this::toGrammarEntity, grammarRepository::saveAll);
        }
        return total;
    }

    private WordEntity toWordEntity(Map<String, Object> raw) {
        WordEntity entity = new WordEntity();
        entity.setWord((String) raw.get("word"));
        entity.setReading((String) raw.get("reading"));
        entity.setRomaji((String) raw.get("romaji"));
        entity.setPos((String) raw.get("pos"));
        @SuppressWarnings("unchecked")
        List<String> meaning = (List<String>) raw.get("meaning");
        entity.setMeaning(meaning != null ? meaning.toArray(new String[0]) : null);
        @SuppressWarnings("unchecked")
        List<String> notes = (List<String>) raw.get("notes");
        entity.setNotes(notes != null ? notes.toArray(new String[0]) : null);
        entity.setIsManualConfirmed(Boolean.TRUE.equals(raw.get("isManualConfirmed")));
        return entity;
    }

    private GrammarEntity toGrammarEntity(Map<String, Object> raw) {
        GrammarEntity entity = new GrammarEntity();
        entity.setWord((String) raw.get("word"));
        entity.setReading((String) raw.get("reading"));
        @SuppressWarnings("unchecked")
        List<String> meaning = (List<String>) raw.get("meaning");
        entity.setMeaning(meaning != null ? meaning.toArray(new String[0]) : null);
        @SuppressWarnings("unchecked")
        List<String> notes = (List<String>) raw.get("notes");
        entity.setNotes(notes != null ? notes.toArray(new String[0]) : null);
        entity.setIsManualConfirmed(Boolean.TRUE.equals(raw.get("isManualConfirmed")));
        return entity;
    }

    private <T> int importJsonArray(InputStream inputStream,
                                    java.util.function.Function<Map<String, Object>, T> converter,
                                    java.util.function.Consumer<List<T>> saver) throws IOException {
        List<T> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0;

        try (JsonParser parser = objectMapper.createParser(inputStream)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array at root");
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                Map<String, Object> raw = mapReader.readValue(parser);
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

    // ---- Export ----

    public void exportWords(OutputStream out) throws IOException {
        List<WordEntity> all = wordRepository.findAll(Sort.by("id"));
        Map<Integer, List<Map<String, Object>>> groups = new TreeMap<>();
        for (WordEntity e : all) {
            int start = ((e.getId() - 1) / 100) * 100 + 1;
            groups.computeIfAbsent(start, _ -> new ArrayList<>()).add(toWordExportMap(e));
        }
        writeZip(out, "word_", groups);
    }

    public void exportGrammars(OutputStream out) throws IOException {
        List<GrammarEntity> all = grammarRepository.findAll(Sort.by("id"));
        Map<Integer, List<Map<String, Object>>> groups = new TreeMap<>();
        for (GrammarEntity e : all) {
            int start = ((e.getId() - 1) / 100) * 100 + 1;
            groups.computeIfAbsent(start, _ -> new ArrayList<>()).add(toGrammarExportMap(e));
        }
        writeZip(out, "grammar_", groups);
    }

    private Map<String, Object> toWordExportMap(WordEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("word", e.getWord());
        map.put("reading", e.getReading());
        map.put("romaji", e.getRomaji());
        map.put("meaning", e.getMeaning());
        map.put("notes", e.getNotes());
        map.put("pos", e.getPos());
        map.put("isManualConfirmed", e.getIsManualConfirmed());
        return map;
    }

    private Map<String, Object> toGrammarExportMap(GrammarEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("word", e.getWord());
        map.put("reading", e.getReading());
        map.put("meaning", e.getMeaning());
        map.put("notes", e.getNotes());
        map.put("isManualConfirmed", e.getIsManualConfirmed());
        return map;
    }

    private void writeZip(OutputStream out, String prefix,
                          Map<Integer, List<Map<String, Object>>> groups) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : groups.entrySet()) {
                String fileName = prefix + entry.getKey() + ".json";
                zos.putNextEntry(new ZipEntry(fileName));
                byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(entry.getValue());
                zos.write(jsonBytes);
                zos.closeEntry();
            }
        }
    }
}
