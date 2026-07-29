package io.github.jpcndict.service;

import io.github.springwhale.framework.core.utils.EdgeTtsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AudioService {

    private final EdgeTtsUtil edgeTtsUtil;
    private final String audioDir;
    private final String voice;

    public AudioService(EdgeTtsUtil edgeTtsUtil,
                        @Value("${app.audio.dir:./audio}") String audioDir,
                        @Value("${app.audio.voice:ja-JP-NanamiNeural}") String voice) {
        this.edgeTtsUtil = edgeTtsUtil;
        this.audioDir = audioDir;
        this.voice = voice;
    }

    /**
     * Generate audio file for a word.
     *
     * @param wordId the word ID
     * @param text   the text to synthesize
     * @return the relative URL path, or null if generation failed
     */
    public String generateWordAudio(Integer wordId, String text) {
        return generateAudio("word", wordId, text);
    }

    /**
     * Generate audio file for an example.
     *
     * @param exampleId the example ID
     * @param text      the text to synthesize
     * @return the relative URL path, or null if generation failed
     */
    public String generateExampleAudio(Integer exampleId, String text) {
        return generateAudio("example", exampleId, text);
    }

    /**
     * Batch generate audio for multiple words concurrently.
     *
     * @param wordIds   list of word IDs
     * @param wordTexts list of corresponding word texts
     * @return list of generated URL paths (null entries indicate failure)
     */
    public List<String> batchGenerateWordAudio(List<Integer> wordIds, List<String> wordTexts) {
        return batchGenerateAudio("word", wordIds, wordTexts);
    }

    /**
     * Batch generate audio for multiple examples concurrently.
     *
     * @param exampleIds   list of example IDs
     * @param exampleTexts list of corresponding example texts
     * @return list of generated URL paths (null entries indicate failure)
     */
    public List<String> batchGenerateExampleAudio(List<Integer> exampleIds, List<String> exampleTexts) {
        return batchGenerateAudio("example", exampleIds, exampleTexts);
    }

    /**
     * Delete audio file for a word.
     */
    public void deleteWordAudio(Integer wordId) {
        deleteAudio("word", wordId);
    }

    /**
     * Delete audio file for an example.
     */
    public void deleteExampleAudio(Integer exampleId) {
        deleteAudio("example", exampleId);
    }

    private String generateAudio(String type, Integer id, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Path dirPath = Paths.get(audioDir, type);
        Path filePath = dirPath.resolve(id + ".mp3");

        try {
            Files.createDirectories(dirPath);

            boolean success = edgeTtsUtil.ttsToMp3(text, voice, filePath.toString());
            if (success) {
                String url = "/audio/" + type + "/" + id + ".mp3";
                log.info("Generated audio: type={}, id={}, url={}", type, id, url);
                return url;
            } else {
                log.warn("Failed to generate audio: type={}, id={}", type, id);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating audio: type={}, id={}", type, id, e);
            return null;
        }
    }

    private List<String> batchGenerateAudio(String type, List<Integer> ids, List<String> texts) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Path dirPath = Paths.get(audioDir, type);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.warn("Failed to create directory for type={}", type, e);
        }

        List<EdgeTtsUtil.TtsRequest> requests = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                continue;
            }
            Path filePath = dirPath.resolve(id + ".mp3");
            requests.add(new EdgeTtsUtil.TtsRequest(
                    String.valueOf(id), text, voice, filePath.toString()));
        }

        if (requests.isEmpty()) {
            return new ArrayList<>(Collections.nCopies(ids.size(), null));
        }

        List<EdgeTtsUtil.TtsResult> results = edgeTtsUtil.ttsToMp3Batch(requests);

        Map<String, String> resultMap = new HashMap<>();
        for (EdgeTtsUtil.TtsResult result : results) {
            if (result.success()) {
                resultMap.put(result.id(), "/audio/" + type + "/" + result.id() + ".mp3");
            } else {
                log.warn("Batch audio generation failed: type={}, id={}, error={}",
                        type, result.id(), result.errorMessage());
            }
        }

        List<String> urls = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            urls.add(resultMap.get(String.valueOf(id)));
        }
        return urls;
    }

    private void deleteAudio(String type, Integer id) {
        Path filePath = Paths.get(audioDir, type, id + ".mp3");
        try {
            Files.deleteIfExists(filePath);
            log.info("Deleted audio: type={}, id={}", type, id);
        } catch (IOException e) {
            log.warn("Failed to delete audio: type={}, id={}", type, id, e);
        }
    }
}
