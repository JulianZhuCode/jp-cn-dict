package io.github.jpcndict.service;

import io.github.jpcndict.entity.ExampleEntity;
import io.github.jpcndict.entity.GrammarEntity;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.repository.ExampleRepository;
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

import java.io.ByteArrayOutputStream;
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
    private final ExampleRepository exampleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectReader mapReader = objectMapper.readerFor(Map.class)
            .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    // ---- Import (upsert by id) ----

    private static Integer toInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    @Transactional
    public int importWords(List<InputStream> inputStreams) throws IOException {
        long before = wordRepository.count();
        int total = 0;
        for (InputStream is : inputStreams) {
            total += importJsonArray(is, this::toWordEntity, wordRepository::saveAll);
        }
        long after = wordRepository.count();
        log.info("Words import complete: {} processed, {} new inserted, {} updated",
                total, after - before, total - (after - before));
        return total;
    }

    @Transactional
    public int importGrammars(List<InputStream> inputStreams) throws IOException {
        long before = grammarRepository.count();
        int total = 0;
        for (InputStream is : inputStreams) {
            total += importJsonArray(is, this::toGrammarEntity, grammarRepository::saveAll);
        }
        long after = grammarRepository.count();
        log.info("Grammars import complete: {} processed, {} new inserted, {} updated",
                total, after - before, total - (after - before));
        return total;
    }

    @Transactional
    public int importExamples(List<InputStream> inputStreams) throws IOException {
        long before = exampleRepository.count();
        int total = 0;
        for (InputStream is : inputStreams) {
            total += importJsonArray(is, this::toExampleEntity, exampleRepository::saveAll);
        }
        long after = exampleRepository.count();
        log.info("Examples import complete: {} processed, {} new inserted, {} updated",
                total, after - before, total - (after - before));
        return total;
    }

    private WordEntity toWordEntity(Map<String, Object> raw) {
        WordEntity entity = new WordEntity();
        entity.setId(toInt(raw.get("id")));
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
        return entity;
    }

    private GrammarEntity toGrammarEntity(Map<String, Object> raw) {
        GrammarEntity entity = new GrammarEntity();
        entity.setId(toInt(raw.get("id")));
        // 兼容旧格式 word 和新格式 pattern
        String pattern = (String) raw.get("pattern");
        if (pattern == null) {
            pattern = (String) raw.get("word");
        }
        entity.setPattern(pattern);
        entity.setReading((String) raw.get("reading"));
        @SuppressWarnings("unchecked")
        List<String> meaning = (List<String>) raw.get("meaning");
        entity.setMeaning(meaning != null ? meaning.toArray(new String[0]) : null);
        @SuppressWarnings("unchecked")
        List<String> notes = (List<String>) raw.get("notes");
        entity.setNotes(notes != null ? notes.toArray(new String[0]) : null);
        return entity;
    }

    private ExampleEntity toExampleEntity(Map<String, Object> raw) {
        ExampleEntity entity = new ExampleEntity();
        entity.setId(toInt(raw.get("id")));
        entity.setJp((String) raw.get("jp"));
        entity.setCn((String) raw.get("cn"));
        @SuppressWarnings("unchecked")
        List<Integer> relatedWords = (List<Integer>) raw.get("relatedWords");
        entity.setRelatedWords(relatedWords != null ? relatedWords.toArray(new Integer[0]) : null);
        @SuppressWarnings("unchecked")
        List<Integer> relatedGrammars = (List<Integer>) raw.get("relatedGrammars");
        entity.setRelatedGrammars(relatedGrammars != null ? relatedGrammars.toArray(new Integer[0]) : null);
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

    public byte[] exportWords() throws IOException {
        List<WordEntity> all = wordRepository.findAll(Sort.by("id"));
        Map<Integer, List<Map<String, Object>>> groups = new TreeMap<>();
        for (WordEntity e : all) {
            int start = ((e.getId() - 1) / 100) * 100 + 1;
            groups.computeIfAbsent(start, _ -> new ArrayList<>()).add(toWordExportMap(e));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeZip(baos, "word_", groups);
        return baos.toByteArray();
    }

    public byte[] exportGrammars() throws IOException {
        List<GrammarEntity> all = grammarRepository.findAll(Sort.by("id"));
        Map<Integer, List<Map<String, Object>>> groups = new TreeMap<>();
        for (GrammarEntity e : all) {
            int start = ((e.getId() - 1) / 100) * 100 + 1;
            groups.computeIfAbsent(start, _ -> new ArrayList<>()).add(toGrammarExportMap(e));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeZip(baos, "grammar_", groups);
        return baos.toByteArray();
    }

    public byte[] exportExamples() throws IOException {
        List<ExampleEntity> all = exampleRepository.findAll(Sort.by("id"));
        Map<Integer, List<Map<String, Object>>> groups = new TreeMap<>();
        for (ExampleEntity e : all) {
            int start = ((e.getId() - 1) / 100) * 100 + 1;
            groups.computeIfAbsent(start, _ -> new ArrayList<>()).add(toExampleExportMap(e));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeZip(baos, "example_", groups);
        return baos.toByteArray();
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
        return map;
    }

    private Map<String, Object> toGrammarExportMap(GrammarEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("pattern", e.getPattern());
        map.put("reading", e.getReading());
        map.put("meaning", e.getMeaning());
        map.put("notes", e.getNotes());
        return map;
    }

    private Map<String, Object> toExampleExportMap(ExampleEntity e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("jp", e.getJp());
        map.put("cn", e.getCn());
        map.put("relatedWords", e.getRelatedWords());
        map.put("relatedGrammars", e.getRelatedGrammars());
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