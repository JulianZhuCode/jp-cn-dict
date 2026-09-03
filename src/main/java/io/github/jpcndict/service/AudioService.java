package io.github.jpcndict.service;

import io.github.springwhale.framework.core.model.TtsRequest;
import io.github.springwhale.framework.core.model.TtsResult;
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

    private final String audioDir;
    private final String voice;

    public AudioService(@Value("${app.audio.dir:./audio}") String audioDir,
                        @Value("${app.audio.voice:ja-JP-NanamiNeural}") String voice) {
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
    public String generateWordAudio(Long wordId, String text) {
        return generateAudio("word", wordId, text);
    }

    /**
     * Generate audio file for an example.
     *
     * @param exampleId the example ID
     * @param text      the text to synthesize
     * @return the relative URL path, or null if generation failed
     */
    public String generateExampleAudio(Long exampleId, String text) {
        return generateAudio("example", exampleId, text);
    }

    /**
     * Batch generate audio for multiple words concurrently.
     *
     * @param wordIds   list of word IDs
     * @param wordTexts list of corresponding word texts
     * @return list of generated URL paths (null entries indicate failure)
     */
    public List<String> batchGenerateWordAudio(List<Long> wordIds, List<String> wordTexts) {
        return batchGenerateAudio("word", wordIds, wordTexts);
    }

    /**
     * Batch generate audio for multiple words concurrently with per-item callback.
     *
     * @param wordIds       list of word IDs
     * @param wordTexts     list of corresponding word texts
     * @param itemCallback  callback invoked immediately after each item completes (id, success, url)
     */
    public void batchGenerateWordAudio(List<Long> wordIds, List<String> wordTexts,
                                       AudioItemCallback itemCallback) {
        batchGenerateAudio("word", wordIds, wordTexts, itemCallback);
    }

    /**
     * Batch generate audio for multiple examples concurrently.
     *
     * @param exampleIds   list of example IDs
     * @param exampleTexts list of corresponding example texts
     * @return list of generated URL paths (null entries indicate failure)
     */
    public List<String> batchGenerateExampleAudio(List<Long> exampleIds, List<String> exampleTexts) {
        return batchGenerateAudio("example", exampleIds, exampleTexts);
    }

    /**
     * Batch generate audio for multiple examples concurrently with per-item callback.
     *
     * @param exampleIds    list of example IDs
     * @param exampleTexts  list of corresponding example texts
     * @param itemCallback  callback invoked immediately after each item completes (id, success, url)
     */
    public void batchGenerateExampleAudio(List<Long> exampleIds, List<String> exampleTexts,
                                          AudioItemCallback itemCallback) {
        batchGenerateAudio("example", exampleIds, exampleTexts, itemCallback);
    }

    /**
     * Callback interface for per-item audio generation results.
     */
    @FunctionalInterface
    public interface AudioItemCallback {
        /**
         * Called when a single item's audio generation completes, with error details.
         *
         * @param id            the item ID
         * @param success       true if successful
         * @param url           the generated URL, or null if failed
         * @param errorMessage  error details when failed
         */
        void onItemResult(long id, boolean success, String url, String errorMessage);

        /**
         * Called when a single item's audio generation completes.
         *
         * @param id      the item ID
         * @param success true if successful
         * @param url     the generated URL, or null if failed
         */
        default void onItemResult(long id, boolean success, String url) {
            onItemResult(id, success, url, null);
        }
    }

    /**
     * Delete audio file for a word.
     */
    public void deleteWordAudio(Long wordId) {
        deleteAudio("word", wordId);
    }

    /**
     * Delete audio file for an example.
     */
    public void deleteExampleAudio(Long exampleId) {
        deleteAudio("example", exampleId);
    }

    private String generateAudio(String type, Long id, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Path dirPath = Paths.get(audioDir, type);
        Path filePath = dirPath.resolve(id + ".mp3");

        try {
            Files.createDirectories(dirPath);

            TtsResult result = EdgeTtsUtil.ttsToMp3(text, voice, filePath.toString());
            if (result.success()) {
                String url = "/audio/" + type + "/" + id + ".mp3";
                log.info("Generated audio: type={}, id={}, url={}", type, id, url);
                return url;
            } else {
                log.warn("Failed to generate audio: type={}, id={}, error={}", type, id, result.errorMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating audio: type={}, id={}", type, id, e);
            return null;
        }
    }

    private List<String> batchGenerateAudio(String type, List<Long> ids, List<String> texts) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Path dirPath = Paths.get(audioDir, type);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.warn("Failed to create directory for type={}", type, e);
        }

        List<TtsRequest> requests = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                continue;
            }
            Path filePath = dirPath.resolve(id + ".mp3");
            requests.add(new TtsRequest(
                    String.valueOf(id), text, voice, filePath.toString()));
        }

        if (requests.isEmpty()) {
            return new ArrayList<>(Collections.nCopies(ids.size(), null));
        }

        List<TtsResult> results = EdgeTtsUtil.ttsToMp3Batch(requests);

        Map<String, String> resultMap = new HashMap<>();
        for (TtsResult result : results) {
            if (result.success()) {
                resultMap.put(result.id(), "/audio/" + type + "/" + result.id() + ".mp3");
            } else {
                log.warn("Batch audio generation failed: type={}, id={}, error={}",
                        type, result.id(), result.errorMessage());
            }
        }

        List<String> urls = new ArrayList<>(ids.size());
        for (Long id : ids) {
            urls.add(resultMap.get(String.valueOf(id)));
        }
        return urls;
    }

    private void batchGenerateAudio(String type, List<Long> ids, List<String> texts,
                                    AudioItemCallback itemCallback) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        Path dirPath = Paths.get(audioDir, type);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.warn("Failed to create directory for type={}", type, e);
        }

        List<TtsRequest> requests = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                if (itemCallback != null) {
                    itemCallback.onItemResult(id, false, null, "文本内容为空，跳过生成");
                }
                continue;
            }
            Path filePath = dirPath.resolve(id + ".mp3");
            requests.add(new TtsRequest(
                    String.valueOf(id), text, voice, filePath.toString()));
        }

        if (requests.isEmpty()) {
            return;
        }

        List<TtsResult> results = EdgeTtsUtil.ttsToMp3Batch(requests);
        for (TtsResult result : results) {
            long itemId = Long.parseLong(result.id());
            String url = result.success() ? "/audio/" + type + "/" + result.id() + ".mp3" : null;
            if (!result.success()) {
                log.warn("Batch audio generation failed: type={}, id={}, error={}",
                        type, result.id(), result.errorMessage());
            }
            if (itemCallback != null) {
                String errMsg = result.success() ? null :
                        (result.errorMessage() != null ? result.errorMessage() : "音频生成失败");
                itemCallback.onItemResult(itemId, result.success(), url, errMsg);
            }
        }
    }

    private void deleteAudio(String type, Long id) {
        Path filePath = Paths.get(audioDir, type, id + ".mp3");
        try {
            Files.deleteIfExists(filePath);
            log.info("Deleted audio: type={}, id={}", type, id);
        } catch (IOException e) {
            log.warn("Failed to delete audio: type={}, id={}", type, id, e);
        }
    }
}