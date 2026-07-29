package io.github.jpcndict.service;

import io.github.springwhale.framework.core.utils.EdgeTtsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class AudioService {

    private static final String VOICE = "ja-JP-NanamiNeural";

    @Value("${app.audio.dir:./audio}")
    private String audioDir;

    @Value("${app.audio.voice:" + VOICE + "}")
    private String voice;

    /**
     * Generate audio file for a word.
     *
     * @param wordId the word ID
     * @param text  the text to synthesize
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

            boolean success = EdgeTtsUtil.ttsToMp3(text, voice, filePath.toString());
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